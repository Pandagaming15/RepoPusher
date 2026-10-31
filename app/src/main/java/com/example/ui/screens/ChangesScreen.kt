package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.GitFileStatus
import com.example.core.models.LocalRepository
import com.example.ui.theme.*

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
    val staged = changes.filter { it.isStaged }
    val unstaged = changes.filter { !it.isStaged }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GitHubDarkBackground)
            .testTag("changes_screen")
    ) {
        // Top Toolbar: Branch & Remote Actions
        Surface(
            color = GitHubDarkSurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Branch Selector chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GitHubDarkSurfaceVariant)
                            .clickable { onOpenBranchManager() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeRepo?.currentBranch ?: "main",
                            color = GitHubBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(16.dp))
                    }

                    // Push & Pull
                    Row {
                        OutlinedButton(
                            onClick = onPull,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubTextPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("pull_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = GitHubPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pull", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onPush,
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("push_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stage toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Staged: ${staged.size} | Unstaged: ${unstaged.size}",
                        fontSize = 12.sp,
                        color = GitHubTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        TextButton(
                            onClick = onStageAll,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Stage All", color = GitHubGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (staged.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = onUnstageAll,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Unstage All", color = GitHubRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // List of Changes
        if (changes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Working Tree Clean",
                        fontWeight = FontWeight.Bold,
                        color = GitHubTextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "No uncommitted changes in current repository.",
                        fontSize = 12.sp,
                        color = GitHubTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        OutlinedButton(onClick = onOpenCommitHistory) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Commit History")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onOpenCreatePr) {
                            Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New PR")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Staged Section
                if (staged.isNotEmpty()) {
                    item {
                        Text(
                            text = "STAGED CHANGES (${staged.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GitHubGreen
                        )
                    }
                    items(staged) { item ->
                        ChangeItemCard(
                            item = item,
                            isStaged = true,
                            onToggleStage = { onUnstageFile(item.relativePath) },
                            onShowDiff = { onShowDiff(item.relativePath) }
                        )
                    }
                }

                // Unstaged Section
                if (unstaged.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CHANGES (${unstaged.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GitHubOrange
                        )
                    }
                    items(unstaged) { item ->
                        ChangeItemCard(
                            item = item,
                            isStaged = false,
                            onToggleStage = { onStageFile(item.relativePath) },
                            onShowDiff = { onShowDiff(item.relativePath) }
                        )
                    }
                }
            }
        }

        // Commit Box at bottom
        Surface(
            color = GitHubDarkSurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("Commit summary (e.g. Update user model and API calls)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("commit_message_input"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = onOpenCreatePr, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.CallMerge, contentDescription = "Create PR", tint = GitHubBlue)
                        }
                        IconButton(onClick = onOpenCreateRelease, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Label, contentDescription = "Create Release", tint = GitHubPurple)
                        }
                    }

                    Button(
                        onClick = {
                            if (commitMessage.isNotBlank()) {
                                onCommit(commitMessage)
                                commitMessage = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        enabled = commitMessage.isNotBlank() && !isLoading,
                        modifier = Modifier.testTag("commit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Commit to ${activeRepo?.currentBranch ?: "main"}")
                        }
                    }
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
    val (badgeText, badgeColor) = when (item.changeType) {
        GitFileStatus.ChangeType.MODIFIED -> Pair("M", GitHubOrange)
        GitFileStatus.ChangeType.ADDED -> Pair("A", GitHubGreen)
        GitFileStatus.ChangeType.UNTRACKED -> Pair("U", GitHubBlue)
        GitFileStatus.ChangeType.DELETED -> Pair("D", GitHubRed)
        GitFileStatus.ChangeType.CONFLICT -> Pair("!", GitHubRed)
        else -> Pair("?", GitHubTextSecondary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("change_item_${item.fileName}"),
        colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowDiff() }
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.fileName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = GitHubTextPrimary
                    )
                    Text(
                        text = item.relativePath,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = GitHubTextMuted,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShowDiff, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Difference, contentDescription = "Diff", tint = GitHubTextSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onToggleStage, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isStaged) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                        contentDescription = if (isStaged) "Unstage" else "Stage",
                        tint = if (isStaged) GitHubRed else GitHubGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
