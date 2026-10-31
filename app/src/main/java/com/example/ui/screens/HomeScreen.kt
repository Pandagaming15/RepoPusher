package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("home_hero_banner"),
                colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_git_banner_1793452534370),
                        contentDescription = "RepoStudio Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(Color(0xEE0D1117), Color(0x770D1117), Color.Transparent)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RepoStudio",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Manage, commit, and publish Git repositories natively.",
                            fontSize = 11.sp,
                            color = GitHubTextPrimary
                        )
                    }
                }
            }
        }

        // Active Repository Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
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
                            text = "Choose or create a repository from the Repositories tab to begin.",
                            fontSize = 12.sp,
                            color = GitHubTextSecondary
                        )
                    }

                    if (activeRepo != null && !activeRepo.isGitInitialized) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onInitGit,
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Initialize Git in this Project")
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
                            text = "Scan files, create repo, commit and push in 1 easy step",
                            color = Color(0xDDFFFFFF),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Publish",
                        tint = Color.White
                    )
                }
            }
        }

        // Quick Action Grid
        item {
            Text(
                text = "Quick Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GitHubTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    onClick = { onNavigateTab(NavigationTab.CHANGES) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(Icons.Default.Difference, contentDescription = null, tint = GitHubGreen)
                            if (changesCount > 0) {
                                Surface(shape = RoundedCornerShape(8.dp), color = GitHubGreen) {
                                    Text("$changesCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("View Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GitHubTextPrimary)
                        Text("Stage & Commit", fontSize = 11.sp, color = GitHubTextMuted)
                    }
                }

                Card(
                    onClick = onPush,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = GitHubBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Push to Remote", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GitHubTextPrimary)
                        Text("Send local commits", fontSize = 11.sp, color = GitHubTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    onClick = onPull,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = GitHubPurple)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pull Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GitHubTextPrimary)
                        Text("Fetch & integrate", fontSize = 11.sp, color = GitHubTextMuted)
                    }
                }

                Card(
                    onClick = onOpenCommitHistory,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = GitHubOrange)
                            if (commitCount > 0) {
                                Surface(shape = RoundedCornerShape(8.dp), color = GitHubDarkSurfaceVariant) {
                                    Text("$commitCount", color = GitHubTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Commit History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GitHubTextPrimary)
                        Text("View commit log", fontSize = 11.sp, color = GitHubTextMuted)
                    }
                }
            }
        }

        // Repository Storage Stats Card
        item {
            Card(
                onClick = onOpenFiles,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Project Files", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GitHubTextPrimary)
                            Text("${projectFiles.size} files in local working tree", fontSize = 12.sp, color = GitHubTextMuted)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Browse", fontSize = 12.sp, color = GitHubGreen, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GitHubGreen)
                    }
                }
            }
        }

        // Recent Activity Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Operations",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitHubTextPrimary
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    color = GitHubBlue,
                    modifier = Modifier.clickable { onNavigateTab(NavigationTab.ACTIVITY) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (recentLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface)
                ) {
                    Text(
                        text = "No recent operations logged.",
                        fontSize = 12.sp,
                        color = GitHubTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recentLogs.take(4).forEach { log ->
                        val badgeColor = when (log.level) {
                            OperationLog.Level.SUCCESS -> GitHubGreen
                            OperationLog.Level.ERROR -> GitHubRed
                            OperationLog.Level.WARNING -> GitHubOrange
                            else -> GitHubBlue
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface)
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
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                                        Box(modifier = Modifier.size(8.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(log.operation, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GitHubTextPrimary)
                                        Text(log.details, fontSize = 11.sp, color = GitHubTextMuted, maxLines = 1)
                                    }
                                }
                                Text(log.formattedTime, fontSize = 10.sp, color = GitHubTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
