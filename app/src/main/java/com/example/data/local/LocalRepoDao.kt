package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalRepoDao {

    @Query("SELECT * FROM local_repository_paths ORDER BY last_accessed_at DESC")
    fun getAllRepositories(): Flow<List<LocalRepoEntity>>

    @Query("SELECT * FROM local_repository_paths WHERE name LIKE '%' || :query || '%' OR path LIKE '%' || :query || '%' OR remote_url LIKE '%' || :query || '%' ORDER BY last_accessed_at DESC")
    fun searchRepositories(query: String): Flow<List<LocalRepoEntity>>

    @Query("SELECT * FROM local_repository_paths WHERE path = :path LIMIT 1")
    suspend fun getRepositoryByPath(path: String): LocalRepoEntity?

    @Query("SELECT * FROM local_repository_paths WHERE id = :id LIMIT 1")
    suspend fun getRepositoryById(id: Long): LocalRepoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repo: LocalRepoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<LocalRepoEntity>)

    @Update
    suspend fun update(repo: LocalRepoEntity)

    @Delete
    suspend fun delete(repo: LocalRepoEntity)

    @Query("DELETE FROM local_repository_paths WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM local_repository_paths WHERE path = :path")
    suspend fun deleteByPath(path: String): Int

    @Query("UPDATE local_repository_paths SET last_accessed_at = :timestamp WHERE path = :path")
    suspend fun updateLastAccessed(path: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM local_repository_paths")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM local_repository_paths")
    suspend fun getCount(): Int
}
