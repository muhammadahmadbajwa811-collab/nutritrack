package com.nutritrack.app.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NtNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    DIARY("diary", "Diary", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    ACTIVITY("activity", "Activity", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun),
    PROGRESS("progress", "Progress", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp)
}

@Composable
fun NtBottomBar(
    currentRoute: String,
    onNavigateToTab: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Bar Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NtBottomNavItem(
                    tab = NtNavTab.HOME,
                    isSelected = currentRoute == NtNavTab.HOME.route,
                    onClick = { onNavigateToTab(NtNavTab.HOME.route) }
                )

                // Diary
                NtBottomNavItem(
                    tab = NtNavTab.DIARY,
                    isSelected = currentRoute == NtNavTab.DIARY.route,
                    onClick = { onNavigateToTab(NtNavTab.DIARY.route) }
                )

                // Center Spacer for FAB notch
                Box(modifier = Modifier.size(60.dp))

                // Activity
                NtBottomNavItem(
                    tab = NtNavTab.ACTIVITY,
                    isSelected = currentRoute == NtNavTab.ACTIVITY.route,
                    onClick = { onNavigateToTab(NtNavTab.ACTIVITY.route) }
                )

                // Progress
                NtBottomNavItem(
                    tab = NtNavTab.PROGRESS,
                    isSelected = currentRoute == NtNavTab.PROGRESS.route,
                    onClick = { onNavigateToTab(NtNavTab.PROGRESS.route) }
                )
            }
        }

        // Center 60dp Red Floating Action Button
        FloatingActionButton(
            onClick = onAddClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            modifier = Modifier
                .size(60.dp)
                .offset(y = (-14).dp)
                .semantics { contentDescription = "Add Food or Log Action" }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun NtBottomNavItem(
    tab: NtNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_item_tint"
    )
    val pillBackground = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(pillBackground)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = tint,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
