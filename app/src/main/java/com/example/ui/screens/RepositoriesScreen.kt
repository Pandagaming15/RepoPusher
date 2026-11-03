package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.models.GitHubRepo
import com.example.core.models.LocalRepository
import com.example.ui.theme.GitHubDarkCard
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen
import com.example.viewmodel.RepositoryPathsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesScreen(
    activeRepo: LocalRepository?,
    localRepos: List<LocalRepository>,
    githubRepos: List<GitHubRepo>,
    isAuthenticated: Boolean,
    onSelectRepo: (LocalRepository) -> Unit,
    onNewProject: () -> Unit,
    onPickFolder: () -> Unit,
    onRefreshGitHubRepos: () -> Unit,
    onDeleteRepo: (LocalRepository) -> Unit,
    onCloneRepo: (String) -> Unit,
    onCreateGitHubRepo: () -> Unit,
    onViewReadme: (owner: String, repo: String) -> Unit,
    onNavigateSettings: () -> Unit,
    onOpenRepoUrl: (String) -> Unit = {},
    repoPathsViewModel: RepositoryPathsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("repositories_screen")
    ) {
        // Tab selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GitHubDarkSurface,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Local (${localRepos.size})") },
                modifier = Modifier.testTag("tab_local_repos")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("GitHub (${if (isAuthenticated) githubRepos.size else 0})") },
                modifier = Modifier.testTag("tab_github_repos")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Saved Paths") },
                modifier = Modifier.testTag("tab_saved_paths")
            )
        }

        when (selectedTab) {
            0 -> {
                // Local repositories tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPickFolder,
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_open_folder")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open Folder", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onNewProject,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_new_project")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New Project", maxLines = 1)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_local_repos"),
                        placeholder = { Text("Search local repositories...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    val filteredRepos = remember(localRepos, searchQuery) {
                        if (searchQuery.isBlank()) localRepos
                        else localRepos.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.path.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredRepos.isEmpty()) {
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (searchQuery.isNotBlank()) "No repositories match your search"
                                    else "No local repositories opened yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Tap 'Open Folder' to select any project on your device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredRepos, key = { it.path }) { repo ->
                                val isActive = activeRepo?.path == repo.path
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("repo_card_${repo.name}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) GitHubDarkSurface else GitHubDarkCard
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isActive) ButtonDefaults.outlinedButtonBorder else null
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = if (isActive) GitHubGreen else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        repo.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (isActive) {
                                                        Spacer(Modifier.width(8.dp))
                                                        Surface(
                                                            color = GitHubGreen.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                "ACTIVE",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = GitHubGreen,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    repo.path,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CallSplit,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    repo.currentBranch ?: "No branch",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { onDeleteRepo(repo) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                if (!isActive) {
                                                    Button(
                                                        onClick = { onSelectRepo(repo) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text("Open", style = MaterialTheme.typography.labelMedium)
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
            1 -> {
                // GitHub repositories tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (!isAuthenticated) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Sign in to GitHub",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Connect your GitHub account or add a Personal Access Token in Settings to view and clone your repositories.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = onNavigateSettings,
                                    colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                                    modifier = Modifier.testTag("btn_go_to_settings")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Open Settings")
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onCreateGitHubRepo,
                                colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_create_gh_repo")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("New Remote", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = onRefreshGitHubRepos,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_refresh_gh_repos")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Refresh", maxLines = 1)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_gh_repos"),
                            placeholder = { Text("Search GitHub repositories...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true
                        )

                        Spacer(Modifier.height(12.dp))

                        val filteredGhRepos = remember(githubRepos, searchQuery) {
                            if (searchQuery.isBlank()) githubRepos
                            else githubRepos.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        (it.description?.contains(searchQuery, ignoreCase = true) == true)
                            }
                        }

                        if (filteredGhRepos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No remote repositories found.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredGhRepos, key = { it.id }) { repo ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("gh_repo_card_${repo.name}"),
                                        colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (repo.isPrivate) Icons.Default.Lock else Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = if (repo.isPrivate) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    repo.fullName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        if (repo.isPrivate) "Private" else "Public",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (!repo.description.isNullOrBlank()) {
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    repo.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (!repo.language.isNullOrBlank()) {
                                                        Text(
                                                            repo.language,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = GitHubGreen
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                                                        Spacer(Modifier.width(2.dp))
                                                        Text("${repo.stargazersCount}", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Spacer(Modifier.width(2.dp))
                                                        Text("${repo.forksCount}", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                Row {
                                                    IconButton(
                                                        onClick = {
                                                            val owner = repo.fullName.substringBefore('/')
                                                            val rName = repo.fullName.substringAfter('/')
                                                            onViewReadme(owner, rName)
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Description, contentDescription = "README", modifier = Modifier.size(18.dp))
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            val url = repo.htmlUrl ?: "https://github.com/${repo.fullName}"
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                            context.startActivity(intent)
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser", modifier = Modifier.size(18.dp))
                                                    }

                                                    Button(
                                                        onClick = { onCloneRepo(repo.cloneUrl) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text("Clone", style = MaterialTheme.typography.labelMedium)
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
            2 -> {
                // Saved Repository Paths tab (Room database)
                RepositoryPathsScreen(
                    viewModel = repoPathsViewModel,
                    onNavigateBack = {}
                )
            }
        }
    }
}
