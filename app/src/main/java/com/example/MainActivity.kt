package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.MindfulAiRepository
import com.example.data.UnbindFirestoreRepository
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.firebase.signOutFromGoogle
import com.example.model.AiLoadState
import com.example.model.CheckInOutcome
import com.example.model.UiState
import com.example.model.WorkspaceTab
import com.example.ui.components.AccountBottomSheet
import com.example.ui.components.SpaceSwitcherBottomSheet
import com.example.ui.components.UnbindBottomNavBar
import com.example.ui.components.UnbindNavigationRail
import com.example.ui.components.WorkspaceTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FocusSpacesScreen
import com.example.ui.screens.GrowthTabScreen
import com.example.ui.screens.InsightsTabScreen
import com.example.ui.screens.RewardsTabScreen
import com.example.ui.screens.TrackerTabScreen
import com.example.ui.theme.UnbindTheme
import com.example.viewmodel.UnbindViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        // STEP 2: Enable full edge-to-edge drawing
        enableEdgeToEdge()

        setContent {
            UnbindTheme {
                UnbindAppRoot()
            }
        }
    }
}

@Composable
private fun UnbindAppRoot() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        // Unauthenticated gate: feature ViewModels and Firestore listeners are NOT composed here
        AuthScreen(onAuthSuccess = {})
    } else {
        // Keyed by uid so sign-out or account switch tears down listeners and ViewModel cleanly
        key(user.uid) {
            AuthenticatedUnbindHost(currentUser = user)
        }
    }
}

@Composable
private fun AuthenticatedUnbindHost(currentUser: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: UnbindViewModel = viewModel(
        key = currentUser.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                UnbindViewModel(
                    repository = UnbindFirestoreRepository(db),
                    aiRepository = MindfulAiRepository(),
                    currentUserId = currentUser.uid,
                )
            }
        },
    )

    val spacesState by viewModel.spacesState.collectAsStateWithLifecycle()
    val selectedSpaceId by viewModel.selectedSpaceId.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val checkInsState by viewModel.activeCheckInsState.collectAsStateWithLifecycle()
    val tasksState by viewModel.activeTasksState.collectAsStateWithLifecycle()
    val rewardsState by viewModel.activeRewardsState.collectAsStateWithLifecycle()
    val savedInsightsState by viewModel.activeSavedInsightsState.collectAsStateWithLifecycle()
    val patternStates by viewModel.patternStates.collectAsStateWithLifecycle()
    val insightStates by viewModel.insightStates.collectAsStateWithLifecycle()
    val copilotMessagesMap by viewModel.copilotMessages.collectAsStateWithLifecycle()
    val isCopilotStreaming by viewModel.isCopilotStreaming.collectAsStateWithLifecycle()
    val copilotError by viewModel.copilotError.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSpaceSwitcher by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }

    LaunchedEffect(statusBanner) {
        val message = statusBanner
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusBanner()
        }
    }

    LaunchedEffect(actionError) {
        val err = actionError
        if (!err.isNullOrBlank()) {
            snackbarHostState.showSnackbar(err)
            viewModel.clearActionError()
        }
    }

    val spaces = (spacesState as? UiState.Success)?.data.orEmpty()
    val activeSpace = spaces.firstOrNull { it.id == selectedSpaceId }

    // Back button inside a Focus Space returns to the Focus Spaces Sanctuary Hub
    BackHandler(enabled = activeSpace != null) {
        viewModel.returnToSpacesHub()
    }

    if (activeSpace == null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            FocusSpacesScreen(
                spacesState = spacesState,
                user = currentUser,
                isBusy = isBusy,
                onContinueSpace = { spaceId -> viewModel.openSpace(spaceId) },
                onCreateSpace = { title, archetype, cost, hours ->
                    viewModel.createSpace(title, archetype, cost, hours)
                },
                onOpenAccountSheet = { showAccountSheet = true },
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val useNavRail = maxWidth >= 680.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    WorkspaceTopBar(
                        activeTab = selectedTab,
                        activeSpace = activeSpace,
                        user = currentUser,
                        onOpenSpaceSwitcher = { showSpaceSwitcher = true },
                        onOpenAccountSheet = { showAccountSheet = true },
                    )
                },
                bottomBar = {
                    if (!useNavRail) {
                        UnbindBottomNavBar(
                            selectedTab = selectedTab,
                            onSelectTab = { viewModel.selectTab(it) },
                        )
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (useNavRail) {
                        UnbindNavigationRail(
                            selectedTab = selectedTab,
                            onSelectTab = { viewModel.selectTab(it) },
                            modifier = Modifier.fillMaxHeight(),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 640.dp)
                                .fillMaxSize(),
                        ) {
                            when (selectedTab) {
                                WorkspaceTab.TRACKER -> TrackerTabScreen(
                                    space = activeSpace,
                                    todayDateKey = viewModel.todayDateKey,
                                    checkInsState = checkInsState,
                                    patternState = patternStates[activeSpace.id] ?: AiLoadState.Idle,
                                    isBusy = isBusy,
                                    onCheckInClean = {
                                        viewModel.logDailyCheckIn(
                                            space = activeSpace,
                                            outcome = CheckInOutcome.CLEAN,
                                            urgeLevel = 2L,
                                        )
                                    },
                                    onCheckInRelapse = { urge, window, note ->
                                        viewModel.logDailyCheckIn(
                                            space = activeSpace,
                                            outcome = CheckInOutcome.RELAPSE,
                                            urgeLevel = urge,
                                            peakUrgeWindow = window,
                                            reflectionNote = note,
                                        )
                                    },
                                    onRunPatternAnalysis = { logs ->
                                        viewModel.runPatternAnalysis(activeSpace, logs)
                                    },
                                    onAddRecommendedAnchor = { title, subtitle, category ->
                                        viewModel.addTask(
                                            spaceId = activeSpace.id,
                                            title = title,
                                            subtitle = subtitle,
                                            category = category,
                                            iconKey = "moon",
                                        )
                                    },
                                )

                                WorkspaceTab.GROWTH -> GrowthTabScreen(
                                    space = activeSpace,
                                    todayDateKey = viewModel.todayDateKey,
                                    tasksState = tasksState,
                                    onAddTask = { title, subtitle, category, iconKey ->
                                        viewModel.addTask(activeSpace.id, title, subtitle, category, iconKey)
                                    },
                                    onToggleTask = { task ->
                                        viewModel.toggleTaskCompletion(activeSpace.id, task)
                                    },
                                    onReclassifyTask = { taskId, newCategory ->
                                        viewModel.reclassifyTask(activeSpace.id, taskId, newCategory)
                                    },
                                    onDeleteTask = { taskId ->
                                        viewModel.deleteTask(activeSpace.id, taskId)
                                    },
                                )

                                WorkspaceTab.REWARDS -> RewardsTabScreen(
                                    space = activeSpace,
                                    rewardsState = rewardsState,
                                    onAddReward = { title, subtitle, days, budget, coverKey, localUri ->
                                        viewModel.addReward(
                                            spaceId = activeSpace.id,
                                            title = title,
                                            subtitle = subtitle,
                                            targetDays = days,
                                            budgetUsd = budget,
                                            coverPresetKey = coverKey,
                                            localImageUri = localUri,
                                        )
                                    },
                                    onRedeemReward = { reward ->
                                        viewModel.redeemReward(activeSpace, reward)
                                    },
                                    onUpdateDailyValuation = { cost, hours ->
                                        viewModel.updateSpaceValuation(activeSpace.id, cost, hours)
                                    },
                                )

                                WorkspaceTab.INSIGHTS -> InsightsTabScreen(
                                    space = activeSpace,
                                    activeInsight = viewModel.currentInsightFor(activeSpace),
                                    insightState = insightStates[activeSpace.id] ?: AiLoadState.Idle,
                                    savedInsightsState = savedInsightsState,
                                    copilotMessages = copilotMessagesMap[activeSpace.id].orEmpty(),
                                    isCopilotStreaming = isCopilotStreaming,
                                    copilotError = copilotError,
                                    onRefreshFreshInsights = { viewModel.refreshFreshInsight(activeSpace) },
                                    onBookmarkInsight = { insight, note ->
                                        viewModel.bookmarkInsight(activeSpace.id, insight, note)
                                    },
                                    onRemoveSavedInsight = { id ->
                                        viewModel.removeSavedInsight(activeSpace.id, id)
                                    },
                                    onSendCopilotPrompt = { prompt ->
                                        viewModel.sendCopilotPrompt(activeSpace, prompt)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSpaceSwitcher) {
        SpaceSwitcherBottomSheet(
            spaces = spaces,
            activeSpaceId = selectedSpaceId,
            onSelectSpace = { viewModel.openSpace(it, selectedTab) },
            onGoToSpacesHub = { viewModel.returnToSpacesHub() },
            onDismiss = { showSpaceSwitcher = false },
        )
    }

    if (showAccountSheet) {
        AccountBottomSheet(
            user = currentUser,
            onGoToSpacesHub = { viewModel.returnToSpacesHub() },
            onSignOut = {
                signOutFromGoogle(
                    credentialManager = credentialManager,
                    scope = scope,
                    onBeforeSignOut = { viewModel.returnToSpacesHub() },
                )
            },
            onDismiss = { showAccountSheet = false },
        )
    }
}
