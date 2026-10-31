package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onOpenFolder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_top_bar"),
        color = GitHubDarkSurface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Repo Selector / Title
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GitHubDarkSurfaceVariant)
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("repo_selector_dropdown"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Repository",
                        tint = GitHubGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeRepo?.name ?: "No Repo Selected",
                            color = GitHubTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activeRepo != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Branch",
                                    tint = GitHubBlue,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeRepo.currentBranch,
                                    color = GitHubTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Repository",
                        tint = GitHubTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(GitHubDarkSurface)
                        .widthIn(min = 220.dp)
                ) {
                    Text(
                        text = "SWITCH REPOSITORY",
                        color = GitHubTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    Divider(color = GitHubDarkBorder)

                    if (localRepos.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No local repositories yet", color = GitHubTextSecondary, fontSize = 13.sp) },
                            onClick = { }
                        )
                    } else {
                        localRepos.forEach { repo ->
                            val isSelected = activeRepo?.path == repo.path
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (repo.isGitInitialized) Icons.Default.CheckCircle else Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (isSelected) GitHubGreen else GitHubTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                text = {
                                    Column {
                                        Text(
                                            text = repo.name,
                                            color = if (isSelected) GitHubGreen else GitHubTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = repo.currentBranch,
                                            color = GitHubTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onSelectRepo(repo)
                                }
                            )
                        }
                    }

                    Divider(color = GitHubDarkBorder)
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(16.dp))
                        },
                        text = { Text("Open Folder (In-Place)...", color = GitHubGreen, fontSize = 13.sp) },
                        onClick = {
                            expanded = false
                            onOpenFolder()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(16.dp))
                        },
                        text = { Text("New Local Project...", color = GitHubBlue, fontSize = 13.sp) },
                        onClick = {
                            expanded = false
                            onNewProject()
                        }
                    )
                }
            }

            // Right Action: Refresh
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = GitHubTextPrimary
                    )
                }
            }
        }
    }
}
