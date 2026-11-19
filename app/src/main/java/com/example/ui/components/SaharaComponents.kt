package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.CheckInLog
import com.example.model.CheckInOutcome
import com.example.model.HabitSpace
import com.example.model.WorkspaceTab
import com.example.ui.theme.LocalSaharaColors
import com.google.firebase.auth.FirebaseUser

@DrawableRes
fun coverDrawableForKey(key: String): Int = when (key.lowercase()) {
    "anchor", "bedside", "reading" -> R.drawable.img_mindful_anchor_1795074064198
    "horizon", "desk", "guitar", "morning" -> R.drawable.img_calm_horizon_1795074061827
    "cabin", "retreat", "mountain" -> R.drawable.img_reward_cabin_1795074062242
    else -> R.drawable.img_reward_dinner_1795074062033
}

fun iconForKey(key: String): ImageVector = when (key.lowercase()) {
    "phone" -> Icons.Filled.PhonelinkErase
    "air" -> Icons.Filled.Air
    "bakery" -> Icons.Filled.BakeryDining
    "gamepad" -> Icons.Filled.SportsEsports
    "bag" -> Icons.Filled.ShoppingBag
    "sun" -> Icons.Filled.WbSunny
    "moon" -> Icons.Filled.NightsStay
    "book" -> Icons.Filled.MenuBook
    "water" -> Icons.Filled.WaterDrop
    "block" -> Icons.Filled.Block
    else -> Icons.Filled.Spa
}

@Composable
fun UnbindLotusMedallion(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Spa,
            contentDescription = "Unbind lotus emblem",
            tint = iconColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun SaharaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = LocalSaharaColors.current.cardBorder,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        content()
    }
}

@Composable
fun SaharaPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    leadingDotColor: Color? = null,
    leadingIcon: ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(leadingDotColor),
                )
            }
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun WorkspaceTopBar(
    activeTab: WorkspaceTab,
    activeSpace: HabitSpace,
    user: FirebaseUser?,
    onOpenSpaceSwitcher: () -> Unit,
    onOpenAccountSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            UnbindLotusMedallion(size = 36.dp, iconSize = 18.dp)
            Column {
                Text(
                    text = "UNBIND",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    text = activeTab.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onOpenSpaceSwitcher)
                    .testTag("space_switcher_pill"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, LocalSaharaColors.current.cardBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Text(
                        text = activeSpace.shortDisplayTitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = Icons.Filled.UnfoldMore,
                        contentDescription = "Switch Focus Space",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            UserAvatarButton(
                user = user,
                onClick = onOpenAccountSheet,
            )
        }
    }
}

@Composable
fun UserAvatarButton(
    user: FirebaseUser?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoUrl = user?.photoUrl?.toString()
    val initials = (user?.displayName ?: user?.email ?: "U")
        .trim()
        .take(1)
        .uppercase()

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            .clickable(onClick = onClick)
            .testTag("account_avatar_button"),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Account profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun UnbindBottomNavBar(
    selectedTab: WorkspaceTab,
    onSelectTab: (WorkspaceTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LocalSaharaColors.current.cardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WorkspaceTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val icon = tabIcon(tab, selected)
                val tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.2.sp,
                        ),
                        color = tint,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun UnbindNavigationRail(
    selectedTab: WorkspaceTab,
    onSelectTab: (WorkspaceTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        WorkspaceTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = tabIcon(tab, selected),
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

private fun tabIcon(tab: WorkspaceTab, selected: Boolean): ImageVector = when (tab) {
    WorkspaceTab.TRACKER -> if (selected) Icons.Filled.TrendingUp else Icons.Outlined.TrendingUp
    WorkspaceTab.GROWTH -> if (selected) Icons.Filled.Spa else Icons.Outlined.Spa
    WorkspaceTab.REWARDS -> if (selected) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents
    WorkspaceTab.INSIGHTS -> if (selected) Icons.Filled.Psychology else Icons.Outlined.Psychology
}

@Composable
fun ProgressJourneyChart(
    checkIns: List<CheckInLog>,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val gridColor = LocalSaharaColors.current.cardBorder

    val recentLogs = checkIns.takeLast(14)
    val points: List<Pair<String, Float>> = recentLogs.mapIndexed { idx, log ->
        val label = if (idx == recentLogs.lastIndex) "D${recentLogs.size}" else log.dateKey.takeLast(2)
        val score = when (log.parsedOutcome) {
            CheckInOutcome.CLEAN -> (0.72f + (0.26f * ((10L - log.urgeLevel).coerceIn(0L, 10L) / 10f)))
            CheckInOutcome.MILD_URGE -> 0.54f
            CheckInOutcome.RELAPSE -> 0.12f
            CheckInOutcome.NONE -> 0.5f
        }.coerceIn(0.08f, 0.96f)
        label to score
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .height(124.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Free", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Mild", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Relapse", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(128.dp)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
                val w = size.width
                val h = size.height

                // Horizontal subtle guidelines
                listOf(0.08f, 0.50f, 0.92f).forEach { frac ->
                    val y = h * frac
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                if (points.isEmpty()) return@Canvas

                val stepX = if (points.size <= 1) 0f else w / (points.size - 1).coerceAtLeast(1)
                val coords = points.mapIndexed { idx, (_, value) ->
                    val x = if (points.size == 1) w * 0.5f else idx * stepX
                    val y = h * (1f - value)
                    Offset(x, y)
                }

                // Dashed urge baseline smoothly trailing beneath
                val baselineCoords = coords.mapIndexed { idx, pt ->
                    val lift = (idx.toFloat() / coords.size.coerceAtLeast(1)) * 0.10f
                    Offset(pt.x, (pt.y + h * (0.18f - lift)).coerceAtMost(h * 0.94f))
                }

                if (coords.size >= 2) {
                    val curvePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.lastIndex) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(curvePath)
                        lineTo(coords.last().x, h)
                        lineTo(coords.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.22f),
                                primaryColor.copy(alpha = 0.01f),
                            ),
                        ),
                    )

                    val baselinePath = Path().apply {
                        moveTo(baselineCoords.first().x, baselineCoords.first().y)
                        for (i in 0 until baselineCoords.lastIndex) {
                            val p0 = baselineCoords[i]
                            val p1 = baselineCoords[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = baselinePath,
                        color = baselineColor,
                        style = Stroke(
                            width = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                            cap = StrokeCap.Round,
                        ),
                    )

                    drawPath(
                        path = curvePath,
                        color = primaryColor,
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round),
                    )
                }

                coords.forEachIndexed { idx, pt ->
                    val isEndpoint = idx == 0 || idx == coords.lastIndex
                    drawCircle(
                        color = primaryColor,
                        radius = if (isEndpoint) 5.dp.toPx() else 3.2.dp.toPx(),
                        center = pt,
                    )
                    if (isEndpoint) {
                        drawCircle(
                            color = surfaceColor,
                            radius = 2.4.dp.toPx(),
                            center = pt,
                        )
                    }
                }
            }
        }

        if (points.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 54.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val displayLabels = if (points.size <= 7) {
                    points.map { it.first }
                } else {
                    listOf(
                        points.first().first,
                        points[points.size / 3].first,
                        points[(points.size * 2) / 3].first,
                        points.last().first,
                    )
                }
                displayLabels.forEachIndexed { idx, label ->
                    val isLast = idx == displayLabels.lastIndex
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isLast) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceSwitcherBottomSheet(
    spaces: List<HabitSpace>,
    activeSpaceId: String?,
    onSelectSpace: (String) -> Unit,
    onGoToSpacesHub: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = "SOVEREIGN FOCUS SPACES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Switch Sanctuary",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))

            spaces.forEach { space ->
                val isSelected = space.id == activeSpaceId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            onSelectSpace(space.id)
                            onDismiss()
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else LocalSaharaColors.current.cardBorder,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        UnbindLotusMedallion(
                            size = 38.dp,
                            iconSize = 18.dp,
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = space.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Day ${space.currentStreakDays} Clean • $${space.totalSavedUsd.toInt()} saved",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Active Space",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = LocalSaharaColors.current.cardBorder,
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onDismiss()
                        onGoToSpacesHub()
                    }
                    .testTag("manage_spaces_hub_button"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Open Focus Spaces Hub / Add New Branch",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBottomSheet(
    user: FirebaseUser?,
    onGoToSpacesHub: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UserAvatarButton(user = user, onClick = {})
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Unbind Member",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = user?.email ?: "Signed in with Google",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SaharaPillBadge(
                text = "Cloud Synced • Private Per-User Isolation",
                leadingIcon = Icons.Filled.AutoAwesome,
            )

            HorizontalDivider(color = LocalSaharaColors.current.cardBorder)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onGoToSpacesHub()
                    },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "View All Focus Spaces",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onSignOut()
                    }
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
