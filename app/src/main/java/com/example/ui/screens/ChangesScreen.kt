package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.models.GitFileStatus
import com.example.core.models.LocalRepository
import com.example.ui.theme.GitHubDarkCard
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@Composable
fun ChangesScreen(
    activeRepo: LocalRepository?,
    changes: List<GitFileStatus>,
    isLoading: Boolean,
    onStageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageFile: (String) -> Unit,
    onUnstageAll: () -> Unit,
    onCommit: (String) -> Unit,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onShowDiff: (String) -> Unit,
    onOpenBranchManager: () -> Unit,
    onOpenCommitHistory: () -> Unit,
    onOpenCreatePr: () -> Unit,
    onOpenCreateRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }

    val stagedChanges = remember(changes) { changes.filter { it.isStaged } }
    val unstagedChanges = remember(changes) { changes.filter { !it.isStaged } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("changes_screen")
    ) {
        if (activeRepo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOff,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No repository selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Please open a repository from the Repositories tab to view and commit changes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }

        // Active Repository Control Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            activeRepo.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenBranchManager,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("btn_branch_manager")
                            ) {
                                Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    activeRepo.currentBranch ?: "master",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onPull,
                            enabled = !isLoading,
                            modifier = Modifier.testTag("btn_pull")
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Pull")
                        }
                        IconButton(
                            onClick = onPush,
                            enabled = !isLoading,
                            modifier = Modifier.testTag("btn_push")
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Push", tint = GitHubGreen)
                        }
                        IconButton(
                            onClick = onOpenCommitHistory,
                            modifier = Modifier.testTag("btn_history")
                        ) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                        IconButton(
                            onClick = onOpenCreatePr,
                            modifier = Modifier.testTag("btn_create_pr")
                        ) {
                            Icon(Icons.Default.MergeType, contentDescription = "Create PR")
                        }
                        IconButton(
                            onClick = onOpenCreateRelease,
                            modifier = Modifier.testTag("btn_create_release")
                        ) {
                            Icon(Icons.Default.Label, contentDescription = "Release")
                        }
                    }
                }
            }
        }

        // List of Changes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Staged Changes Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Staged Changes (${stagedChanges.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GitHubGreen
                    )
                    if (stagedChanges.isNotEmpty()) {
                        TextButton(
                            onClick = onUnstageAll,
                            modifier = Modifier.testTag("btn_unstage_all")
                        ) {
                            Text("Unstage All", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (stagedChanges.isEmpty()) {
                item {
                    Text(
                        "No staged changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(stagedChanges, key = { "staged_${it.relativePath}" }) { change ->
                    ChangeItemCard(
                        item = change,
                        isStaged = true,
                        onToggleStage = { onUnstageFile(change.relativePath) },
                        onShowDiff = { onShowDiff(change.relativePath) }
                    )
                }
            }

            // Unstaged Changes Header
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Changes (${unstagedChanges.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (unstagedChanges.isNotEmpty()) {
                        TextButton(
                            onClick = onStageAll,
                            modifier = Modifier.testTag("btn_stage_all")
                        ) {
                            Text("Stage All", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (unstagedChanges.isEmpty()) {
                item {
                    Text(
                        if (stagedChanges.isEmpty()) "Working tree clean. No modified files."
                        else "No unstaged changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(unstagedChanges, key = { "unstaged_${it.relativePath}" }) { change ->
                    ChangeItemCard(
                        item = change,
                        isStaged = false,
                        onToggleStage = { onStageFile(change.relativePath) },
                        onShowDiff = { onShowDiff(change.relativePath) }
                    )
                }
            }
        }

        // Commit Box at Bottom
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("Commit message...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_commit_message"),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (commitMessage.isNotBlank()) {
                            onCommit(commitMessage.trim())
                            commitMessage = ""
                        }
                    },
                    enabled = !isLoading && commitMessage.isNotBlank() && stagedChanges.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_commit")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (stagedChanges.isNotEmpty()) "Commit (${stagedChanges.size} staged)"
                        else "Stage files to commit"
                    )
                }
            }
        }
    }
}

@Composable
fun ChangeItemCard(
    item: GitFileStatus,
    isStaged: Boolean,
    onToggleStage: () -> Unit,
    onShowDiff: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("change_item_${item.relativePath}"),
        colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (statusLetter, statusColor) = when (item.changeType) {
                GitFileStatus.ChangeType.MODIFIED -> "M" to Color(0xFFFFB300)
                GitFileStatus.ChangeType.UNTRACKED -> "?" to GitHubGreen
                GitFileStatus.ChangeType.ADDED -> "A" to GitHubGreen
                GitFileStatus.ChangeType.DELETED -> "D" to MaterialTheme.colorScheme.error
                GitFileStatus.ChangeType.MISSING -> "!" to MaterialTheme.colorScheme.error
                GitFileStatus.ChangeType.CONFLICT -> "C" to MaterialTheme.colorScheme.error
                else -> "U" to MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        statusLetter,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.relativePath,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.changeType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onShowDiff,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Difference,
                    contentDescription = "Show Diff",
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onToggleStage,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    if (isStaged) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                    contentDescription = if (isStaged) "Unstage" else "Stage",
                    tint = if (isStaged) MaterialTheme.colorScheme.error else GitHubGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
