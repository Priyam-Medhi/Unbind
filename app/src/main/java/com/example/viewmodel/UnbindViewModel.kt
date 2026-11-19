package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MindfulAiRepository
import com.example.data.UnbindFirestoreRepository
import com.example.model.AiFreshInsight
import com.example.model.AiLoadState
import com.example.model.AiPatternAnalysis
import com.example.model.CheckInLog
import com.example.model.CheckInOutcome
import com.example.model.CopilotMessage
import com.example.model.CuratedRecoveryContent
import com.example.model.GrowthTask
import com.example.model.HabitArchetype
import com.example.model.HabitSpace
import com.example.model.RewardGoal
import com.example.model.SavedInsight
import com.example.model.TaskCategory
import com.example.model.UiState
import com.example.model.WorkspaceTab
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "UnbindViewModel"
private const val STOP_TIMEOUT_MS = 5000L

@OptIn(ExperimentalCoroutinesApi::class)
class UnbindViewModel(
    private val repository: UnbindFirestoreRepository,
    private val aiRepository: MindfulAiRepository,
    private val currentUserId: String,
) : ViewModel() {

    private val _selectedSpaceId = MutableStateFlow<String?>(null)
    val selectedSpaceId: StateFlow<String?> = _selectedSpaceId.asStateFlow()

    private val _selectedTab = MutableStateFlow(WorkspaceTab.TRACKER)
    val selectedTab: StateFlow<WorkspaceTab> = _selectedTab.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // Per-space AI Pattern Analysis cache
    private val _patternStates = MutableStateFlow<Map<String, AiLoadState<AiPatternAnalysis>>>(emptyMap())
    val patternStates: StateFlow<Map<String, AiLoadState<AiPatternAnalysis>>> = _patternStates.asStateFlow()

    // Per-space Fresh Insights state
    private val _insightStates = MutableStateFlow<Map<String, AiLoadState<AiFreshInsight>>>(emptyMap())
    val insightStates: StateFlow<Map<String, AiLoadState<AiFreshInsight>>> = _insightStates.asStateFlow()

    // Per-space Mindful AI Copilot conversations
    private val _copilotMessages = MutableStateFlow<Map<String, List<CopilotMessage>>>(emptyMap())
    val copilotMessages: StateFlow<Map<String, List<CopilotMessage>>> = _copilotMessages.asStateFlow()

    private val _copilotError = MutableStateFlow<String?>(null)
    val copilotError: StateFlow<String?> = _copilotError.asStateFlow()

    private val _isCopilotStreaming = MutableStateFlow(false)
    val isCopilotStreaming: StateFlow<Boolean> = _isCopilotStreaming.asStateFlow()

    val todayDateKey: String
        get() = repository.todayDateKey()

    // --- Real-time Firestore Streams with Two-Tier Error Unwrapping ---

    val spacesState: StateFlow<UiState<List<HabitSpace>>> =
        repository.observeSpaces(currentUserId)
            .map<List<HabitSpace>, UiState<List<HabitSpace>>> { UiState.Success(it) }
            .catch { error -> emit(unwrapFirestoreError(error, "Failed to load Focus Spaces")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val activeCheckInsState: StateFlow<UiState<List<CheckInLog>>> =
        _selectedSpaceId
            .flatMapLatest { spaceId ->
                if (spaceId.isNullOrBlank()) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    repository.observeCheckIns(currentUserId, spaceId)
                        .map<List<CheckInLog>, UiState<List<CheckInLog>>> { UiState.Success(it) }
                        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load check-ins")) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val activeTasksState: StateFlow<UiState<List<GrowthTask>>> =
        _selectedSpaceId
            .flatMapLatest { spaceId ->
                if (spaceId.isNullOrBlank()) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    repository.observeTasks(currentUserId, spaceId)
                        .map<List<GrowthTask>, UiState<List<GrowthTask>>> { UiState.Success(it) }
                        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load Daily Architecture")) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val activeRewardsState: StateFlow<UiState<List<RewardGoal>>> =
        _selectedSpaceId
            .flatMapLatest { spaceId ->
                if (spaceId.isNullOrBlank()) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    repository.observeRewards(currentUserId, spaceId)
                        .map<List<RewardGoal>, UiState<List<RewardGoal>>> { UiState.Success(it) }
                        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load rewards")) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val activeSavedInsightsState: StateFlow<UiState<List<SavedInsight>>> =
        _selectedSpaceId
            .flatMapLatest { spaceId ->
                if (spaceId.isNullOrBlank()) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    repository.observeSavedInsights(currentUserId, spaceId)
                        .map<List<SavedInsight>, UiState<List<SavedInsight>>> { UiState.Success(it) }
                        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load saved insights")) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    // --- Navigation Actions ---

    fun openSpace(spaceId: String, initialTab: WorkspaceTab = WorkspaceTab.TRACKER) {
        _selectedSpaceId.value = spaceId
        _selectedTab.value = initialTab
        _copilotError.value = null
    }

    fun returnToSpacesHub() {
        _selectedSpaceId.value = null
    }

    fun selectTab(tab: WorkspaceTab) {
        _selectedTab.value = tab
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun clearActionError() {
        _actionError.value = null
    }

    // --- Space CRUD ---

    fun createSpace(
        title: String,
        archetype: HabitArchetype,
        dailyCostUsd: Double,
        dailyHoursSaved: Double,
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            _actionError.value = null
            repository.createSpace(title, archetype, dailyCostUsd, dailyHoursSaved)
                .onSuccess { newSpaceId ->
                    _selectedSpaceId.value = newSpaceId
                    _selectedTab.value = WorkspaceTab.TRACKER
                    _statusBanner.value = "Created isolated sanctuary: ${title.trim()}"
                }
                .onFailure { err ->
                    _actionError.value = err.localizedMessage ?: "Could not create habit space."
                }
            _isBusy.value = false
        }
    }

    fun updateSpaceValuation(spaceId: String, dailyCostUsd: Double, dailyHoursSaved: Double) {
        viewModelScope.launch {
            repository.updateSpaceValuation(spaceId, dailyCostUsd, dailyHoursSaved)
                .onSuccess { _statusBanner.value = "Updated daily savings & reclaimed time rate." }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Update failed." }
        }
    }

    fun deleteSpace(spaceId: String) {
        viewModelScope.launch {
            repository.deleteSpace(spaceId)
                .onSuccess {
                    if (_selectedSpaceId.value == spaceId) {
                        _selectedSpaceId.value = null
                    }
                    _statusBanner.value = "Focus Space removed."
                }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Delete failed." }
        }
    }

    // --- Daily Check-In Actions ---

    fun logDailyCheckIn(
        space: HabitSpace,
        outcome: CheckInOutcome,
        urgeLevel: Long = if (outcome == CheckInOutcome.CLEAN) 2L else 7L,
        peakUrgeWindow: String = "",
        reflectionNote: String = "",
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.recordDailyCheckIn(space, outcome, urgeLevel, peakUrgeWindow, reflectionNote)
                .onSuccess {
                    _statusBanner.value = when (outcome) {
                        CheckInOutcome.CLEAN, CheckInOutcome.MILD_URGE -> "Today's clean check-in saved. Streak strengthened."
                        CheckInOutcome.RELAPSE -> "Gentle safe reset logged. Tomorrow begins fresh."
                        CheckInOutcome.NONE -> null
                    }
                }
                .onFailure { err ->
                    _actionError.value = err.localizedMessage ?: "Could not save check-in."
                }
            _isBusy.value = false
        }
    }

    // --- Growth Task Actions ---

    fun addTask(
        spaceId: String,
        title: String,
        subtitle: String,
        category: TaskCategory,
        iconKey: String = "leaf",
    ) {
        viewModelScope.launch {
            val currentCount = (activeTasksState.value as? UiState.Success)?.data?.size?.toLong() ?: 0L
            repository.addTask(spaceId, title, subtitle, category, iconKey, currentCount)
                .onSuccess { _statusBanner.value = "Added to ${category.title}." }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not add task." }
        }
    }

    fun toggleTaskCompletion(spaceId: String, task: GrowthTask) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(spaceId, task)
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not update task." }
        }
    }

    fun reclassifyTask(spaceId: String, taskId: String, newCategory: TaskCategory) {
        viewModelScope.launch {
            repository.reclassifyTask(spaceId, taskId, newCategory)
                .onSuccess { _statusBanner.value = "Moved to ${newCategory.title}." }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not move task." }
        }
    }

    fun deleteTask(spaceId: String, taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(spaceId, taskId)
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not delete task." }
        }
    }

    // --- Reward Actions ---

    fun addReward(
        spaceId: String,
        title: String,
        subtitle: String,
        targetDays: Long,
        budgetUsd: Double,
        coverPresetKey: String,
        localImageUri: String?,
    ) {
        viewModelScope.launch {
            repository.addReward(spaceId, title, subtitle, targetDays, budgetUsd, coverPresetKey, localImageUri)
                .onSuccess { _statusBanner.value = "Added \"$title\" to your Vision Board." }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not save reward." }
        }
    }

    fun redeemReward(space: HabitSpace, reward: RewardGoal) {
        viewModelScope.launch {
            repository.redeemReward(space, reward)
                .onSuccess { _statusBanner.value = "Redeemed \"${reward.title}\" — celebrate mindfully!" }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not redeem reward." }
        }
    }

    fun deleteReward(spaceId: String, rewardId: String) {
        viewModelScope.launch {
            repository.deleteReward(spaceId, rewardId)
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not remove reward." }
        }
    }

    // --- Saved Insight Bookmark Actions ---

    fun bookmarkInsight(spaceId: String, insight: AiFreshInsight, reflectionNote: String = "") {
        viewModelScope.launch {
            repository.saveInsightBookmark(
                spaceId = spaceId,
                quote = insight.quote,
                author = insight.author,
                source = insight.source,
                reflectionNote = reflectionNote,
            )
                .onSuccess { _statusBanner.value = "Anchor saved to your sanctuary." }
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not save anchor." }
        }
    }

    fun removeSavedInsight(spaceId: String, insightId: String) {
        viewModelScope.launch {
            repository.removeSavedInsight(spaceId, insightId)
                .onFailure { err -> _actionError.value = err.localizedMessage ?: "Could not remove bookmark." }
        }
    }

    // --- Live Gemini AI Actions (Firebase AI Logic) ---

    fun runPatternAnalysis(space: HabitSpace, checkIns: List<CheckInLog>) {
        viewModelScope.launch {
            _patternStates.value = _patternStates.value + (space.id to AiLoadState.Loading)
            aiRepository.analyzePatterns(space, checkIns)
                .onSuccess { analysis ->
                    _patternStates.value = _patternStates.value + (space.id to AiLoadState.Success(analysis))
                }
                .onFailure { err ->
                    _patternStates.value = _patternStates.value + (
                        space.id to AiLoadState.Error(err.localizedMessage ?: "AI Pattern Analysis failed.")
                    )
                }
        }
    }

    fun refreshFreshInsight(space: HabitSpace) {
        viewModelScope.launch {
            _insightStates.value = _insightStates.value + (space.id to AiLoadState.Loading)
            aiRepository.generateFreshInsight(space)
                .onSuccess { fresh ->
                    _insightStates.value = _insightStates.value + (space.id to AiLoadState.Success(fresh))
                }
                .onFailure { err ->
                    _insightStates.value = _insightStates.value + (
                        space.id to AiLoadState.Error(err.localizedMessage ?: "Could not fetch fresh insights.")
                    )
                }
        }
    }

    fun currentInsightFor(space: HabitSpace): AiFreshInsight {
        val state = _insightStates.value[space.id]
        return if (state is AiLoadState.Success) {
            state.data
        } else {
            CuratedRecoveryContent.defaultAnchorQuoteFor(space.parsedArchetype)
        }
    }

    fun sendCopilotPrompt(space: HabitSpace, promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty() || _isCopilotStreaming.value) return

        val existingHistory = _copilotMessages.value[space.id].orEmpty()
        val userMsg = CopilotMessage(
            id = UUID.randomUUID().toString(),
            fromUser = true,
            text = cleanPrompt,
        )
        val assistantMsgId = UUID.randomUUID().toString()
        val placeholderAssistant = CopilotMessage(
            id = assistantMsgId,
            fromUser = false,
            text = "",
            streaming = true,
        )

        _copilotError.value = null
        _isCopilotStreaming.value = true
        _copilotMessages.value = _copilotMessages.value + (space.id to (existingHistory + userMsg + placeholderAssistant))

        viewModelScope.launch {
            try {
                var latestText = ""
                aiRepository.streamCopilotReply(space, existingHistory, cleanPrompt).collect { streamed ->
                    latestText = streamed
                    updateCopilotMessage(space.id, assistantMsgId, streamed, streaming = true)
                }
                updateCopilotMessage(space.id, assistantMsgId, latestText, streaming = false)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Copilot timed out", e)
                removeEmptyCopilotPlaceholder(space.id, assistantMsgId)
                _copilotError.value = "Mindful AI Copilot timed out after 60 seconds. Please try again."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Copilot streaming failed", e)
                removeEmptyCopilotPlaceholder(space.id, assistantMsgId)
                _copilotError.value = e.localizedMessage ?: "Unable to reach Mindful AI Copilot."
            } finally {
                _isCopilotStreaming.value = false
            }
        }
    }

    private fun updateCopilotMessage(
        spaceId: String,
        messageId: String,
        text: String,
        streaming: Boolean,
    ) {
        val current = _copilotMessages.value[spaceId].orEmpty()
        val updated = current.map { msg ->
            if (msg.id == messageId) msg.copy(text = text, streaming = streaming) else msg
        }
        _copilotMessages.value = _copilotMessages.value + (spaceId to updated)
    }

    private fun removeEmptyCopilotPlaceholder(spaceId: String, messageId: String) {
        val current = _copilotMessages.value[spaceId].orEmpty()
        val cleaned = current.filterNot { it.id == messageId && it.text.isBlank() }
        _copilotMessages.value = _copilotMessages.value + (spaceId to cleaned)
    }

    private fun unwrapFirestoreError(error: Throwable, fallback: String): UiState.Error {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, fallback, rootError)
        return UiState.Error(rootError.message ?: fallback)
    }
}
