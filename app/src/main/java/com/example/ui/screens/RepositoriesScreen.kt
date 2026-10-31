package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.GitHubRepo
import com.example.core.models.LocalRepository
import com.example.ui.theme.*

@Composable
fun RepositoriesScreen(
    activeRepo: LocalRepository?,
    localRepos: List<LocalRepository>,
    githubRepos: List<GitHubRepo>,
    isAuthenticated: Boolean,
    onSelectRepo: (LocalRepository) -> Unit,
    onNewProject: () -> Unit,
    onImportSaf: () -> Unit,
    onCloneRepo: (initialUrl: String) -> Unit,
    onCreateGitHubRepo: () -> Unit = {},
    onViewReadme: (owner: String, repo: String) -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Local, 1 = GitHub
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GitHubDarkBackground)
            .testTag("repositories_screen")
    ) {
        // Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GitHubDarkSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) GitHubGreen else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Local (${localRepos.size})",
                            color = if (selectedTab == 0) Color.White else GitHubTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) GitHubGreen else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GitHub (${githubRepos.size})",
                            color = if (selectedTab == 1) Color.White else GitHubTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Action Toolbar
        if (selectedTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNewProject,
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 12.sp, color = GitHubTextPrimary)
                }

                Button(
                    onClick = onImportSaf,
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_saf_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import SAF", fontSize = 12.sp, color = GitHubTextPrimary)
                }

                Button(
                    onClick = { onCloneRepo("") },
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clone_repo_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = GitHubPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone", fontSize = 12.sp, color = GitHubTextPrimary)
                }
            }
        } else {
            // Action Toolbar for GitHub Tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCreateGitHubRepo,
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_github_repo_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New GitHub Repo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onCloneRepo("") },
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clone_github_repo_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = GitHubPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clone URL", fontSize = 12.sp, color = GitHubTextPrimary)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (selectedTab == 0) "Filter local repositories..." else "Search GitHub repositories...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GitHubTextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GitHubGreen,
                unfocusedBorderColor = GitHubDarkBorder
            )
        )

        // List Content
        if (selectedTab == 0) {
            val filteredLocal = localRepos.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (filteredLocal.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOff, contentDescription = null, tint = GitHubTextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No local projects match query", color = GitHubTextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLocal) { repo ->
                        val isCurrentActive = activeRepo?.path == repo.path
                        Card(
                            onClick = { onSelectRepo(repo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repo_card_${repo.name}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentActive) GitHubDarkSurfaceVariant else GitHubDarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isCurrentActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubGreen))
                            else CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isCurrentActive) GitHubGreen else GitHubBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = repo.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = GitHubTextPrimary
                                        )
                                    }
                                    if (isCurrentActive) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = GitHubGreenDark
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GitHubDarkBackground
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(repo.currentBranch, fontSize = 11.sp, color = GitHubBlue)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = repo.path,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = GitHubTextMuted,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (repo.remoteUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Remote: " + repo.remoteUrl,
                                        fontSize = 10.sp,
                                        color = GitHubTextMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // GitHub Tab
            if (!isAuthenticated) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "GitHub Authentication Required",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GitHubTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sign in with GitHub or provide a Personal Access Token in Settings to list and clone your repositories.",
                                fontSize = 12.sp,
                                color = GitHubTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen)
                            ) {
                                Text("Go to Settings")
                            }
                        }
                    }
                }
            } else {
                val filteredGithub = githubRepos.filter { it.name.contains(searchQuery, ignoreCase = true) || it.fullName.contains(searchQuery, ignoreCase = true) }
                if (filteredGithub.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No GitHub repositories found.", color = GitHubTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredGithub) { repo ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                                shape = RoundedCornerShape(12.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = repo.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = GitHubBlue
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (repo.isPrivate) Color(0x33F85149) else Color(0x332EA043)
                                        ) {
                                            Text(
                                                text = if (repo.isPrivate) "Private" else "Public",
                                                color = if (repo.isPrivate) GitHubRed else GitHubGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (repo.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = repo.description,
                                            fontSize = 12.sp,
                                            color = GitHubTextSecondary,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (repo.language.isNotBlank()) {
                                                Text(repo.language, fontSize = 11.sp, color = GitHubOrange, fontWeight = FontWeight.SemiBold)
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                            Icon(Icons.Default.Star, contentDescription = null, tint = GitHubOrange, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${repo.stargazersCount}", fontSize = 11.sp, color = GitHubTextMuted)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.CallSplit, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${repo.forksCount}", fontSize = 11.sp, color = GitHubTextMuted)
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    val owner = repo.fullName.substringBefore("/")
                                                    onViewReadme(owner, repo.name)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Description, contentDescription = "README", tint = GitHubTextSecondary, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = { onCloneRepo(repo.cloneUrl) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = "Clone Locally", tint = GitHubGreen, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    try {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.htmlUrl)))
                                                    } catch (ignored: Exception) {}
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.OpenInBrowser, contentDescription = "Open Web", tint = GitHubBlue, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
