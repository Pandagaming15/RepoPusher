package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
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
        modifier = modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = GitHubDarkSurface,
        tonalElevation = 6.dp
    ) {
        // HOME
        NavigationBarItem(
            selected = currentTab == NavigationTab.HOME,
            onClick = { onTabSelected(NavigationTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                indicatorColor = GitHubDarkSurfaceVariant,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // REPOSITORIES
        NavigationBarItem(
            selected = currentTab == NavigationTab.REPOSITORIES,
            onClick = { onTabSelected(NavigationTab.REPOSITORIES) },
            icon = { Icon(Icons.Default.FolderCopy, contentDescription = "Repositories") },
            label = { Text("Repos", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                indicatorColor = GitHubDarkSurfaceVariant,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_repositories")
        )

        // CHANGES
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
                    Icon(Icons.Default.Difference, contentDescription = "Changes")
                }
            },
            label = { Text("Changes", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                indicatorColor = GitHubDarkSurfaceVariant,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_changes")
        )

        // ACTIVITY
        NavigationBarItem(
            selected = currentTab == NavigationTab.ACTIVITY,
            onClick = { onTabSelected(NavigationTab.ACTIVITY) },
            icon = { Icon(Icons.Default.Terminal, contentDescription = "Activity") },
            label = { Text("Activity", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                indicatorColor = GitHubDarkSurfaceVariant,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_activity")
        )

        // SETTINGS
        NavigationBarItem(
            selected = currentTab == NavigationTab.SETTINGS,
            onClick = { onTabSelected(NavigationTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GitHubGreen,
                selectedTextColor = GitHubGreen,
                indicatorColor = GitHubDarkSurfaceVariant,
                unselectedIconColor = GitHubTextSecondary,
                unselectedTextColor = GitHubTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}
