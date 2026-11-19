package com.example.data

import android.util.Log
import com.example.model.AiFreshInsight
import com.example.model.AiPatternAnalysis
import com.example.model.CheckInLog
import com.example.model.CopilotMessage
import com.example.model.HabitSpace
import com.example.model.TaskCategory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "MindfulAiRepo"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val TIMEOUT_MS = 60_000L

class MindfulAiRepository {

    private val patternSchema = Schema.obj(
        properties = mapOf(
            "patternTitle" to Schema.string(description = "Short 2-4 word label like 'Evening Urge Window' or 'Baseline Stabilization'"),
            "patternSummary" to Schema.string(description = "1 concise sentence identifying the user's behavioral pattern or peak risk window"),
            "recommendationText" to Schema.string(description = "1-2 concise, warm, actionable sentences with a concrete physiological or environmental swap"),
            "actionButtonLabel" to Schema.string(description = "Short imperative CTA label up to 34 chars, e.g., 'Add 9:00 PM Digital Sunset'"),
            "suggestedAnchorTitle" to Schema.string(description = "Concrete daily ritual title up to 60 chars to add to Growth list"),
            "suggestedAnchorSubtitle" to Schema.string(description = "Short 2-4 word anchor tag like 'Restoration boundary'"),
            "suggestedCategory" to Schema.enumeration(
                values = listOf("NON_NEGOTIABLE", "TODO", "NOT_TO_DO"),
                description = "Which Daily Architecture bucket fits best",
            ),
        ),
    )

    private val freshInsightSchema = Schema.obj(
        properties = mapOf(
            "quote" to Schema.string(description = "A calm, insightful quote relevant to breaking compulsive habits and building mindful systems"),
            "author" to Schema.string(description = "Author of the quote"),
            "source" to Schema.string(description = "Book, essay, or behavioral philosophy source"),
            "reflectionPrompt" to Schema.string(description = "One gentle single-sentence reflection prompt tailored to the habit"),
        ),
    )

    private val patternModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = patternSchema
                temperature = 0.6f
            },
        )
    }

    private val freshInsightModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = freshInsightSchema
                temperature = 0.85f
            },
        )
    }

    private val copilotModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = content {
                text(
                    "You are the Mindful AI Copilot inside Unbind, a calm, warm, shame-free habit recovery sanctuary. " +
                        "When the user feels a compulsive reflex, craving, or withdrawal urge, respond with grounded, empathetic, " +
                        "science-backed physiological reset techniques (such as the physiological sigh, 90-second urge surfing, " +
                        "tactile substitution, or friction placement). Keep responses concise (3-5 crisp sentences or 3 short steps), " +
                        "warm, non-judgmental, and immediately actionable.",
                )
            },
        )
    }

    suspend fun analyzePatterns(
        space: HabitSpace,
        recentCheckIns: List<CheckInLog>,
    ): Result<AiPatternAnalysis> = try {
        val logSummary = if (recentCheckIns.isEmpty()) {
            "No daily check-ins logged yet; the user just opened this isolated sanctuary."
        } else {
            recentCheckIns.takeLast(14).joinToString("\n") { log ->
                "- ${log.dateKey}: outcome=${log.outcome}, urge=${log.urgeLevel}/10, window='${log.peakUrgeWindow}', note='${log.reflectionNote}'"
            }
        }

        val prompt = """
            Analyze this isolated habit recovery Focus Space and provide a compassionate, evidence-based pattern insight and one concrete anchor ritual.
            Habit Space: ${space.title} (archetype: ${space.archetype})
            Current clean streak: ${space.currentStreakDays} days (total clean: ${space.totalCleanDays} days)
            Recent check-ins:
            $logSummary
        """.trimIndent()

        val response = withTimeout(TIMEOUT_MS) {
            patternModel.generateContent(prompt)
        }
        val rawJson = response.text
            ?: throw IllegalStateException("Gemini returned an empty pattern analysis.")

        val json = JSONObject(rawJson)
        val analysis = AiPatternAnalysis(
            patternTitle = json.optString("patternTitle", "Pattern Detected"),
            patternSummary = json.getString("patternSummary"),
            recommendationText = json.getString("recommendationText"),
            actionButtonLabel = json.optString("actionButtonLabel", "Add Ritual to Pillars"),
            suggestedAnchorTitle = json.getString("suggestedAnchorTitle"),
            suggestedAnchorSubtitle = json.optString("suggestedAnchorSubtitle", "AI recommended anchor"),
            suggestedCategory = TaskCategory.fromWire(json.optString("suggestedCategory", "NON_NEGOTIABLE")),
        )
        Result.success(analysis)
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Pattern analysis timed out", e)
        Result.failure(IllegalStateException("AI Pattern Analysis timed out after 60 seconds. Please try again."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Pattern analysis failed", e)
        Result.failure(e)
    }

    suspend fun generateFreshInsight(space: HabitSpace): Result<AiFreshInsight> = try {
        val prompt = """
            Generate a fresh, deeply calming Daily Mindful Anchor quote and reflection prompt tailored for someone gently unbinding: "${space.title}".
            Current clean streak: ${space.currentStreakDays} days.
            Choose a real, well-known behavioral psychology, stoic, or mindfulness quote (not a cliché) and a grounded 1-sentence reflection question.
        """.trimIndent()

        val response = withTimeout(TIMEOUT_MS) {
            freshInsightModel.generateContent(prompt)
        }
        val rawJson = response.text
            ?: throw IllegalStateException("Gemini returned an empty insight.")
        val json = JSONObject(rawJson)
        Result.success(
            AiFreshInsight(
                quote = json.getString("quote"),
                author = json.getString("author"),
                source = json.optString("source", "Behavioral Mindfulness"),
                reflectionPrompt = json.optString("reflectionPrompt", "What tiny shift protects your peace tonight?"),
            ),
        )
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Fresh insight timed out", e)
        Result.failure(IllegalStateException("Fresh Insights timed out after 60 seconds. Please try again."))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Fresh insight generation failed", e)
        Result.failure(e)
    }

    fun streamCopilotReply(
        space: HabitSpace,
        history: List<CopilotMessage>,
        userMessage: String,
    ): Flow<String> = flow {
        val conversationContext = history.takeLast(6).joinToString("\n") { msg ->
            val speaker = if (msg.fromUser) "User" else "Copilot"
            "$speaker: ${msg.text}"
        }
        val prompt = """
            Active Focus Space: ${space.title} (Current clean streak: ${space.currentStreakDays} days).
            ${if (conversationContext.isNotBlank()) "Recent conversation:\n$conversationContext\n" else ""}
            User right now: $userMessage
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(TIMEOUT_MS) {
            copilotModel.generateContentStream(prompt).collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    accumulated.append(piece)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("Mindful AI Copilot returned an empty response.")
        }
    }
}
