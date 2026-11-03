package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.models.LocalRepository
import com.example.core.models.OperationLog
import com.example.core.models.ProjectFile
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab

@Composable
fun HomeScreen(
    activeRepo: LocalRepository?,
    projectFiles: List<ProjectFile>,
    changesCount: Int,
    commitCount: Int,
    recentLogs: List<OperationLog>,
    onQuickPublish: () -> Unit,
    onInitGit: () -> Unit,
    onNavigateTab: (NavigationTab) -> Unit,
    onOpenCommitHistory: () -> Unit,
    onOpenBranchManager: () -> Unit,
    onOpenFiles: () -> Unit,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onOpenFolder: () -> Unit = {},
    onSyncToFolder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GitHubDarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("home_hero_banner"),
                colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF0F2B1D), Color(0xFF161B22))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GitHubGreenDark
                        ) {
                            Text(
                                text = "GIT CLIENT FOR ANDROID",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RepoStudio",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GitHubTextPrimary
                        )
                        Text(
                            text = "Lightweight GitHub Desktop for Android",
                            fontSize = 12.sp,
                            color = GitHubTextSecondary
                        )
                    }
                }
            }
        }

        // Active Repository Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_active_repo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = GitHubGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeRepo?.name ?: "No Repository Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GitHubTextPrimary
                            )
                        }
                        if (activeRepo?.isGitInitialized == true) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x222EA043),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubGreen))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Git Ready", color = GitHubGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeRepo != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GitHubDarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { onOpenBranchManager() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(activeRepo.currentBranch, color = GitHubBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (activeRepo.remoteUrl.isNotBlank()) {
                                Text(
                                    text = activeRepo.remoteUrl,
                                    fontSize = 11.sp,
                                    color = GitHubTextMuted,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Text(
                                    text = "Remote: not configured",
                                    fontSize = 11.sp,
                                    color = GitHubTextMuted
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No repository currently open. Select any project folder from your device storage to view files, commit, and push.",
                            fontSize = 12.sp,
                            color = GitHubTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenFolder,
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_open_device_folder_button")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Device Folder", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (activeRepo != null && !activeRepo.isGitInitialized) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onInitGit,
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                            modifier = Modifier.fillMaxWidth().testTag("home_init_git_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Initialize Git in this Project")
                        }
                    }

                    if (activeRepo != null && activeRepo.safTreeUri.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onSyncToFolder,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubGreen),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_sync_folder_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Changes to Device Folder", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Quick Publish Hero Card
        item {
            Card(
                onClick = onQuickPublish,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_quick_publish_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GitHubGreenDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Publish to GitHub",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Stage, commit, create repo and push in one guided flow",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Repository Operations",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GitHubTextSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(NavigationTab.CHANGES) }
                        .testTag("home_quick_action_changes"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Commit, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GitHubTextPrimary)
                        Text("$changesCount pending", fontSize = 11.sp, color = GitHubTextSecondary)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenFiles() }
                        .testTag("home_quick_action_files"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Project Files", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GitHubTextPrimary)
                        Text("${projectFiles.size} files", fontSize = 11.sp, color = GitHubTextSecondary)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenCommitHistory() }
                        .testTag("home_quick_action_history"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, tint = GitHubPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GitHubTextPrimary)
                        Text("$commitCount commits", fontSize = 11.sp, color = GitHubTextSecondary)
                    }
                }
            }
        }

        // Push / Pull Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPush,
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    modifier = Modifier.weight(1f).testTag("home_push_button")
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Push", color = GitHubTextPrimary)
                }

                Button(
                    onClick = onPull,
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    modifier = Modifier.weight(1f).testTag("home_pull_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pull", color = GitHubTextPrimary)
                }
            }
        }

        // Recent Operations Section
        item {
            Text(
                text = "Recent Git Activity",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GitHubTextSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (recentLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                    )
                ) {
                    Text(
                        text = "No recent operations logged.",
                        fontSize = 12.sp,
                        color = GitHubTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        recentLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val color = when (log.level) {
                                    OperationLog.Level.SUCCESS -> GitHubGreen
                                    OperationLog.Level.ERROR -> GitHubRed
                                    OperationLog.Level.WARNING -> GitHubOrange
                                    else -> GitHubBlue
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = color.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = log.operation,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = log.details,
                                    fontSize = 11.sp,
                                    color = GitHubTextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.formattedTime,
                                    fontSize = 10.sp,
                                    color = GitHubTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
