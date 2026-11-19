package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CuratedRecoveryContent
import com.example.model.GrowthTask
import com.example.model.HabitSpace
import com.example.model.TaskCategory
import com.example.model.UiState
import com.example.ui.components.SaharaCard
import com.example.ui.components.SaharaPillBadge
import com.example.ui.components.UnbindLotusMedallion
import com.example.ui.components.iconForKey
import com.example.ui.theme.LocalSaharaColors

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GrowthTabScreen(
    space: HabitSpace,
    todayDateKey: String,
    tasksState: UiState<List<GrowthTask>>,
    onAddTask: (title: String, subtitle: String, category: TaskCategory, iconKey: String) -> Unit,
    onToggleTask: (GrowthTask) -> Unit,
    onReclassifyTask: (taskId: String, newCategory: TaskCategory) -> Unit,
    onDeleteTask: (taskId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tasks = (tasksState as? UiState.Success)?.data.orEmpty()
    val completedCount = tasks.count { it.completedDate == todayDateKey }
    val totalCount = tasks.size
    val percent = if (totalCount == 0) 0 else ((completedCount * 100) / totalCount)
    val flowLabel = when {
        totalCount == 0 -> "Ready"
        percent >= 80 -> "$percent%\nGrounded"
        percent >= 50 -> "$percent%\nSolid"
        else -> "$percent%\nBuilding"
    }

    var addTargetCategory by remember { mutableStateOf<TaskCategory?>(null) }
    var taskToMove by remember { mutableStateOf<GrowthTask?>(null) }

    val starterSuggestions = remember(space.archetype) {
        CuratedRecoveryContent.starterTasksFor(space.parsedArchetype)
    }
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 1. Daily Architecture Hero Card
        item {
            SaharaCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                cornerRadius = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.growth_eyebrow),
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.3.sp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.growth_title),
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.growth_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        UnbindLotusMedallion(size = 56.dp, iconSize = 26.dp)
                    }

                    // Habit Flow Progress Strip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, LocalSaharaColors.current.cardBorder),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        append("Habit Flow: ")
                                        withStyle(
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        ) {
                                            append("$completedCount / $totalCount Anchors")
                                        }
                                        append(" protected today")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                            ) {
                                Text(
                                    text = flowLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.2.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Three Sovereign Sections (Non-negotiables, To-do List, Not-to-do List)
        TaskCategory.entries.forEach { category ->
            val categoryTasks = tasks.filter { it.parsedCategory == category }
            val dotColor = when (category) {
                TaskCategory.NON_NEGOTIABLE -> colorScheme.primary
                TaskCategory.TODO -> colorScheme.secondary
                TaskCategory.NOT_TO_DO -> colorScheme.tertiary
            }
            val badgeBg = when (category) {
                TaskCategory.NON_NEGOTIABLE -> colorScheme.primaryContainer
                TaskCategory.TODO -> colorScheme.surfaceContainer
                TaskCategory.NOT_TO_DO -> colorScheme.tertiaryContainer
            }
            val badgeText = dotColor

            item(key = "header_${category.wireValue}") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            SaharaPillBadge(
                                text = category.badge,
                                containerColor = badgeBg,
                                contentColor = badgeText,
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .clickable { addTargetCategory = category }
                                .testTag("add_task_${category.wireValue.lowercase()}"),
                            shape = CircleShape,
                            color = badgeBg,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add to ${category.title}",
                                    tint = badgeText,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = stringResource(R.string.growth_add_task),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.2.sp,
                                    ),
                                    color = badgeText,
                                )
                            }
                        }
                    }

                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (categoryTasks.isEmpty()) {
                item(key = "empty_${category.wireValue}") {
                    val suggestionsForCategory = starterSuggestions.filter { it.category == category }
                    SaharaCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        cornerRadius = 18.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "No ${category.badge.lowercase()} added yet — tap a starter anchor or create your own:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                suggestionsForCategory.forEach { suggestion ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                onAddTask(
                                                    suggestion.title,
                                                    suggestion.subtitle,
                                                    suggestion.category,
                                                    suggestion.iconKey,
                                                )
                                            },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                                        border = BorderStroke(1.dp, LocalSaharaColors.current.cardBorder),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = null,
                                                tint = dotColor,
                                                modifier = Modifier.size(14.dp),
                                            )
                                            Text(
                                                text = suggestion.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(categoryTasks, key = { it.id }) { task ->
                    GrowthTaskCard(
                        task = task,
                        isCompletedToday = task.completedDate == todayDateKey,
                        accentColor = dotColor,
                        onToggle = { onToggleTask(task) },
                        onOpenMoveSheet = { taskToMove = task },
                    )
                }
            }
        }

        // 3. The Rule of Three Anchors editorial card
        item {
            SaharaCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                cornerRadius = 22.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "The Rule of Three Anchors",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("If a regular To-do becomes vital to your sanity, elevate it to a ")
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                                    append("Non-negotiable")
                                }
                                append(". If a habit proves slippery, frame it as a strict boundary in ")
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)) {
                                    append("Not-to-do")
                                }
                                append(".")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    addTargetCategory?.let { category ->
        AddGrowthTaskDialog(
            category = category,
            onDismiss = { addTargetCategory = null },
            onConfirm = { title, subtitle, iconKey ->
                addTargetCategory = null
                onAddTask(title, subtitle, category, iconKey)
            },
        )
    }

    taskToMove?.let { task ->
        ModalBottomSheet(
            onDismissRequest = { taskToMove = null },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "RECLASSIFY ANCHOR",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TaskCategory.entries.forEach { target ->
                    val isCurrent = task.parsedCategory == target
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (!isCurrent) {
                                    onReclassifyTask(task.id, target)
                                }
                                taskToMove = null
                            }
                            .testTag("move_task_to_${target.wireValue.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${target.title} (${target.badge})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = target.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Current list",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = LocalSaharaColors.current.cardBorder)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onDeleteTask(task.id)
                            taskToMove = null
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete anchor",
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        Text(
                            text = "Remove Anchor",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowthTaskCard(
    task: GrowthTask,
    isCompletedToday: Boolean,
    accentColor: Color,
    onToggle: () -> Unit,
    onOpenMoveSheet: () -> Unit,
) {
    SaharaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("growth_task_card_${task.id}"),
        cornerRadius = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Soft rounded checkbox matching Screen 3
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCompletedToday) accentColor else MaterialTheme.colorScheme.surfaceContainer,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isCompletedToday) accentColor else LocalSaharaColors.current.cardBorder,
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompletedToday) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = if (isCompletedToday) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                if (task.subtitle.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = iconForKey(task.iconKey),
                            contentDescription = null,
                            tint = if (task.parsedCategory == TaskCategory.NOT_TO_DO) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = task.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.parsedCategory == TaskCategory.NOT_TO_DO) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenMoveSheet,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .testTag("reclassify_task_${task.id}"),
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = "Move item between lists",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun AddGrowthTaskDialog(
    category: TaskCategory,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subtitle: String, iconKey: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    val defaultIcon = when (category) {
        TaskCategory.NON_NEGOTIABLE -> "sun"
        TaskCategory.TODO -> "book"
        TaskCategory.NOT_TO_DO -> "block"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Add to ${category.title}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Ritual or boundary") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input"),
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { if (it.length <= 120) subtitle = it },
                    label = { Text("Short anchor tag (optional)") },
                    placeholder = { Text("e.g., Restoration boundary") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = title.trim()
                    if (clean.isNotEmpty()) {
                        onConfirm(clean, subtitle.trim().ifEmpty { category.badge }, defaultIcon)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_add_task_button"),
            ) {
                Text("Add Anchor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun categoryAccentColor(category: TaskCategory): Color = when (category) {
    TaskCategory.NON_NEGOTIABLE -> MaterialTheme.colorScheme.primary
    TaskCategory.TODO -> MaterialTheme.colorScheme.secondary
    TaskCategory.NOT_TO_DO -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun categoryBadgeContainerColor(category: TaskCategory): Color = when (category) {
    TaskCategory.NON_NEGOTIABLE -> MaterialTheme.colorScheme.primaryContainer
    TaskCategory.TODO -> MaterialTheme.colorScheme.surfaceContainer
    TaskCategory.NOT_TO_DO -> MaterialTheme.colorScheme.tertiaryContainer
}
