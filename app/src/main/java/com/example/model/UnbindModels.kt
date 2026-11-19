package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class WorkspaceTab(val label: String) {
    TRACKER("Tracker"),
    GROWTH("Growth"),
    REWARDS("Rewards"),
    INSIGHTS("Insights"),
}

enum class HabitArchetype(
    val wireValue: String,
    val displayTitle: String,
    val shortTitle: String,
    val iconKey: String,
    val defaultDailyCostUsd: Double,
    val defaultDailyHoursSaved: Double,
    val defaultRitualNote: String,
) {
    DOOMSCROLLING(
        wireValue = "DOOMSCROLLING",
        displayTitle = "Smartphone Doomscrolling",
        shortTitle = "Doomscrolling",
        iconKey = "phone",
        defaultDailyCostUsd = 10.0,
        defaultDailyHoursSaved = 2.0,
        defaultRitualNote = "Evening digital sunset ritual ready",
    ),
    NICOTINE(
        wireValue = "NICOTINE",
        displayTitle = "Nicotine / Vaping",
        shortTitle = "Nicotine",
        iconKey = "air",
        defaultDailyCostUsd = 12.0,
        defaultDailyHoursSaved = 0.75,
        defaultRitualNote = "Breathwork anchor ready for cravings",
    ),
    SUGAR(
        wireValue = "SUGAR",
        displayTitle = "Sugar & Junk Food",
        shortTitle = "Sugar Reset",
        iconKey = "bakery",
        defaultDailyCostUsd = 9.0,
        defaultDailyHoursSaved = 0.5,
        defaultRitualNote = "Evening kitchen boundary active",
    ),
    GAMING(
        wireValue = "GAMING",
        displayTitle = "Gaming / Late Night Screen",
        shortTitle = "Late Gaming",
        iconKey = "gamepad",
        defaultDailyCostUsd = 8.0,
        defaultDailyHoursSaved = 2.5,
        defaultRitualNote = "Nightly controller dock-off ready",
    ),
    SPENDING(
        wireValue = "SPENDING",
        displayTitle = "Compulsive Spending",
        shortTitle = "Mindful Spend",
        iconKey = "bag",
        defaultDailyCostUsd = 18.0,
        defaultDailyHoursSaved = 1.0,
        defaultRitualNote = "48-hour impulse pause protocol active",
    ),
    CUSTOM(
        wireValue = "CUSTOM",
        displayTitle = "Custom Habit",
        shortTitle = "Custom",
        iconKey = "leaf",
        defaultDailyCostUsd = 10.0,
        defaultDailyHoursSaved = 1.5,
        defaultRitualNote = "Isolated sanctuary initialized",
    );

    companion object {
        fun fromWire(raw: String?): HabitArchetype =
            entries.firstOrNull { it.wireValue == raw } ?: CUSTOM
    }
}

enum class CheckInOutcome(val wireValue: String) {
    NONE("NONE"),
    CLEAN("CLEAN"),
    MILD_URGE("MILD_URGE"),
    RELAPSE("RELAPSE");

    companion object {
        fun fromWire(raw: String?): CheckInOutcome =
            entries.firstOrNull { it.wireValue == raw } ?: NONE
    }
}

enum class TaskCategory(
    val wireValue: String,
    val title: String,
    val badge: String,
    val description: String,
) {
    NON_NEGOTIABLE(
        wireValue = "NON_NEGOTIABLE",
        title = "Non-negotiables",
        badge = "Pillars",
        description = "Anchor rituals that happen without debate. Guard these fiercely.",
    ),
    TODO(
        wireValue = "TODO",
        title = "To-do List",
        badge = "Actionable",
        description = "Proactive intentional efforts to move your creative and personal needle today.",
    ),
    NOT_TO_DO(
        wireValue = "NOT_TO_DO",
        title = "Not-to-do List",
        badge = "Boundaries",
        description = "Defensive walls against dopamine traps, mindless scrolling, and draining defaults.",
    );

    companion object {
        fun fromWire(raw: String?): TaskCategory =
            entries.firstOrNull { it.wireValue == raw } ?: TODO
    }
}

@Keep
@IgnoreExtraProperties
data class HabitSpace(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val archetype: String = HabitArchetype.CUSTOM.wireValue,
    val iconKey: String = "leaf",
    val dailyCostUsd: Double = 10.0,
    val dailyHoursSaved: Double = 1.5,
    val currentStreakDays: Long = 0L,
    val longestStreakDays: Long = 0L,
    val totalCleanDays: Long = 0L,
    val claimedBudgetUsd: Double = 0.0,
    val lastCheckInDate: String = "",
    val lastCheckInOutcome: String = CheckInOutcome.NONE.wireValue,
    val ritualStatusNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val parsedArchetype: HabitArchetype
        get() = HabitArchetype.fromWire(archetype)

    val shortDisplayTitle: String
        get() = if (title.length <= 16) title else parsedArchetype.shortTitle

    val totalHoursSaved: Double
        get() = totalCleanDays * dailyHoursSaved

    val totalSavedUsd: Double
        get() = totalCleanDays * dailyCostUsd

    val claimableBudgetUsd: Double
        get() = (totalSavedUsd - claimedBudgetUsd).coerceAtLeast(0.0)
}

@Keep
@IgnoreExtraProperties
data class CheckInLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val spaceId: String = "",
    val dateKey: String = "",
    val outcome: String = CheckInOutcome.CLEAN.wireValue,
    val urgeLevel: Long = 2L,
    val peakUrgeWindow: String = "",
    val reflectionNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val parsedOutcome: CheckInOutcome
        get() = CheckInOutcome.fromWire(outcome)
}

@Keep
@IgnoreExtraProperties
data class GrowthTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val spaceId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val category: String = TaskCategory.TODO.wireValue,
    val iconKey: String = "leaf",
    val completedDate: String = "",
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val parsedCategory: TaskCategory
        get() = TaskCategory.fromWire(category)
}

@Keep
@IgnoreExtraProperties
data class RewardGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val spaceId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val targetDays: Long = 14L,
    val budgetUsd: Double = 60.0,
    val coverPresetKey: String = "dinner",
    val localImageUri: String? = null,
    val redeemed: Boolean = false,
    @ServerTimestamp val redeemedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SavedInsight(
    @DocumentId val id: String = "",
    val userId: String = "",
    val spaceId: String = "",
    val quote: String = "",
    val author: String = "",
    val source: String = "",
    val reflectionNote: String = "",
    val bookmarked: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiLoadState<out T> {
    data object Idle : AiLoadState<Nothing>
    data object Loading : AiLoadState<Nothing>
    data class Success<T>(val data: T) : AiLoadState<T>
    data class Error(val message: String) : AiLoadState<Nothing>
}

data class AiPatternAnalysis(
    val patternTitle: String,
    val patternSummary: String,
    val recommendationText: String,
    val actionButtonLabel: String,
    val suggestedAnchorTitle: String,
    val suggestedAnchorSubtitle: String,
    val suggestedCategory: TaskCategory = TaskCategory.NON_NEGOTIABLE,
)

data class AiFreshInsight(
    val quote: String,
    val author: String,
    val source: String,
    val reflectionPrompt: String,
)

data class CopilotMessage(
    val id: String,
    val fromUser: Boolean,
    val text: String,
    val streaming: Boolean = false,
)

data class CuratedVideo(
    val id: String,
    val sourceTag: String,
    val categoryTag: String,
    val statsText: String,
    val duration: String,
    val title: String,
    val description: String,
    val creatorInitials: String,
    val creatorName: String,
    val coverKey: String,
    val externalUrl: String,
)

data class RecoveryCaseStudy(
    val id: String,
    val categoryTag: String,
    val dayCount: Int,
    val title: String,
    val summary: String,
    val beforeMetric: String,
    val afterMetric: String,
    val highlightNote: String?,
    val ctaLabel: String,
    val dailyScheduleSteps: List<String>,
    val keyTakeaway: String,
)

data class StarterTaskSuggestion(
    val title: String,
    val subtitle: String,
    val category: TaskCategory,
    val iconKey: String,
)

data class StarterRewardSuggestion(
    val title: String,
    val subtitle: String,
    val targetDays: Long,
    val budgetUsd: Double,
    val coverPresetKey: String,
)
