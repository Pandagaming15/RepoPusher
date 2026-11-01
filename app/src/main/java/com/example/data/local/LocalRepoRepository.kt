package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository pattern implementation abstracting Room database operations for local repository paths.
 */
class LocalRepoRepository(private val dao: LocalRepoDao) {

    val allRepositories: Flow<List<LocalRepoEntity>> = dao.getAllRepositories()

    fun searchRepositories(query: String): Flow<List<LocalRepoEntity>> {
        return dao.searchRepositories(query)
    }

    suspend fun getRepositoryByPath(path: String): LocalRepoEntity? = withContext(Dispatchers.IO) {
        dao.getRepositoryByPath(path)
    }

    suspend fun getRepositoryById(id: Long): LocalRepoEntity? = withContext(Dispatchers.IO) {
        dao.getRepositoryById(id)
    }

    suspend fun insertOrUpdate(
        path: String,
        name: String = File(path).name,
        remoteUrl: String = "",
        branch: String = "main",
        safTreeUri: String = "",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val file = File(path)
        val gitDir = File(file, ".git")
        val isGit = gitDir.exists() && gitDir.isDirectory

        val existing = dao.getRepositoryByPath(path)
        val entity = if (existing != null) {
            existing.copy(
                name = if (name.isNotBlank()) name else existing.name,
                remoteUrl = if (remoteUrl.isNotBlank()) remoteUrl else existing.remoteUrl,
                branch = if (branch.isNotBlank()) branch else existing.branch,
                isGitRepo = isGit || existing.isGitRepo,
                lastAccessedAt = System.currentTimeMillis(),
                safTreeUri = if (safTreeUri.isNotBlank()) safTreeUri else existing.safTreeUri,
                notes = if (notes.isNotBlank()) notes else existing.notes
            )
        } else {
            LocalRepoEntity(
                name = if (name.isNotBlank()) name else file.name,
                path = path,
                remoteUrl = remoteUrl,
                branch = branch,
                isGitRepo = isGit,
                createdAt = System.currentTimeMillis(),
                lastAccessedAt = System.currentTimeMillis(),
                safTreeUri = safTreeUri,
                notes = notes
            )
        }
        dao.insert(entity)
    }

    suspend fun insert(repo: LocalRepoEntity): Long = withContext(Dispatchers.IO) {
        dao.insert(repo)
    }

    suspend fun update(repo: LocalRepoEntity) = withContext(Dispatchers.IO) {
        dao.update(repo)
    }

    suspend fun delete(repo: LocalRepoEntity) = withContext(Dispatchers.IO) {
        dao.delete(repo)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun deleteByPath(path: String) = withContext(Dispatchers.IO) {
        dao.deleteByPath(path)
    }

    suspend fun updateLastAccessed(path: String) = withContext(Dispatchers.IO) {
        dao.updateLastAccessed(path, System.currentTimeMillis())
    }

    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        dao.getCount()
    }
}
