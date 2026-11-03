package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab

@Composable
fun AppBottomNav(
    currentTab: NavigationTab,
    changesCount: Int,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_nav"),
        containerColor = GitHubDarkSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.HOME,
            onClick = { onTabSelected(NavigationTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary,
                indicatorColor = GitHubDarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.REPOSITORIES,
            onClick = { onTabSelected(NavigationTab.REPOSITORIES) },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Repositories") },
            label = { Text("Repositories", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary,
                indicatorColor = GitHubDarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_repositories")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.CHANGES,
            onClick = { onTabSelected(NavigationTab.CHANGES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (changesCount > 0) {
                            Badge(
                                containerColor = GitHubGreen,
                                contentColor = Color.White
                            ) {
                                Text("$changesCount")
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Commit, contentDescription = "Changes")
                }
            },
            label = { Text("Changes", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary,
                indicatorColor = GitHubDarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_changes")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.ACTIVITY,
            onClick = { onTabSelected(NavigationTab.ACTIVITY) },
            icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
            label = { Text("Activity", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary,
                indicatorColor = GitHubDarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_activity")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SETTINGS,
            onClick = { onTabSelected(NavigationTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary,
                indicatorColor = GitHubDarkSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}
