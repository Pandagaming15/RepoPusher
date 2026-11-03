package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.LocalRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    activeRepo: LocalRepository?,
    localRepos: List<LocalRepository>,
    onSelectRepo: (LocalRepository) -> Unit,
    onRefresh: () -> Unit,
    onNewProject: () -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    var expandedRepoSelector by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.testTag("app_top_bar"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedRepoSelector = true }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("top_bar_repo_selector")
            ) {
                Icon(
                    imageVector = Icons.Default.Source,
                    contentDescription = null,
                    tint = GitHubGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "RepoStudio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GitHubTextPrimary
                    )
                    Text(
                        text = activeRepo?.name ?: "No repo selected",
                        fontSize = 11.sp,
                        color = GitHubTextSecondary,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Switch Repository",
                    tint = GitHubTextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                DropdownMenu(
                    expanded = expandedRepoSelector,
                    onDismissRequest = { expandedRepoSelector = false },
                    modifier = Modifier.testTag("top_bar_repo_dropdown")
                ) {
                    if (localRepos.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No repositories found", color = GitHubTextMuted) },
                            onClick = { expandedRepoSelector = false }
                        )
                    } else {
                        localRepos.forEach { repo ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (repo.id == activeRepo?.id) GitHubGreen else GitHubTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = repo.name,
                                            color = if (repo.id == activeRepo?.id) GitHubGreen else GitHubTextPrimary,
                                            fontWeight = if (repo.id == activeRepo?.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectRepo(repo)
                                    expandedRepoSelector = false
                                }
                            )
                        }
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("top_bar_refresh_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GitHubTextPrimary)
            }

            Box {
                IconButton(
                    onClick = { expandedMenu = true },
                    modifier = Modifier.testTag("top_bar_more_button")
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More actions", tint = GitHubTextPrimary)
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.testTag("top_bar_more_dropdown")
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Device Folder") },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubBlue) },
                        onClick = {
                            expandedMenu = false
                            onOpenFolder()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New Starter Project") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = GitHubGreen) },
                        onClick = {
                            expandedMenu = false
                            onNewProject()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GitHubDarkSurface
        )
    )
}
