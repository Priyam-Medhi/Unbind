package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.CheckInLog
import com.example.model.CheckInOutcome
import com.example.model.GrowthTask
import com.example.model.HabitArchetype
import com.example.model.HabitSpace
import com.example.model.RewardGoal
import com.example.model.SavedInsight
import com.example.model.TaskCategory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnbindFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    // Always initialize with the provisioned custom database ID (never parameterless getInstance())
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id),
        ),
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun userSpacesCollection(uid: String) =
        db.collection("users").document(uid).collection("spaces")

    private fun spaceDoc(uid: String, spaceId: String) =
        userSpacesCollection(uid).document(spaceId)

    fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // --- Real-Time Observations (First-Party snapshots() Flows with ESTIMATE behavior) ---

    fun observeSpaces(userId: String): Flow<List<HabitSpace>> = flow {
        val path = "users/$userId/spaces"
        emitAll(
            userSpacesCollection(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        HabitSpace::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamErrorIfAuthenticated(error, userId, OperationType.LIST, path)
                    throw error
                },
        )
    }

    fun observeCheckIns(userId: String, spaceId: String): Flow<List<CheckInLog>> = flow {
        val path = "users/$userId/spaces/$spaceId/checkIns"
        emitAll(
            spaceDoc(userId, spaceId)
                .collection("checkIns")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CheckInLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedBy { it.dateKey }
                }
                .catch { error ->
                    logStreamErrorIfAuthenticated(error, userId, OperationType.LIST, path)
                    throw error
                },
        )
    }

    fun observeTasks(userId: String, spaceId: String): Flow<List<GrowthTask>> = flow {
        val path = "users/$userId/spaces/$spaceId/tasks"
        emitAll(
            spaceDoc(userId, spaceId)
                .collection("tasks")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GrowthTask::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedWith(compareBy<GrowthTask> { it.sortOrder }.thenBy { it.createdAt?.seconds ?: 0L })
                }
                .catch { error ->
                    logStreamErrorIfAuthenticated(error, userId, OperationType.LIST, path)
                    throw error
                },
        )
    }

    fun observeRewards(userId: String, spaceId: String): Flow<List<RewardGoal>> = flow {
        val path = "users/$userId/spaces/$spaceId/rewards"
        emitAll(
            spaceDoc(userId, spaceId)
                .collection("rewards")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        RewardGoal::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedWith(compareBy<RewardGoal> { it.redeemed }.thenBy { it.targetDays })
                }
                .catch { error ->
                    logStreamErrorIfAuthenticated(error, userId, OperationType.LIST, path)
                    throw error
                },
        )
    }

    fun observeSavedInsights(userId: String, spaceId: String): Flow<List<SavedInsight>> = flow {
        val path = "users/$userId/spaces/$spaceId/savedInsights"
        emitAll(
            spaceDoc(userId, spaceId)
                .collection("savedInsights")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedInsight::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logStreamErrorIfAuthenticated(error, userId, OperationType.LIST, path)
                    throw error
                },
        )
    }

    // --- Space Mutations ---

    suspend fun createSpace(
        title: String,
        archetype: HabitArchetype,
        dailyCostUsd: Double,
        dailyHoursSaved: Double,
    ): Result<String> {
        val cleanTitle = title.trim().take(60)
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces"
        return try {
            val uid = requireUserId()
            require(cleanTitle.isNotEmpty()) { "Habit name cannot be empty." }
            val safeCost = dailyCostUsd.coerceIn(0.0, 10000.0)
            val safeHours = dailyHoursSaved.coerceIn(0.0, 24.0)

            val docRef = userSpacesCollection(uid).document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "archetype" to archetype.wireValue,
                "iconKey" to archetype.iconKey,
                "dailyCostUsd" to safeCost,
                "dailyHoursSaved" to safeHours,
                "currentStreakDays" to 0L,
                "longestStreakDays" to 0L,
                "totalCleanDays" to 0L,
                "claimedBudgetUsd" to 0.0,
                "lastCheckInDate" to "",
                "lastCheckInOutcome" to CheckInOutcome.NONE.wireValue,
                "ritualStatusNote" to archetype.defaultRitualNote.take(160),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateSpaceValuation(
        spaceId: String,
        dailyCostUsd: Double,
        dailyHoursSaved: Double,
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId"
        return try {
            val uid = requireUserId()
            require(spaceId.isNotBlank()) { "Space ID cannot be blank." }
            val docRef = spaceDoc(uid, spaceId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "dailyCostUsd" to dailyCostUsd.coerceIn(0.0, 10000.0),
                    "dailyHoursSaved" to dailyHoursSaved.coerceIn(0.0, 24.0),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteSpace(spaceId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId"
        return try {
            val uid = requireUserId()
            require(spaceId.isNotBlank()) { "Space ID cannot be blank." }
            val docRef = spaceDoc(uid, spaceId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Daily Check-In Mutation ---

    suspend fun recordDailyCheckIn(
        space: HabitSpace,
        outcome: CheckInOutcome,
        urgeLevel: Long,
        peakUrgeWindow: String,
        reflectionNote: String,
    ): Result<Unit> {
        val dateKey = todayDateKey()
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/${space.id}/checkIns/$dateKey"
        return try {
            val uid = requireUserId()
            require(space.id.isNotBlank()) { "Space ID cannot be blank." }
            val validOutcome = if (outcome == CheckInOutcome.NONE) CheckInOutcome.CLEAN else outcome
            val safeUrge = urgeLevel.coerceIn(0L, 10L)
            val safeWindow = peakUrgeWindow.trim().take(60)
            val safeReflection = reflectionNote.trim().take(500)

            val checkInRef = spaceDoc(uid, space.id).collection("checkIns").document(dateKey)
            docPath = checkInRef.path
            val existingSnap = checkInRef.get().await()
            val previouslyLoggedToday = existingSnap.exists()
            val previousOutcome = CheckInOutcome.fromWire(existingSnap.getString("outcome"))

            if (previouslyLoggedToday) {
                checkInRef.update(
                    mapOf(
                        "outcome" to validOutcome.wireValue,
                        "urgeLevel" to safeUrge,
                        "peakUrgeWindow" to safeWindow,
                        "reflectionNote" to safeReflection,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            } else {
                checkInRef.set(
                    mapOf(
                        "userId" to uid,
                        "spaceId" to space.id,
                        "dateKey" to dateKey,
                        "outcome" to validOutcome.wireValue,
                        "urgeLevel" to safeUrge,
                        "peakUrgeWindow" to safeWindow,
                        "reflectionNote" to safeReflection,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            }

            // Recompute space counters deterministically without double-counting same-day edits
            val wasCleanToday = previouslyLoggedToday && (previousOutcome == CheckInOutcome.CLEAN || previousOutcome == CheckInOutcome.MILD_URGE)
            val isCleanNow = validOutcome == CheckInOutcome.CLEAN || validOutcome == CheckInOutcome.MILD_URGE

            var nextStreak = space.currentStreakDays
            var nextTotalClean = space.totalCleanDays

            when {
                !previouslyLoggedToday && isCleanNow -> {
                    nextStreak = space.currentStreakDays + 1L
                    nextTotalClean = space.totalCleanDays + 1L
                }
                !previouslyLoggedToday && !isCleanNow -> {
                    nextStreak = 0L
                }
                wasCleanToday && !isCleanNow -> {
                    nextStreak = 0L
                    nextTotalClean = (space.totalCleanDays - 1L).coerceAtLeast(0L)
                }
                !wasCleanToday && isCleanNow -> {
                    nextStreak = 1L
                    nextTotalClean = space.totalCleanDays + 1L
                }
            }

            val nextLongest = maxOf(space.longestStreakDays, nextStreak)
            val ritualNote = when (validOutcome) {
                CheckInOutcome.CLEAN -> if (safeReflection.isNotBlank()) safeReflection.take(140) else "Clean commitment honored today"
                CheckInOutcome.MILD_URGE -> "Navigated mild urge and stayed clean"
                CheckInOutcome.RELAPSE -> "Gentle safe reset logged — fresh start tomorrow"
                CheckInOutcome.NONE -> space.ritualStatusNote
            }

            val parentRef = spaceDoc(uid, space.id)
            docPath = parentRef.path
            parentRef.update(
                mapOf(
                    "currentStreakDays" to nextStreak,
                    "longestStreakDays" to nextLongest,
                    "totalCleanDays" to nextTotalClean,
                    "lastCheckInDate" to dateKey,
                    "lastCheckInOutcome" to validOutcome.wireValue,
                    "ritualStatusNote" to ritualNote.take(160),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- Growth Task Mutations ---

    suspend fun addTask(
        spaceId: String,
        title: String,
        subtitle: String,
        category: TaskCategory,
        iconKey: String,
        sortOrder: Long = 0L,
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/tasks"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "Task title cannot be empty." }
            val docRef = spaceDoc(uid, spaceId).collection("tasks").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "spaceId" to spaceId,
                    "title" to cleanTitle,
                    "subtitle" to subtitle.trim().take(120),
                    "category" to category.wireValue,
                    "iconKey" to iconKey.ifBlank { "leaf" }.take(30),
                    "completedDate" to "",
                    "sortOrder" to sortOrder.coerceIn(0L, 10000L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleTaskCompletion(spaceId: String, task: GrowthTask): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/tasks/${task.id}"
        return try {
            val uid = requireUserId()
            require(task.id.isNotBlank()) { "Task ID cannot be blank." }
            val today = todayDateKey()
            val nextDate = if (task.completedDate == today) "" else today
            val docRef = spaceDoc(uid, spaceId).collection("tasks").document(task.id)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "completedDate" to nextDate,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun reclassifyTask(
        spaceId: String,
        taskId: String,
        newCategory: TaskCategory,
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/tasks/$taskId"
        return try {
            val uid = requireUserId()
            require(taskId.isNotBlank()) { "Task ID cannot be blank." }
            val docRef = spaceDoc(uid, spaceId).collection("tasks").document(taskId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "category" to newCategory.wireValue,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteTask(spaceId: String, taskId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/tasks/$taskId"
        return try {
            val uid = requireUserId()
            require(taskId.isNotBlank()) { "Task ID cannot be blank." }
            val docRef = spaceDoc(uid, spaceId).collection("tasks").document(taskId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Reward Mutations ---

    suspend fun addReward(
        spaceId: String,
        title: String,
        subtitle: String,
        targetDays: Long,
        budgetUsd: Double,
        coverPresetKey: String,
        localImageUri: String?,
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/rewards"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(100)
            require(cleanTitle.isNotEmpty()) { "Reward title cannot be empty." }
            val docRef = spaceDoc(uid, spaceId).collection("rewards").document()
            docPath = docRef.path

            val rawMap = mutableMapOf<String, Any?>(
                "userId" to uid,
                "spaceId" to spaceId,
                "title" to cleanTitle,
                "subtitle" to subtitle.trim().take(160),
                "targetDays" to targetDays.coerceIn(1L, 3650L),
                "budgetUsd" to budgetUsd.coerceIn(1.0, 1000000.0),
                "coverPresetKey" to coverPresetKey.ifBlank { "dinner" }.take(40),
                "localImageUri" to localImageUri?.trim()?.take(500)?.takeIf { it.isNotEmpty() },
                "redeemed" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            // Omit null optional keys so Firestore rules (!('localImageUri' in data) || ...) match 1:1
            val payload = rawMap.filterValues { it != null }.mapValues { it.value as Any }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun redeemReward(space: HabitSpace, reward: RewardGoal): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/${space.id}/rewards/${reward.id}"
        return try {
            val uid = requireUserId()
            require(reward.id.isNotBlank()) { "Reward ID cannot be blank." }
            require(!reward.redeemed) { "Reward has already been redeemed." }

            val rewardRef = spaceDoc(uid, space.id).collection("rewards").document(reward.id)
            docPath = rewardRef.path
            rewardRef.update(
                mapOf(
                    "redeemed" to true,
                    "redeemedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

            val nextClaimed = (space.claimedBudgetUsd + reward.budgetUsd).coerceAtMost(10000000.0)
            val parentRef = spaceDoc(uid, space.id)
            docPath = parentRef.path
            parentRef.update(
                mapOf(
                    "claimedBudgetUsd" to nextClaimed,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteReward(spaceId: String, rewardId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/rewards/$rewardId"
        return try {
            val uid = requireUserId()
            require(rewardId.isNotBlank()) { "Reward ID cannot be blank." }
            val docRef = spaceDoc(uid, spaceId).collection("rewards").document(rewardId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Saved Insight Mutations ---

    suspend fun saveInsightBookmark(
        spaceId: String,
        quote: String,
        author: String,
        source: String,
        reflectionNote: String = "",
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/savedInsights"
        return try {
            val uid = requireUserId()
            val cleanQuote = quote.trim().take(600)
            require(cleanQuote.isNotEmpty()) { "Quote cannot be empty." }
            val docRef = spaceDoc(uid, spaceId).collection("savedInsights").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "spaceId" to spaceId,
                    "quote" to cleanQuote,
                    "author" to author.trim().ifEmpty { "Mindful Anchor" }.take(120),
                    "source" to source.trim().take(120),
                    "reflectionNote" to reflectionNote.trim().take(1000),
                    "bookmarked" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun removeSavedInsight(spaceId: String, insightId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/spaces/$spaceId/savedInsights/$insightId"
        return try {
            val uid = requireUserId()
            require(insightId.isNotBlank()) { "Insight ID cannot be blank." }
            val docRef = spaceDoc(uid, spaceId).collection("savedInsights").document(insightId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private fun logStreamErrorIfAuthenticated(
        error: Throwable,
        expectedUid: String,
        operationType: OperationType,
        path: String,
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }
}
