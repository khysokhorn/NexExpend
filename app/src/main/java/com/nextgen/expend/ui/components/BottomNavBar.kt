package com.nextgen.expend.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class NavTab { HOME, HISTORY, ADD, INSIGHTS }

@Composable
fun BottomNavBar(
    selected: NavTab,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onAdd: () -> Unit,
    onInsights: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = scheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = selected == NavTab.HOME,
            onClick  = onHome,
            icon     = {
                Icon(
                    if (selected == NavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(26.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = scheme.primary,
                unselectedIconColor = scheme.onSurfaceVariant,
                indicatorColor      = scheme.surfaceVariant,
            )
        )

        NavigationBarItem(
            selected = selected == NavTab.HISTORY,
            onClick  = onHistory,
            icon     = {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = "History",
                    modifier = Modifier.size(26.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = scheme.primary,
                unselectedIconColor = scheme.onSurfaceVariant,
                indicatorColor      = scheme.surfaceVariant,
            )
        )

        NavigationBarItem(
            selected = selected == NavTab.ADD,
            onClick  = onAdd,
            icon     = {
                Icon(
                    if (selected == NavTab.ADD) Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(32.dp),
                    tint = scheme.primary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = scheme.primary,
                unselectedIconColor = scheme.primary,
                indicatorColor      = scheme.surfaceVariant,
            )
        )

        NavigationBarItem(
            selected = selected == NavTab.INSIGHTS,
            onClick  = onInsights,
            icon     = {
                Icon(
                    Icons.Outlined.Insights,
                    contentDescription = "Insights",
                    modifier = Modifier.size(26.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = scheme.primary,
                unselectedIconColor = scheme.onSurfaceVariant,
                indicatorColor      = scheme.surfaceVariant,
            )
        )
    }
}
