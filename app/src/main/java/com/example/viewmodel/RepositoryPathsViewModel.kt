package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LocalRepoEntity
import com.example.data.local.LocalRepoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class RepositoryPathsUiState(
    val repositories: List<LocalRepoEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedRepo: LocalRepoEntity? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class RepositoryPathsViewModel(
    application: Application,
    private val repository: LocalRepoRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        LocalRepoRepository(AppDatabase.getDatabase(application).localRepoDao())
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedRepo = MutableStateFlow<LocalRepoEntity?>(null)
    val selectedRepo: StateFlow<LocalRepoEntity?> = _selectedRepo.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val allRepositories: StateFlow<List<LocalRepoEntity>> = repository.allRepositories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<RepositoryPathsUiState> = combine(
        allRepositories,
        _searchQuery,
        _isLoading,
        _statusMessage,
        _errorMessage
    ) { repos, query, loading, status, error ->
        val filtered = if (query.isBlank()) {
            repos
        } else {
            repos.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.path.contains(query, ignoreCase = true) ||
                        it.remoteUrl.contains(query, ignoreCase = true) ||
                        it.branch.contains(query, ignoreCase = true)
            }
        }
        RepositoryPathsUiState(
            repositories = filtered,
            searchQuery = query,
            isLoading = loading,
            selectedRepo = _selectedRepo.value,
            statusMessage = status,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RepositoryPathsUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectRepository(repo: LocalRepoEntity?) {
        _selectedRepo.value = repo
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    fun addRepositoryPath(
        path: String,
        name: String = "",
        remoteUrl: String = "",
        branch: String = "main",
        safTreeUri: String = "",
        notes: String = ""
    ) {
        val trimmedPath = path.trim()
        if (trimmedPath.isEmpty()) {
            _errorMessage.value = "Repository path cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val file = File(trimmedPath)
                val resolvedName = if (name.isNotBlank()) name.trim() else file.name.ifEmpty { "Repository" }
                val gitDir = File(file, ".git")
                val isGit = gitDir.exists()

                repository.insertOrUpdate(
                    path = trimmedPath,
                    name = resolvedName,
                    remoteUrl = remoteUrl.trim(),
                    branch = if (branch.isNotBlank()) branch.trim() else "main",
                    safTreeUri = safTreeUri.trim(),
                    notes = notes.trim()
                )
                _statusMessage.value = "Saved '$resolvedName' to database"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save repository path: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRepository(repo: LocalRepoEntity) {
        viewModelScope.launch {
            try {
                repository.delete(repo)
                if (_selectedRepo.value?.id == repo.id) {
                    _selectedRepo.value = null
                }
                _statusMessage.value = "Removed '${repo.name}' from saved paths"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteById(id)
                _statusMessage.value = "Removed repository path"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun updateRepository(repo: LocalRepoEntity) {
        viewModelScope.launch {
            try {
                repository.update(repo)
                _statusMessage.value = "Updated '${repo.name}'"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
            }
        }
    }

    fun checkAndUpdateStatus(repo: LocalRepoEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dir = File(repo.path)
                val exists = dir.exists() && dir.isDirectory
                val isGit = File(dir, ".git").exists()
                val updated = repo.copy(
                    isGitRepo = isGit,
                    lastAccessedAt = System.currentTimeMillis()
                )
                repository.update(updated)
            }
            _statusMessage.value = "Refreshed status for '${repo.name}'"
        }
    }
}
