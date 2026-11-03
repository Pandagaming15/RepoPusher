package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.RepoStudioTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.RepositoryPathsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var repoPathsViewModel: RepositoryPathsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        repoPathsViewModel = ViewModelProvider(this)[RepositoryPathsViewModel::class.java]

        handleOAuthIntent(intent)

        setContent {
            RepoStudioTheme {
                val currentTab by mainViewModel.currentTab.collectAsStateWithLifecycle()
                val activeRepo by mainViewModel.activeRepo.collectAsStateWithLifecycle()
                val localRepos by mainViewModel.localRepos.collectAsStateWithLifecycle()
                val githubRepos by mainViewModel.githubRepos.collectAsStateWithLifecycle()
                val gitHubUser by mainViewModel.gitHubUser.collectAsStateWithLifecycle()
                val gitChanges by mainViewModel.gitChanges.collectAsStateWithLifecycle()
                val commitHistory by mainViewModel.commitHistory.collectAsStateWithLifecycle()
                val branches by mainViewModel.branches.collectAsStateWithLifecycle()
                val operationLogs by mainViewModel.operationLogs.collectAsStateWithLifecycle()
                val projectFiles by mainViewModel.projectFiles.collectAsStateWithLifecycle()
                val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
                val statusMessage by mainViewModel.statusMessage.collectAsStateWithLifecycle()
                val errorMessage by mainViewModel.errorMessage.collectAsStateWithLifecycle()
                val diffDialogData by mainViewModel.diffDialogData.collectAsStateWithLifecycle()
                val readmeDialogData by mainViewModel.readmeDialogData.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                // Dialog visibility states
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

                // SAF directory picker
                val openDocumentTreeLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri: Uri? ->
                    uri?.let {
                        try {
                            contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                        } catch (e: Exception) {
                            // Ignore if permission cannot be persisted
                        }
                        mainViewModel.openFolderFromSaf(it)
                        val folderName = uri.lastPathSegment?.substringAfterLast(':')?.substringAfterLast('/') ?: "Repository"
                        repoPathsViewModel.addRepositoryPath(
                            path = it.toString(),
                            name = folderName,
                            safTreeUri = it.toString()
                        )
                    }
                }

                // Show status or error snackbars
                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        mainViewModel.clearStatusMessages()
                    }
                }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        mainViewModel.clearStatusMessages()
                    }
                }

                Scaffold(
                    topBar = {
                        AppTopBar(
                            activeRepo = activeRepo,
                            localRepos = localRepos,
                            onSelectRepo = { mainViewModel.selectRepository(it) },
                            onRefresh = { mainViewModel.refreshAll() },
                            onNewProject = { showNewProjectDialog = true },
                            onOpenFolder = { openDocumentTreeLauncher.launch(null) }
                        )
                    },
                    bottomBar = {
                        AppBottomNav(
                            currentTab = currentTab,
                            changesCount = gitChanges.size,
                            onTabSelected = { mainViewModel.setTab(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold")
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
                                    onInitGit = { mainViewModel.initGit() },
                                    onNavigateTab = { mainViewModel.setTab(it) },
                                    onOpenCommitHistory = { showCommitHistoryDialog = true },
                                    onOpenBranchManager = { showBranchDialog = true },
                                    onOpenFiles = { showProjectFilesDialog = true },
                                    onPush = { mainViewModel.push() },
                                    onPull = { mainViewModel.pull() },
                                    onOpenFolder = { openDocumentTreeLauncher.launch(null) },
                                    onSyncToFolder = { mainViewModel.syncActiveRepoToDeviceFolder() }
                                )
                            }
                            NavigationTab.REPOSITORIES -> {
                                RepositoriesScreen(
                                    activeRepo = activeRepo,
                                    localRepos = localRepos,
                                    githubRepos = githubRepos,
                                    isAuthenticated = gitHubUser != null,
                                    onSelectRepo = { mainViewModel.selectRepository(it) },
                                    onNewProject = { showNewProjectDialog = true },
                                    onPickFolder = { openDocumentTreeLauncher.launch(null) },
                                    onRefreshGitHubRepos = { mainViewModel.loadGitHubUserAndRepos() },
                                    onDeleteRepo = { mainViewModel.removeRepository(it) },
                                    onCloneRepo = { url ->
                                        cloneInitialUrl = url
                                        showCloneDialog = true
                                    },
                                    onCreateGitHubRepo = { showCreateGitHubRepoDialog = true },
                                    onViewReadme = { owner, repo -> mainViewModel.showReadme(owner, repo) },
                                    onNavigateSettings = { mainViewModel.setTab(NavigationTab.SETTINGS) },
                                    repoPathsViewModel = repoPathsViewModel
                                )
                            }
                            NavigationTab.CHANGES -> {
                                ChangesScreen(
                                    activeRepo = activeRepo,
                                    changes = gitChanges,
                                    isLoading = isLoading,
                                    onStageFile = { mainViewModel.stageFile(it) },
                                    onStageAll = { mainViewModel.stageAll() },
                                    onUnstageFile = { mainViewModel.unstageFile(it) },
                                    onUnstageAll = { mainViewModel.unstageAll() },
                                    onCommit = { mainViewModel.commit(it) },
                                    onPush = { mainViewModel.push() },
                                    onPull = { mainViewModel.pull() },
                                    onShowDiff = { mainViewModel.showDiff(it) },
                                    onOpenBranchManager = { showBranchDialog = true },
                                    onOpenCommitHistory = { showCommitHistoryDialog = true },
                                    onOpenCreatePr = { showCreatePrDialog = true },
                                    onOpenCreateRelease = { showCreateReleaseDialog = true }
                                )
                            }
                            NavigationTab.ACTIVITY -> {
                                ActivityScreen(
                                    logs = operationLogs,
                                    onClearLogs = { mainViewModel.clearLogs() }
                                )
                            }
                            NavigationTab.SETTINGS -> {
                                SettingsScreen(
                                    gitHubUser = gitHubUser,
                                    isAuthenticated = gitHubUser != null,
                                    currentAuthorName = mainViewModel.getGitAuthorName(),
                                    currentAuthorEmail = mainViewModel.getGitAuthorEmail(),
                                    gitignoreContent = mainViewModel.getGitignoreContent(),
                                    activeRepo = activeRepo,
                                    onSaveToken = { mainViewModel.saveGitHubToken(it) },
                                    onSignOut = { mainViewModel.signOut() },
                                    onSaveAuthor = { name, email -> mainViewModel.saveGitAuthor(name, email) },
                                    onSaveGitignore = { mainViewModel.saveGitignore(it) },
                                    onGetPresetGitignore = { mainViewModel.getGitignorePreset(it) }
                                )
                            }
                        }

                        // Dialogs
                        if (showQuickPublish) {
                            QuickPublishDialog(
                                activeRepo = activeRepo,
                                localRepos = localRepos,
                                projectFiles = projectFiles,
                                isAuthenticated = gitHubUser != null,
                                isLoading = isLoading,
                                onDismiss = { showQuickPublish = false },
                                onPublish = { repoName, desc, isPrivate, branch, commitMsg, onComplete ->
                                    mainViewModel.executeQuickPublish(repoName, desc, isPrivate, branch, commitMsg, onComplete)
                                }
                            )
                        }

                        if (showCloneDialog) {
                            CloneRepoDialog(
                                initialUrl = cloneInitialUrl,
                                isLoading = isLoading,
                                onDismiss = { showCloneDialog = false },
                                onClone = { url, dirName ->
                                    mainViewModel.cloneRemoteRepo(url, dirName)
                                    showCloneDialog = false
                                }
                            )
                        }

                        if (showNewProjectDialog) {
                            NewProjectDialog(
                                isLoading = isLoading,
                                onDismiss = { showNewProjectDialog = false },
                                onCreate = { name, path ->
                                    mainViewModel.createNewProject(name, "android")
                                    showNewProjectDialog = false
                                }
                            )
                        }

                        if (showCreateGitHubRepoDialog) {
                            CreateGitHubRepoDialog(
                                isAuthenticated = gitHubUser != null,
                                currentUsername = gitHubUser?.login ?: "",
                                isLoading = isLoading,
                                onDismiss = { showCreateGitHubRepoDialog = false },
                                onSaveToken = { mainViewModel.saveGitHubToken(it) },
                                onCreateRepo = { name, desc, isPrivate, autoInit, onCreated ->
                                    mainViewModel.createRemoteGitHubRepo(name, desc, isPrivate, autoInit, onCreated)
                                }
                            )
                        }

                        if (showBranchDialog) {
                            BranchDialog(
                                currentBranch = activeRepo?.currentBranch ?: "master",
                                branches = branches,
                                onSwitchBranch = { mainViewModel.switchBranch(it) },
                                onCreateBranch = { mainViewModel.createBranch(it) },
                                onRenameBranch = { oldB, newB -> mainViewModel.renameBranch(oldB, newB) },
                                onDismiss = { showBranchDialog = false }
                            )
                        }

                        if (showCommitHistoryDialog) {
                            CommitHistoryDialog(
                                repoName = activeRepo?.name ?: "Repository",
                                branchName = activeRepo?.currentBranch ?: "master",
                                commits = commitHistory,
                                onDismiss = { showCommitHistoryDialog = false }
                            )
                        }

                        if (showProjectFilesDialog) {
                            ProjectFilesDialog(
                                repoName = activeRepo?.name ?: "Repository",
                                files = projectFiles,
                                onDismiss = { showProjectFilesDialog = false }
                            )
                        }

                        if (showCreatePrDialog) {
                            CreatePrDialog(
                                defaultHeadBranch = activeRepo?.currentBranch ?: "master",
                                branches = branches,
                                isLoading = isLoading,
                                onDismiss = { showCreatePrDialog = false },
                                onCreatePr = { title, body, head, base ->
                                    val repoName = activeRepo?.name ?: "repo"
                                    val owner = gitHubUser?.login ?: "owner"
                                    mainViewModel.createPullRequest(owner, repoName, title, body, head, base) {}
                                    showCreatePrDialog = false
                                }
                            )
                        }

                        if (showCreateReleaseDialog) {
                            CreateReleaseDialog(
                                isLoading = isLoading,
                                onDismiss = { showCreateReleaseDialog = false },
                                onCreateRelease = { tag, name, body, isDraft, isPre ->
                                    val repoName = activeRepo?.name ?: "repo"
                                    val owner = gitHubUser?.login ?: "owner"
                                    mainViewModel.createRelease(owner, repoName, tag, name, body, isDraft, isPre) {}
                                    showCreateReleaseDialog = false
                                }
                            )
                        }

                        diffDialogData?.let { (filePath, diffText) ->
                            DiffDialog(
                                filePath = filePath,
                                diffText = diffText,
                                onDismiss = { mainViewModel.closeDiff() }
                            )
                        }

                        readmeDialogData?.let { (repoName, content) ->
                            ReadmeDialog(
                                repoName = repoName,
                                content = content,
                                onDismiss = { mainViewModel.closeReadme() }
                            )
                        }
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
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "repostudio" && data.host == "oauth-callback") {
            val code = data.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                mainViewModel.saveGitHubToken(code)
            }
        }
    }
}
