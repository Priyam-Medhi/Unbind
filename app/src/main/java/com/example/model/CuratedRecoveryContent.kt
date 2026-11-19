package com.example.model

object CuratedRecoveryContent {

    val quickStartArchetypes: List<HabitArchetype> = listOf(
        HabitArchetype.DOOMSCROLLING,
        HabitArchetype.NICOTINE,
        HabitArchetype.SUGAR,
        HabitArchetype.GAMING,
        HabitArchetype.SPENDING,
    )

    fun starterTasksFor(archetype: HabitArchetype): List<StarterTaskSuggestion> = when (archetype) {
        HabitArchetype.DOOMSCROLLING -> listOf(
            StarterTaskSuggestion("Morning 15-min sunlight walk", "Circadian anchor", TaskCategory.NON_NEGOTIABLE, "sun"),
            StarterTaskSuggestion("No phone in bedroom after 9 PM", "Restoration boundary", TaskCategory.NON_NEGOTIABLE, "moon"),
            StarterTaskSuggestion("Read 15 pages of a physical book", "Mindfulness reading", TaskCategory.TODO, "book"),
            StarterTaskSuggestion("Drink 2.5L structured water", "Vitality & energy", TaskCategory.TODO, "water"),
            StarterTaskSuggestion("Do not open social Explore feeds", "Zero algorithm feeding", TaskCategory.NOT_TO_DO, "block"),
            StarterTaskSuggestion("No phone during meals", "Present nourishment", TaskCategory.NOT_TO_DO, "block"),
        )
        HabitArchetype.NICOTINE -> listOf(
            StarterTaskSuggestion("3-minute physiological sigh on waking", "Vagal tone anchor", TaskCategory.NON_NEGOTIABLE, "sun"),
            StarterTaskSuggestion("Carry herbal tea or mint toothpick", "Oral reflex replacement", TaskCategory.NON_NEGOTIABLE, "water"),
            StarterTaskSuggestion("20-minute brisk outdoor walk", "Dopamine baseline lift", TaskCategory.TODO, "sun"),
            StarterTaskSuggestion("Log peak craving window tonight", "Pattern awareness", TaskCategory.TODO, "book"),
            StarterTaskSuggestion("Do not pair coffee with nicotine cues", "Trigger decoupling", TaskCategory.NOT_TO_DO, "block"),
            StarterTaskSuggestion("No stepping outside with smokers on breaks", "Environment shield", TaskCategory.NOT_TO_DO, "block"),
        )
        HabitArchetype.SUGAR -> listOf(
            StarterTaskSuggestion("Protein-first savory breakfast", "Glucose stability pillar", TaskCategory.NON_NEGOTIABLE, "sun"),
            StarterTaskSuggestion("Kitchen closed after 8 PM", "Intermittent restoration", TaskCategory.NON_NEGOTIABLE, "moon"),
            StarterTaskSuggestion("Prep fresh berries & herbal tea", "Gentle afternoon swap", TaskCategory.TODO, "water"),
            StarterTaskSuggestion("10-minute post-dinner walk", "Insulin calm", TaskCategory.TODO, "sun"),
            StarterTaskSuggestion("No late-night pantry grazing", "Boundary against spikes", TaskCategory.NOT_TO_DO, "block"),
            StarterTaskSuggestion("No sweetened beverages before noon", "Palate recalibration", TaskCategory.NOT_TO_DO, "block"),
        )
        HabitArchetype.GAMING -> listOf(
            StarterTaskSuggestion("Dock controller outside bedroom at 9 PM", "Sleep sanctuary", TaskCategory.NON_NEGOTIABLE, "moon"),
            StarterTaskSuggestion("15-min analog hobby before screens", "Tactile presence", TaskCategory.NON_NEGOTIABLE, "book"),
            StarterTaskSuggestion("Evening walk or stretch session", "Nervous system unwind", TaskCategory.TODO, "sun"),
            StarterTaskSuggestion("Plan tomorrow's top creative focus", "Real-world progression", TaskCategory.TODO, "book"),
            StarterTaskSuggestion("No 'one more match' past curfew", "Stop-loss boundary", TaskCategory.NOT_TO_DO, "block"),
            StarterTaskSuggestion("No watching gaming streams in bed", "Cue elimination", TaskCategory.NOT_TO_DO, "block"),
        )
        HabitArchetype.SPENDING -> listOf(
            StarterTaskSuggestion("48-hour cooling-off list for wants", "Impulse buffer", TaskCategory.NON_NEGOTIABLE, "book"),
            StarterTaskSuggestion("Review daily savings balance", "Positive reinforcement", TaskCategory.NON_NEGOTIABLE, "sun"),
            StarterTaskSuggestion("Unsubscribe from 3 promo newsletters", "Trigger removal", TaskCategory.TODO, "block"),
            StarterTaskSuggestion("Cook one nourishing meal at home", "Intentional abundance", TaskCategory.TODO, "water"),
            StarterTaskSuggestion("No one-click checkout browsing after 9 PM", "Fatigue shield", TaskCategory.NOT_TO_DO, "moon"),
            StarterTaskSuggestion("No retail therapy when stressed", "Emotional boundary", TaskCategory.NOT_TO_DO, "block"),
        )
        HabitArchetype.CUSTOM -> listOf(
            StarterTaskSuggestion("Morning 10-min quiet breathwork", "Grounded start", TaskCategory.NON_NEGOTIABLE, "sun"),
            StarterTaskSuggestion("Evening wind-down ritual at 9 PM", "Restoration boundary", TaskCategory.NON_NEGOTIABLE, "moon"),
            StarterTaskSuggestion("Write 3 lines of daily reflection", "Clarity practice", TaskCategory.TODO, "book"),
            StarterTaskSuggestion("Hydrate with 2L clean water", "Vitality & energy", TaskCategory.TODO, "water"),
            StarterTaskSuggestion("Avoid autopilot cue triggers", "Mindful boundary", TaskCategory.NOT_TO_DO, "block"),
        )
    }

    val starterRewardSuggestions: List<StarterRewardSuggestion> = listOf(
        StarterRewardSuggestion(
            title = "Specialty Artisan Dinner",
            subtitle = "Milestone: 14 Days Clean • Earned fully from habit savings",
            targetDays = 14L,
            budgetUsd = 60.0,
            coverPresetKey = "dinner",
        ),
        StarterRewardSuggestion(
            title = "Weekend Mountain Cabin Getaway",
            subtitle = "Milestone: 30 Days Clean • Unplugged nature summit",
            targetDays = 30L,
            budgetUsd = 220.0,
            coverPresetKey = "cabin",
        ),
        StarterRewardSuggestion(
            title = "Mindful Morning Ritual Set",
            subtitle = "Milestone: 7 Days Clean • Ceramic pour-over & linen journal",
            targetDays = 7L,
            budgetUsd = 45.0,
            coverPresetKey = "horizon",
        ),
        StarterRewardSuggestion(
            title = "Bedside Reading Sanctuary",
            subtitle = "Milestone: 21 Days Clean • Warm amber lamp & hardcover stack",
            targetDays = 21L,
            budgetUsd = 95.0,
            coverPresetKey = "anchor",
        ),
    )

    fun defaultAnchorQuoteFor(archetype: HabitArchetype): AiFreshInsight = when (archetype) {
        HabitArchetype.DOOMSCROLLING -> AiFreshInsight(
            quote = "You do not rise to the level of your goals. You fall to the level of your systems.",
            author = "James Clear",
            source = "Atomic Habits",
            reflectionPrompt = "Which small environmental friction can you place before your phone tonight?",
        )
        HabitArchetype.NICOTINE -> AiFreshInsight(
            quote = "Between stimulus and response there is a space. In that space is our power to choose our response.",
            author = "Viktor E. Frankl",
            source = "Man's Search for Meaning",
            reflectionPrompt = "What happens to a craving when you observe it for 90 calm seconds without acting?",
        )
        HabitArchetype.SUGAR -> AiFreshInsight(
            quote = "Calmness of mind is one of the beautiful jewels of wisdom. It is the result of long and patient effort in self-control.",
            author = "James Allen",
            source = "As a Man Thinketh",
            reflectionPrompt = "How does steady nourishment change your evening energy compared to quick spikes?",
        )
        HabitArchetype.GAMING -> AiFreshInsight(
            quote = "How we spend our days is, of course, how we spend our lives.",
            author = "Annie Dillard",
            source = "The Writing Life",
            reflectionPrompt = "What tangible real-world craft deserves the focus you are reclaiming tonight?",
        )
        HabitArchetype.SPENDING -> AiFreshInsight(
            quote = "Wealth consists not in having great possessions, but in having few wants.",
            author = "Epictetus",
            source = "Discourses",
            reflectionPrompt = "What feeling were you hoping to buy, and how can you gift yourself calm for free?",
        )
        HabitArchetype.CUSTOM -> AiFreshInsight(
            quote = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            author = "Will Durant",
            source = "The Story of Philosophy",
            reflectionPrompt = "Which quiet daily pillar makes staying free feel effortless today?",
        )
    }

    fun curatedVideosFor(spaceTitle: String): List<CuratedVideo> = listOf(
        CuratedVideo(
            id = "vid_dopamine_reset",
            sourceTag = "YouTube Rec",
            categoryTag = "Behavioral Science",
            statsText = "1.4M views • 98% resonance",
            duration = "18:24",
            title = "How Dopamine Fasting Rewires Your Brain",
            description = "Deconstruct the synaptic baseline shift triggered by compulsive loops and learn actionable friction protocols for $spaceTitle.",
            creatorInitials = "AH",
            creatorName = "Neuroscience & Baseline Lab",
            coverKey = "anchor",
            externalUrl = "https://www.youtube.com/results?search_query=controlling+your+dopamine+for+motivation+focus+and+satisfaction+huberman",
        ),
        CuratedVideo(
            id = "vid_digital_minimalism",
            sourceTag = "Deep Work Guild",
            categoryTag = "Digital Minimalism",
            statsText = "890K views • High impact",
            duration = "12:10",
            title = "Breaking the Endless Compulsion Loop",
            description = "Structured behavioral breakdown: how modern cues engineer urge cycles, and practical architectural swaps that restore calm focus.",
            creatorInitials = "CN",
            creatorName = "Deep Focus Cadence",
            coverKey = "horizon",
            externalUrl = "https://www.youtube.com/results?search_query=cal+newport+digital+minimalism+breaking+compulsive+habits",
        ),
    )

    val recoveryCaseStudies: List<RecoveryCaseStudy> = listOf(
        RecoveryCaseStudy(
            id = "study_marcus_60",
            categoryTag = "DOPAMINE RESET",
            dayCount = 60,
            title = "From 7 Hours Screen Time to 45 Minutes: Marcus's 60-Day Protocol",
            summary = "Marcus eliminated midnight phone check-ins by relocating his device to a physical foyer dock and substituting a warm bedside lamp with a hardcover book.",
            beforeMetric = "7h 14m / day avg",
            afterMetric = "42m / day avg",
            highlightNote = null,
            ctaLabel = "Read Full Study & Daily Schedule",
            dailyScheduleSteps = listOf(
                "07:00 AM — Wake with analog alarm; 15 minutes of outdoor morning sunlight before touching any screen.",
                "09:00 AM — Batch messages in two dedicated 20-minute windows; keep notifications muted all morning.",
                "08:45 PM — Digital sunset: phone placed on foyer charger outside the bedroom.",
                "09:00 PM — Warm bedside lamp + 15 pages of print fiction to wind down cortisol naturally.",
            ),
            keyTakeaway = "Physical distance outperformed willpower every single night. Once the phone left the nightstand, evening urges dropped 70% within ten days.",
        ),
        RecoveryCaseStudy(
            id = "study_elena_42",
            categoryTag = "HABIT SUBSTITUTION",
            dayCount = 42,
            title = "Elena's Replacement Rituals: Tactile Focus vs. Blue Light",
            summary = "Tackling post-dinner craving surges with tactile acoustic engagement. Keeping hands occupied on physical rituals made impulsive loops completely friction-heavy.",
            beforeMetric = "2h 30m nightly loops",
            afterMetric = "30m restorative craft",
            highlightNote = "30 min evening practice replaced 2.5h compulsive loops",
            ctaLabel = "Read Elena's Step-by-Step Transition",
            dailyScheduleSteps = listOf(
                "06:30 PM — Immediately after dinner, brew caffeine-free chamomile tea as a transition cue.",
                "07:00 PM — 30 minutes of tactile evening practice (music, sketching, or journaling) with device in another room.",
                "08:00 PM — Check off daily Growth Pillars and reflect on urge peak intensity (0–10).",
            ),
            keyTakeaway = "Never leave an empty void where a compulsive habit lived—pair the exact trigger hour with a tactile, soothing replacement.",
        ),
        RecoveryCaseStudy(
            id = "study_devon_30",
            categoryTag = "CRAVING SURFING",
            dayCount = 30,
            title = "Devon's 90-Second Wave: Unbinding Stress Cravings at Work",
            summary = "Instead of fighting afternoon cravings with sheer force, Devon used two physiological sighs and a 5-minute sunlit courtyard walk to let the wave crest and dissolve.",
            beforeMetric = "14 urges / day",
            afterMetric = "1–2 mild urges / day",
            highlightNote = "90-second physiological sigh cut afternoon relapses to zero",
            ctaLabel = "Read Devon's 90-Second Wave Protocol",
            dailyScheduleSteps = listOf(
                "Trigger Alert — Notice jaw tension or restlessness; name it out loud as 'just a dopamine dip.'",
                "00:00–01:30 — Two double-inhale physiological sighs followed by slow 6-second exhales.",
                "01:30–06:30 — Stand up, drink cold water, and walk outside without bringing a phone.",
            ),
            keyTakeaway = "Most acute cravings peak and subside within 3 minutes if you shift your physiology rather than debating the urge.",
        ),
    )

    fun copilotQuickPromptsFor(archetype: HabitArchetype): List<String> = when (archetype) {
        HabitArchetype.DOOMSCROLLING -> listOf("Auto-swiping loop", "2-min urge reset", "Bedtime impulse")
        HabitArchetype.NICOTINE -> listOf("Sudden craving wave", "2-min breath reset", "Post-meal trigger")
        HabitArchetype.SUGAR -> listOf("Late-night sweet urge", "Stress snacking reset", "Afternoon crash")
        HabitArchetype.GAMING -> listOf("One more match urge", "Late-night unwind", "Boredom restless loop")
        HabitArchetype.SPENDING -> listOf("Checkout impulse", "48-hr pause talk", "Stress browsing reset")
        HabitArchetype.CUSTOM -> listOf("Acute urge wave", "2-min nervous reset", "Evening impulse")
    }
}
