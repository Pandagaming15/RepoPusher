package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_repository_paths",
    indices = [
        Index(value = ["path"], unique = true)
    ]
)
data class LocalRepoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "path")
    val path: String,

    @ColumnInfo(name = "remote_url")
    val remoteUrl: String = "",

    @ColumnInfo(name = "branch")
    val branch: String = "main",

    @ColumnInfo(name = "is_git_repo")
    val isGitRepo: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "saf_tree_uri")
    val safTreeUri: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = ""
)
