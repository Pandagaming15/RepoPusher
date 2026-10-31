package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.RepositoryService
import com.example.core.models.GitCommitInfo
import com.example.core.models.GitFileStatus
import com.example.core.models.GitHubRepo
import com.example.core.models.GitHubUser
import com.example.core.models.LocalRepository
import com.example.core.models.OperationLog
import com.example.core.models.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class NavigationTab {
    HOME,
    REPOSITORIES,
    CHANGES,
    ACTIVITY,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repoService = RepositoryService.getInstance(application)

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

    // Dialog & Detail states
    private val _diffDialogData = MutableStateFlow<Pair<String, String>?>(null) // (File, DiffText)
    val diffDialogData: StateFlow<Pair<String, String>?> = _diffDialogData.asStateFlow()

    private val _readmeDialogData = MutableStateFlow<Pair<String, String>?>(null) // (RepoName, Content)
    val readmeDialogData: StateFlow<Pair<String, String>?> = _readmeDialogData.asStateFlow()

    init {
        refreshAll()
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun clearStatusMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.refreshActiveRepoInfo()
                    _activeRepo.value = repoService.activeRepository
                    _localRepos.value = repoService.allLocalRepositories
                    _operationLogs.value = repoService.operationLogs

                    refreshCurrentRepoData()
                    loadGitHubUserAndRepos()
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun refreshCurrentRepoData() {
        val repo = repoService.activeRepository ?: return
        val dir = File(repo.path)
        if (dir.exists()) {
            // Scan project files
            val files = repoService.safFileManager.scanProjectFiles(dir, null)
            _projectFiles.value = files

            // Git data
            if (repoService.gitEngine.isGitRepository(dir)) {
                _gitChanges.value = repoService.gitEngine.getStatus(dir)
                _commitHistory.value = repoService.gitEngine.getCommitHistory(dir, 50)
                _branches.value = repoService.gitEngine.getBranches(dir)
            } else {
                _gitChanges.value = emptyList()
                _commitHistory.value = emptyList()
                _branches.value = listOf("main")
            }
        }
    }

    fun selectRepository(repo: LocalRepository) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                repoService.activeRepository = repo
                _activeRepo.value = repo
                refreshCurrentRepoData()
                _operationLogs.value = repoService.operationLogs
            }
            _isLoading.value = false
        }
    }

    fun initGit() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.initGitInActiveRepo()
                    _activeRepo.value = repoService.activeRepository
                    refreshCurrentRepoData()
                    _statusMessage.value = "Git initialized successfully"
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to init Git: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun stageFile(pattern: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    repoService.stageFile(pattern)
                    refreshCurrentRepoData()
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to stage: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                }
            }
        }
    }

    fun stageAll() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.stageAll()
                    refreshCurrentRepoData()
                    _statusMessage.value = "All changes staged"
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to stage all: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun unstageFile(pattern: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    repoService.unstageFile(pattern)
                    refreshCurrentRepoData()
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to unstage: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                }
            }
        }
    }

    fun unstageAll() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.unstageAll()
                    refreshCurrentRepoData()
                    _statusMessage.value = "All changes unstaged"
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to unstage all: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun commit(message: String) {
        if (message.isBlank()) {
            _errorMessage.value = "Please enter a commit message"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val info = repoService.commitChanges(message.trim())
                    refreshCurrentRepoData()
                    _statusMessage.value = "Committed [${info.shortId}]: ${info.message}"
                } catch (e: Exception) {
                    _errorMessage.value = "Commit failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun push() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val res = repoService.pushActiveRepo()
                    _statusMessage.value = "Pushed to GitHub: $res"
                } catch (e: Exception) {
                    _errorMessage.value = "Push failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun pull() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val res = repoService.pullActiveRepo()
                    refreshCurrentRepoData()
                    _statusMessage.value = res
                } catch (e: Exception) {
                    _errorMessage.value = "Pull failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val res = repoService.fetchActiveRepo()
                    _statusMessage.value = res
                } catch (e: Exception) {
                    _errorMessage.value = "Fetch failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun switchBranch(branch: String) {
        viewModelScope.launch {
            val repo = repoService.activeRepository ?: return@launch
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.gitEngine.checkoutBranch(File(repo.path), branch)
                    repoService.refreshActiveRepoInfo()
                    _activeRepo.value = repoService.activeRepository
                    refreshCurrentRepoData()
                    _statusMessage.value = "Switched to branch $branch"
                } catch (e: Exception) {
                    _errorMessage.value = "Branch checkout failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun createBranch(branch: String) {
        if (branch.isBlank()) return
        viewModelScope.launch {
            val repo = repoService.activeRepository ?: return@launch
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.gitEngine.createBranch(File(repo.path), branch.trim())
                    repoService.gitEngine.checkoutBranch(File(repo.path), branch.trim())
                    repoService.refreshActiveRepoInfo()
                    _activeRepo.value = repoService.activeRepository
                    refreshCurrentRepoData()
                    _statusMessage.value = "Created and switched to branch $branch"
                } catch (e: Exception) {
                    _errorMessage.value = "Create branch failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun renameBranch(oldName: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val repo = repoService.activeRepository ?: return@launch
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.gitEngine.renameBranch(File(repo.path), oldName, newName.trim())
                    repoService.refreshActiveRepoInfo()
                    _activeRepo.value = repoService.activeRepository
                    refreshCurrentRepoData()
                    _statusMessage.value = "Renamed branch to $newName"
                } catch (e: Exception) {
                    _errorMessage.value = "Rename branch failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun createNewProject(name: String, templateType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val dir = repoService.safFileManager.createNewProjectDirectory(name)
                    repoService.safFileManager.generateStarterProject(dir, templateType, name)
                    repoService.gitEngine.initRepository(dir)
                    repoService.gitEngine.stageAll(dir)
                    repoService.gitEngine.commit(dir, "Initial commit ($templateType template)", repoService.keystoreManager.gitAuthorName, repoService.keystoreManager.gitAuthorEmail)
                    val newRepo = LocalRepository(name, dir.absolutePath, "", "main", true)
                    repoService.activeRepository = newRepo
                    _activeRepo.value = newRepo
                    _localRepos.value = repoService.allLocalRepositories
                    refreshCurrentRepoData()
                    _statusMessage.value = "Project $name created with $templateType template"
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to create project: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun openFolderFromSaf(treeUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val repo = repoService.openFolderFromSaf(treeUri)
                    _activeRepo.value = repo
                    _localRepos.value = repoService.allLocalRepositories
                    refreshCurrentRepoData()
                    _statusMessage.value = "Opened '${repo.name}' (${if (repo.isGitInitialized) "Git branch: ${repo.currentBranch}" else "ready to initialize"})"
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to open folder: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    // Backward-compatible alias
    fun importFromSaf(treeUri: Uri) {
        openFolderFromSaf(treeUri)
    }

    fun removeRepository(repo: LocalRepository) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repoService.removeLocalRepository(repo)
                _localRepos.value = repoService.allLocalRepositories
                _activeRepo.value = repoService.activeRepository
                refreshCurrentRepoData()
                _statusMessage.value = "Removed '${repo.name}' from repository list"
            }
        }
    }

    fun cloneRemoteRepo(cloneUrl: String, repoName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val clean = if (repoName.isNotBlank()) repoName else cloneUrl.substringAfterLast("/").removeSuffix(".git")
                    val targetDir = File(repoService.safFileManager.baseProjectsDir, clean)
                    val token = repoService.keystoreManager.gitHubToken ?: ""
                    repoService.gitEngine.cloneRepository(cloneUrl, targetDir, token)
                    val newRepo = LocalRepository(clean, targetDir.absolutePath, cloneUrl, "main", true)
                    repoService.activeRepository = newRepo
                    _activeRepo.value = newRepo
                    _localRepos.value = repoService.allLocalRepositories
                    refreshCurrentRepoData()
                    _statusMessage.value = "Cloned $clean successfully"
                } catch (e: Exception) {
                    _errorMessage.value = "Clone failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun saveGitHubToken(token: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val ok = repoService.keystoreManager.saveGitHubToken(token)
                if (ok) {
                    _statusMessage.value = "GitHub token saved securely in Keystore"
                    loadGitHubUserAndRepos()
                } else {
                    _errorMessage.value = "Failed to encrypt and save token"
                }
            }
        }
    }

    fun signOut() {
        repoService.keystoreManager.clearToken()
        _gitHubUser.value = null
        _githubRepos.value = emptyList()
        _statusMessage.value = "Signed out. GitHub token removed from secure storage."
    }

    fun saveGitAuthor(name: String, email: String) {
        repoService.keystoreManager.gitAuthorName = name.trim()
        repoService.keystoreManager.gitAuthorEmail = email.trim()
        _statusMessage.value = "Author profile updated"
    }

    fun loadGitHubUserAndRepos() {
        if (!repoService.keystoreManager.hasValidToken()) {
            _gitHubUser.value = null
            _githubRepos.value = emptyList()
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val user = repoService.gitHubApiClient.currentUser
                    _gitHubUser.value = user
                    val repos = repoService.gitHubApiClient.userRepositories
                    _githubRepos.value = repos
                } catch (e: Exception) {
                    // Could be rate limit or bad token
                    _errorMessage.value = "GitHub API: ${e.message}"
                }
            }
        }
    }

    fun showDiff(relativePath: String) {
        val repo = repoService.activeRepository ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val diff = repoService.gitEngine.getFileDiff(File(repo.path), relativePath)
                _diffDialogData.value = Pair(relativePath, diff)
            }
        }
    }

    fun closeDiff() {
        _diffDialogData.value = null
    }

    fun showReadme(owner: String, repo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val content = repoService.gitHubApiClient.getReadmeContent(owner, repo)
                    _readmeDialogData.value = Pair(repo, content)
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load README: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
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
        val repo = repoService.activeRepository ?: run {
            _errorMessage.value = "No active project directory selected"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    repoService.quickPublish(
                        File(repo.path),
                        repoName,
                        description,
                        isPrivate,
                        branchName,
                        commitMsg
                    )
                    _statusMessage.value = "Repository published to GitHub successfully!"
                    refreshAll()
                    withContext(Dispatchers.Main) {
                        onSuccess("https://github.com/${_gitHubUser.value?.login ?: ""}/$repoName")
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Publish failed: ${e.message}"
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
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
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val repo = repoService.createRemoteGitHubRepository(name, description, isPrivate, autoInit)
                    _statusMessage.value = "Repository created on GitHub: ${repo.htmlUrl}"
                    loadGitHubUserAndRepos()
                    withContext(Dispatchers.Main) {
                        onComplete(repo)
                    }
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                } finally {
                    _operationLogs.value = repoService.operationLogs
                    _isLoading.value = false
                }
            }
        }
    }

    fun createPullRequest(owner: String, repo: String, title: String, body: String, head: String, base: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val url = repoService.gitHubApiClient.createPullRequest(owner, repo, title, body, head, base)
                    _statusMessage.value = "Pull Request created: $url"
                    withContext(Dispatchers.Main) { onComplete(url) }
                } catch (e: Exception) {
                    _errorMessage.value = "Create PR failed: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun createRelease(owner: String, repo: String, tag: String, title: String, notes: String, isDraft: Boolean, isPrerelease: Boolean, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val url = repoService.gitHubApiClient.createRelease(owner, repo, tag, title, notes, isDraft, isPrerelease)
                    _statusMessage.value = "Release created: $url"
                    withContext(Dispatchers.Main) { onComplete(url) }
                } catch (e: Exception) {
                    _errorMessage.value = "Create Release failed: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearLogs() {
        repoService.clearLogs()
        _operationLogs.value = repoService.operationLogs
        _statusMessage.value = "Audit logs cleared"
    }

    fun saveGitignore(content: String) {
        val repo = repoService.activeRepository ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    repoService.safFileManager.writeGitignore(File(repo.path), content)
                    _statusMessage.value = ".gitignore saved"
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to save .gitignore: ${e.message}"
                }
            }
        }
    }

    fun getGitignoreContent(): String {
        val repo = repoService.activeRepository ?: return ""
        return repoService.safFileManager.readGitignore(File(repo.path))
    }

    fun getGitignorePreset(type: String): String {
        return repoService.safFileManager.getGitignorePreset(type)
    }

    fun getGitAuthorName(): String = repoService.keystoreManager.gitAuthorName
    fun getGitAuthorEmail(): String = repoService.keystoreManager.gitAuthorEmail
    fun isGitHubAuthenticated(): Boolean = repoService.keystoreManager.hasValidToken()
}
