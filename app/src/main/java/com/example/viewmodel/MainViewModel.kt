package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.RepositoryService
import com.example.core.models.*
import com.example.data.local.AppDatabase
import com.example.data.local.LocalRepoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repoService = RepositoryService.getInstance(application)
    private val roomRepo = LocalRepoRepository(AppDatabase.getDatabase(application).localRepoDao())

    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _activeRepo = MutableStateFlow<LocalRepository?>(null)
    val activeRepo: StateFlow<LocalRepository?> = _activeRepo.asStateFlow()

    private val _localRepos = MutableStateFlow<List<LocalRepository>>(emptyList())
    val localRepos: StateFlow<List<LocalRepository>> = _localRepos.asStateFlow()

    private val _githubRepos = MutableStateFlow<List<GitHubRepo>>(emptyList())
    val githubRepos: StateFlow<List<GitHubRepo>> = _githubRepos.asStateFlow()

    private val _gitHubUser = MutableStateFlow<GitHubUser?>(null)
    val gitHubUser: StateFlow<GitHubUser?> = _gitHubUser.asStateFlow()

    private val _gitChanges = MutableStateFlow<List<GitFileStatus>>(emptyList())
    val gitChanges: StateFlow<List<GitFileStatus>> = _gitChanges.asStateFlow()

    private val _commitHistory = MutableStateFlow<List<GitCommitInfo>>(emptyList())
    val commitHistory: StateFlow<List<GitCommitInfo>> = _commitHistory.asStateFlow()

    private val _branches = MutableStateFlow<List<String>>(listOf("main"))
    val branches: StateFlow<List<String>> = _branches.asStateFlow()

    private val _operationLogs = MutableStateFlow<List<OperationLog>>(emptyList())
    val operationLogs: StateFlow<List<OperationLog>> = _operationLogs.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val projectFiles: StateFlow<List<ProjectFile>> = _projectFiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _diffDialogData = MutableStateFlow<Pair<String, String>?>(null)
    val diffDialogData: StateFlow<Pair<String, String>?> = _diffDialogData.asStateFlow()

    private val _readmeDialogData = MutableStateFlow<Pair<String, String>?>(null)
    val readmeDialogData: StateFlow<Pair<String, String>?> = _readmeDialogData.asStateFlow()

    init {
        refreshAll()
        loadGitHubUserAndRepos()
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun clearStatusMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _localRepos.value = repoService.allLocalRepositories
            _operationLogs.value = repoService.operationLogs
            val active = repoService.activeRepository
            _activeRepo.value = active
            if (active != null) {
                refreshCurrentRepoData()
            }
        }
    }

    fun refreshCurrentRepoData() {
        val active = repoService.activeRepository ?: return
        val dir = File(active.path)
        try {
            _projectFiles.value = repoService.safFileManager.scanProjectFiles(dir, emptySet())
            if (repoService.gitEngine.isGitRepository(dir)) {
                _gitChanges.value = repoService.activeRepoStatus
                _commitHistory.value = repoService.gitEngine.getCommitHistory(dir, 50)
                _branches.value = repoService.gitEngine.getBranches(dir)
                val currentB = repoService.gitEngine.getCurrentBranch(dir)
                active.currentBranch = currentB
                _activeRepo.value = active
            } else {
                _gitChanges.value = emptyList()
                _commitHistory.value = emptyList()
                _branches.value = listOf("main")
            }
        } catch (e: Exception) {
            // Non-fatal error during data scan
        }
    }

    fun selectRepository(repo: LocalRepository) {
        viewModelScope.launch(Dispatchers.IO) {
            repoService.activeRepository = repo
            _activeRepo.value = repo
            refreshCurrentRepoData()
            _operationLogs.value = repoService.operationLogs
        }
    }

    fun initGit() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                repoService.initGitInActiveRepo()
                refreshCurrentRepoData()
                _statusMessage.value = "Git repository initialized successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize Git: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun stageFile(pattern: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repoService.stageFile(pattern)
                _gitChanges.value = repoService.activeRepoStatus
            } catch (e: Exception) {
                _errorMessage.value = "Failed to stage file: ${e.message}"
            } finally {
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun stageAll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repoService.stageAll()
                _gitChanges.value = repoService.activeRepoStatus
            } catch (e: Exception) {
                _errorMessage.value = "Failed to stage all: ${e.message}"
            } finally {
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun unstageFile(pattern: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repoService.unstageFile(pattern)
                _gitChanges.value = repoService.activeRepoStatus
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unstage: ${e.message}"
            } finally {
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun unstageAll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repoService.unstageAll()
                _gitChanges.value = repoService.activeRepoStatus
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unstage all: ${e.message}"
            } finally {
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun commit(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val commitInfo = repoService.commitChanges(message)
                refreshCurrentRepoData()
                _statusMessage.value = "Committed [${commitInfo.shortId}]: $message"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to commit: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun push() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = repoService.pushActiveRepo()
                _statusMessage.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Failed to push: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun pull() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = repoService.pullActiveRepo()
                refreshCurrentRepoData()
                _statusMessage.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Failed to pull: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = repoService.fetchActiveRepo()
                _statusMessage.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun switchBranch(branch: String) {
        val active = repoService.activeRepository ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                repoService.gitEngine.checkoutBranch(File(active.path), branch)
                refreshCurrentRepoData()
                _statusMessage.value = "Switched to branch $branch"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to switch branch: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun createBranch(branch: String) {
        val active = repoService.activeRepository ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                repoService.gitEngine.createBranch(File(active.path), branch)
                refreshCurrentRepoData()
                _statusMessage.value = "Created branch $branch"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create branch: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun renameBranch(oldName: String, newName: String) {
        val active = repoService.activeRepository ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                repoService.gitEngine.renameBranch(File(active.path), oldName, newName)
                refreshCurrentRepoData()
                _statusMessage.value = "Renamed branch to $newName"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rename branch: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun createNewProject(name: String, templateType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val newDir = repoService.safFileManager.createNewProjectDirectory(name)
                repoService.safFileManager.generateStarterProject(newDir, templateType, name)
                repoService.gitEngine.initRepository(newDir)
                val newRepo = LocalRepository(newDir.name, newDir.absolutePath, "", "main", true)
                repoService.activeRepository = newRepo
                _localRepos.value = repoService.allLocalRepositories
                _activeRepo.value = newRepo
                refreshCurrentRepoData()
                _statusMessage.value = "Created project '$name'"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create project: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun openFolderFromSaf(treeUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val opened = repoService.openFolderFromSaf(treeUri)
                _localRepos.value = repoService.allLocalRepositories
                _activeRepo.value = opened
                refreshCurrentRepoData()
                _statusMessage.value = "Opened folder '${opened.name}'"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to open folder: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun syncActiveRepoToDeviceFolder() {
        val active = repoService.activeRepository ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                _statusMessage.value = "Repository synced"
            } catch (e: Exception) {
                _errorMessage.value = "Sync failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importFromSaf(treeUri: Uri) {
        openFolderFromSaf(treeUri)
    }

    fun removeRepository(repo: LocalRepository) {
        viewModelScope.launch(Dispatchers.IO) {
            repoService.removeLocalRepository(repo)
            _localRepos.value = repoService.allLocalRepositories
            if (_activeRepo.value?.path == repo.path) {
                _activeRepo.value = null
            }
        }
    }

    fun openRepositoryByPath(path: String) {
        val file = File(path)
        if (!file.exists()) {
            _errorMessage.value = "Path does not exist: $path"
            return
        }
        val isGit = repoService.gitEngine.isGitRepository(file)
        val branch = if (isGit) repoService.gitEngine.getCurrentBranch(file) else "main"
        val repo = LocalRepository(file.name, file.absolutePath, "", branch, isGit)
        repoService.activeRepository = repo
        _activeRepo.value = repo
        _localRepos.value = repoService.allLocalRepositories
        refreshCurrentRepoData()
    }

    fun cloneRemoteRepo(cloneUrl: String, repoName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val dir = File(repoService.safFileManager.baseProjectsDir, repoName.ifBlank { "cloned-repo" })
                val token = repoService.keystoreManager.gitHubToken ?: ""
                repoService.gitEngine.cloneRepository(cloneUrl, dir, token)
                val repo = LocalRepository(dir.name, dir.absolutePath, cloneUrl, "main", true)
                repoService.activeRepository = repo
                _localRepos.value = repoService.allLocalRepositories
                _activeRepo.value = repo
                refreshCurrentRepoData()
                _statusMessage.value = "Cloned '$repoName' successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clone: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun saveGitHubToken(token: String) {
        repoService.keystoreManager.saveGitHubToken(token)
        _statusMessage.value = "GitHub token saved securely"
        loadGitHubUserAndRepos()
    }

    fun signOut() {
        repoService.keystoreManager.clearToken()
        _gitHubUser.value = null
        _githubRepos.value = emptyList()
        _statusMessage.value = "Signed out of GitHub"
    }

    fun saveGitAuthor(name: String, email: String) {
        repoService.keystoreManager.gitAuthorName = name
        repoService.keystoreManager.gitAuthorEmail = email
        _statusMessage.value = "Git author profile saved"
    }

    fun loadGitHubUserAndRepos() {
        if (!isGitHubAuthenticated()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repoService.gitHubApiClient.currentUser
                _gitHubUser.value = user
                val repos = repoService.gitHubApiClient.userRepositories
                _githubRepos.value = repos
            } catch (e: Exception) {
                // Ignore API connection errors gracefully
            }
        }
    }

    fun showDiff(relativePath: String) {
        val active = repoService.activeRepository ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val diff = repoService.gitEngine.getFileDiff(File(active.path), relativePath)
            _diffDialogData.value = Pair(relativePath, diff ?: "No diff available.")
        }
    }

    fun closeDiff() {
        _diffDialogData.value = null
    }

    fun showReadme(owner: String, repo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = repoService.gitHubApiClient.getReadmeContent(owner, repo)
                _readmeDialogData.value = Pair(repo, content ?: "No README found.")
            } catch (e: Exception) {
                _readmeDialogData.value = Pair(repo, "Could not load README: ${e.message}")
            }
        }
    }

    fun closeReadme() {
        _readmeDialogData.value = null
    }

    fun executeQuickPublish(
        repoName: String,
        description: String,
        isPrivate: Boolean,
        branchName: String,
        commitMsg: String,
        onSuccess: (String) -> Unit
    ) {
        val active = repoService.activeRepository ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                repoService.quickPublish(File(active.path), repoName, description, isPrivate, branchName, commitMsg)
                refreshCurrentRepoData()
                val url = "https://github.com/${_gitHubUser.value?.login ?: "user"}/$repoName"
                _statusMessage.value = "Published to $url"
                withContext(Dispatchers.Main) {
                    onSuccess(url)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Publish failed: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun createRemoteGitHubRepo(
        name: String,
        description: String,
        isPrivate: Boolean,
        autoInit: Boolean,
        onComplete: (GitHubRepo) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val created = repoService.createRemoteGitHubRepository(name, description, isPrivate, autoInit)
                loadGitHubUserAndRepos()
                _statusMessage.value = "Repository '${created.name}' created on GitHub"
                withContext(Dispatchers.Main) {
                    onComplete(created)
                }
            } catch (e: Throwable) {
                _errorMessage.value = "Failed to create remote repository: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun createPullRequest(
        owner: String,
        repo: String,
        title: String,
        body: String,
        head: String,
        base: String,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val prUrl = repoService.gitHubApiClient.createPullRequest(owner, repo, title, body, head, base)
                _statusMessage.value = "Pull request created: $prUrl"
                withContext(Dispatchers.Main) {
                    onComplete(prUrl)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create PR: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun createRelease(
        owner: String,
        repo: String,
        tag: String,
        title: String,
        notes: String,
        isDraft: Boolean,
        isPrerelease: Boolean,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val releaseUrl = repoService.gitHubApiClient.createRelease(owner, repo, tag, title, notes, isDraft, isPrerelease)
                _statusMessage.value = "Release created: $releaseUrl"
                withContext(Dispatchers.Main) {
                    onComplete(releaseUrl)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create release: ${e.message}"
            } finally {
                _isLoading.value = false
                _operationLogs.value = repoService.operationLogs
            }
        }
    }

    fun clearLogs() {
        repoService.clearLogs()
        _operationLogs.value = emptyList()
    }

    fun saveGitignore(content: String) {
        val active = repoService.activeRepository ?: return
        try {
            repoService.safFileManager.writeGitignore(File(active.path), content)
            _statusMessage.value = ".gitignore updated"
        } catch (e: Exception) {
            _errorMessage.value = "Failed to save .gitignore: ${e.message}"
        }
    }

    fun getGitignoreContent(): String {
        val active = repoService.activeRepository ?: return ""
        return repoService.safFileManager.readGitignore(File(active.path)) ?: ""
    }

    fun getGitignorePreset(type: String): String {
        return repoService.safFileManager.getGitignorePreset(type) ?: ""
    }

    fun getGitAuthorName(): String {
        return repoService.keystoreManager.gitAuthorName ?: ""
    }

    fun getGitAuthorEmail(): String {
        return repoService.keystoreManager.gitAuthorEmail ?: ""
    }

    fun isGitHubAuthenticated(): Boolean {
        return repoService.keystoreManager.hasValidToken()
    }
}
