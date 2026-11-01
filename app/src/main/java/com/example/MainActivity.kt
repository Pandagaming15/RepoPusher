package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.GitHubDarkBackground
import com.example.ui.theme.GitHubGreen
import com.example.ui.theme.GitHubRed
import com.example.ui.theme.RepoStudioTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    private var pendingOAuthCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleOAuthIntent(intent)

        setContent {
            RepoStudioTheme {
                val viewModel: MainViewModel = viewModel()
                val currentTab by viewModel.currentTab.collectAsState()
                val activeRepo by viewModel.activeRepo.collectAsState()
                val localRepos by viewModel.localRepos.collectAsState()
                val githubRepos by viewModel.githubRepos.collectAsState()
                val gitHubUser by viewModel.gitHubUser.collectAsState()
                val gitChanges by viewModel.gitChanges.collectAsState()
                val commitHistory by viewModel.commitHistory.collectAsState()
                val branches by viewModel.branches.collectAsState()
                val operationLogs by viewModel.operationLogs.collectAsState()
                val projectFiles by viewModel.projectFiles.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val statusMessage by viewModel.statusMessage.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val diffDialogData by viewModel.diffDialogData.collectAsState()
                val readmeDialogData by viewModel.readmeDialogData.collectAsState()

                // Dialog states
                var showQuickPublish by remember { mutableStateOf(false) }
                var showCloneDialog by remember { mutableStateOf(false) }
                var cloneInitialUrl by remember { mutableStateOf("") }
                var showNewProjectDialog by remember { mutableStateOf(false) }
                var showCreateGitHubRepoDialog by remember { mutableStateOf(false) }
                var showBranchDialog by remember { mutableStateOf(false) }
                var showCommitHistoryDialog by remember { mutableStateOf(false) }
                var showProjectFilesDialog by remember { mutableStateOf(false) }
                var showCreatePrDialog by remember { mutableStateOf(false) }
                var showCreateReleaseDialog by remember { mutableStateOf(false) }

                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                // Check pending OAuth code
                LaunchedEffect(pendingOAuthCode) {
                    pendingOAuthCode?.let { code ->
                        viewModel.saveGitHubToken(code)
                        pendingOAuthCode = null
                    }
                }

                // Handle status messages
                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearStatusMessages()
                    }
                }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearStatusMessages()
                    }
                }

                // SAF Folder Picker launcher
                val safLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                        } catch (ignored: Exception) {}
                        viewModel.openFolderFromSaf(uri)
                    }
                }

                // BackHandler
                BackHandler(enabled = currentTab != NavigationTab.HOME) {
                    viewModel.setTab(NavigationTab.HOME)
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_screen"),
                    containerColor = GitHubDarkBackground,
                    topBar = {
                        AppTopBar(
                            activeRepo = activeRepo,
                            localRepos = localRepos,
                            onSelectRepo = { viewModel.selectRepository(it) },
                            onRefresh = { viewModel.refreshAll() },
                            onNewProject = { showNewProjectDialog = true },
                            onOpenFolder = { safLauncher.launch(null) }
                        )
                    },
                    bottomBar = {
                        AppBottomNav(
                            currentTab = currentTab,
                            changesCount = gitChanges.size,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.HOME -> {
                                HomeScreen(
                                    activeRepo = activeRepo,
                                    projectFiles = projectFiles,
                                    changesCount = gitChanges.size,
                                    commitCount = commitHistory.size,
                                    recentLogs = operationLogs,
                                    onQuickPublish = { showQuickPublish = true },
                                    onInitGit = { viewModel.initGit() },
                                    onNavigateTab = { viewModel.setTab(it) },
                                    onOpenCommitHistory = { showCommitHistoryDialog = true },
                                    onOpenBranchManager = { showBranchDialog = true },
                                    onOpenFiles = { showProjectFilesDialog = true },
                                    onPush = { viewModel.push() },
                                    onPull = { viewModel.pull() },
                                    onOpenFolder = { safLauncher.launch(null) },
                                    onSyncToFolder = { viewModel.syncActiveRepoToDeviceFolder() }
                                )
                            }
                            NavigationTab.REPOSITORIES -> {
                                RepositoriesScreen(
                                    activeRepo = activeRepo,
                                    localRepos = localRepos,
                                    githubRepos = githubRepos,
                                    isAuthenticated = viewModel.isGitHubAuthenticated(),
                                    onSelectRepo = { viewModel.selectRepository(it) },
                                    onNewProject = { showNewProjectDialog = true },
                                    onOpenFolder = { safLauncher.launch(null) },
                                    onRemoveRepo = { viewModel.removeRepository(it) },
                                    onCloneRepo = { url ->
                                        cloneInitialUrl = url
                                        showCloneDialog = true
                                    },
                                    onCreateGitHubRepo = { showCreateGitHubRepoDialog = true },
                                    onViewReadme = { owner, repo ->
                                        viewModel.showReadme(owner, repo)
                                    },
                                    onNavigateSettings = { viewModel.setTab(NavigationTab.SETTINGS) },
                                    onOpenRepositoryPath = { path -> viewModel.openRepositoryByPath(path) }
                                )
                            }
                            NavigationTab.CHANGES -> {
                                ChangesScreen(
                                    activeRepo = activeRepo,
                                    changes = gitChanges,
                                    isLoading = isLoading,
                                    onStageFile = { viewModel.stageFile(it) },
                                    onStageAll = { viewModel.stageAll() },
                                    onUnstageFile = { viewModel.unstageFile(it) },
                                    onUnstageAll = { viewModel.unstageAll() },
                                    onCommit = { viewModel.commit(it) },
                                    onPush = { viewModel.push() },
                                    onPull = { viewModel.pull() },
                                    onShowDiff = { viewModel.showDiff(it) },
                                    onOpenBranchManager = { showBranchDialog = true },
                                    onOpenCommitHistory = { showCommitHistoryDialog = true },
                                    onOpenCreatePr = { showCreatePrDialog = true },
                                    onOpenCreateRelease = { showCreateReleaseDialog = true }
                                )
                            }
                            NavigationTab.ACTIVITY -> {
                                ActivityScreen(
                                    logs = operationLogs,
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                            }
                            NavigationTab.SETTINGS -> {
                                SettingsScreen(
                                    gitHubUser = gitHubUser,
                                    isAuthenticated = viewModel.isGitHubAuthenticated(),
                                    currentAuthorName = viewModel.getGitAuthorName(),
                                    currentAuthorEmail = viewModel.getGitAuthorEmail(),
                                    gitignoreContent = viewModel.getGitignoreContent(),
                                    activeRepo = activeRepo,
                                    onSaveToken = { viewModel.saveGitHubToken(it) },
                                    onSignOut = { viewModel.signOut() },
                                    onSaveAuthor = { name, email -> viewModel.saveGitAuthor(name, email) },
                                    onSaveGitignore = { viewModel.saveGitignore(it) },
                                    onGetPresetGitignore = { viewModel.getGitignorePreset(it) }
                                )
                            }
                        }

                        if (isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("loading_progress"),
                                color = GitHubGreen
                            )
                        }
                    }

                    // Dialogs
                    if (showQuickPublish) {
                        QuickPublishDialog(
                            activeRepo = activeRepo,
                            localRepos = localRepos,
                            projectFiles = projectFiles,
                            isAuthenticated = viewModel.isGitHubAuthenticated(),
                            isLoading = isLoading,
                            onDismiss = { showQuickPublish = false },
                            onSaveToken = { viewModel.saveGitHubToken(it) },
                            onOpenFolder = { safLauncher.launch(null) },
                            onPublish = { name, desc, priv, branch, msg, onSuccess ->
                                viewModel.executeQuickPublish(name, desc, priv, branch, msg, onSuccess)
                            }
                        )
                    }

                    if (showCreateGitHubRepoDialog) {
                        CreateGitHubRepoDialog(
                            isAuthenticated = viewModel.isGitHubAuthenticated(),
                            currentUsername = gitHubUser?.login ?: "",
                            isLoading = isLoading,
                            onDismiss = { showCreateGitHubRepoDialog = false },
                            onSaveToken = { viewModel.saveGitHubToken(it) },
                            onCreateRepo = { name, desc, priv, autoInit, onComplete ->
                                viewModel.createRemoteGitHubRepo(name, desc, priv, autoInit, onComplete)
                            }
                        )
                    }

                    if (showCloneDialog) {
                        CloneRepoDialog(
                            initialUrl = cloneInitialUrl,
                            isLoading = isLoading,
                            onDismiss = { showCloneDialog = false },
                            onClone = { url, name ->
                                viewModel.cloneRemoteRepo(url, name)
                            }
                        )
                    }

                    if (showNewProjectDialog) {
                        NewProjectDialog(
                            isLoading = isLoading,
                            onDismiss = { showNewProjectDialog = false },
                            onCreate = { name, template ->
                                viewModel.createNewProject(name, template)
                            }
                        )
                    }

                    if (showBranchDialog) {
                        BranchDialog(
                            currentBranch = activeRepo?.currentBranch ?: "main",
                            branches = branches,
                            onSwitchBranch = { viewModel.switchBranch(it) },
                            onCreateBranch = { viewModel.createBranch(it) },
                            onRenameBranch = { old, new -> viewModel.renameBranch(old, new) },
                            onDismiss = { showBranchDialog = false }
                        )
                    }

                    if (showCommitHistoryDialog) {
                        CommitHistoryDialog(
                            repoName = activeRepo?.name ?: "",
                            branchName = activeRepo?.currentBranch ?: "main",
                            commits = commitHistory,
                            onDismiss = { showCommitHistoryDialog = false }
                        )
                    }

                    if (showProjectFilesDialog) {
                        ProjectFilesDialog(
                            repoName = activeRepo?.name ?: "",
                            files = projectFiles,
                            onDismiss = { showProjectFilesDialog = false }
                        )
                    }

                    if (showCreatePrDialog) {
                        CreatePrDialog(
                            defaultHeadBranch = activeRepo?.currentBranch ?: "main",
                            branches = branches,
                            isLoading = isLoading,
                            onDismiss = { showCreatePrDialog = false },
                            onCreatePr = { title, body, head, base ->
                                val repo = activeRepo
                                if (repo != null && repo.remoteUrl.contains("github.com/")) {
                                    val parts = repo.remoteUrl.substringAfter("github.com/").removeSuffix(".git").split("/")
                                    if (parts.size >= 2) {
                                        viewModel.createPullRequest(parts[0], parts[1], title, body, head, base) {}
                                    }
                                }
                            }
                        )
                    }

                    if (showCreateReleaseDialog) {
                        CreateReleaseDialog(
                            isLoading = isLoading,
                            onDismiss = { showCreateReleaseDialog = false },
                            onCreateRelease = { tag, title, notes, draft, prerelease ->
                                val repo = activeRepo
                                if (repo != null && repo.remoteUrl.contains("github.com/")) {
                                    val parts = repo.remoteUrl.substringAfter("github.com/").removeSuffix(".git").split("/")
                                    if (parts.size >= 2) {
                                        viewModel.createRelease(parts[0], parts[1], tag, title, notes, draft, prerelease) {}
                                    }
                                }
                            }
                        )
                    }

                    diffDialogData?.let { (file, diff) ->
                        DiffDialog(
                            filePath = file,
                            diffText = diff,
                            onDismiss = { viewModel.closeDiff() }
                        )
                    }

                    readmeDialogData?.let { (repo, content) ->
                        ReadmeDialog(
                            repoName = repo,
                            content = content,
                            onDismiss = { viewModel.closeReadme() }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if ("repostudio" == uri.scheme && "oauth-callback" == uri.host) {
            val code = uri.getQueryParameter("code")
            if (code != null && code.isNotEmpty()) {
                pendingOAuthCode = code
            }
        }
    }
}
