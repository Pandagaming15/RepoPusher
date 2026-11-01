package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.LocalRepoDao
import com.example.data.local.LocalRepoEntity
import com.example.data.local.LocalRepoRepository
import com.example.viewmodel.RepositoryPathsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var db: AppDatabase
    private lateinit var dao: LocalRepoDao
    private lateinit var repository: LocalRepoRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.localRepoDao()
        repository = LocalRepoRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `test Room schema insertion, retrieval, and Flow updates`() = runBlocking {
        val repo = LocalRepoEntity(
            name = "TestProject",
            path = "/storage/emulated/0/Projects/TestProject",
            remoteUrl = "https://github.com/developer/TestProject.git",
            branch = "main",
            isGitRepo = true
        )

        val id = dao.insert(repo)
        assertTrue("Inserted row ID should be greater than 0", id > 0)

        // Query by path
        val retrieved = dao.getRepositoryByPath("/storage/emulated/0/Projects/TestProject")
        assertNotNull(retrieved)
        assertEquals("TestProject", retrieved?.name)
        assertEquals("https://github.com/developer/TestProject.git", retrieved?.remoteUrl)
        assertEquals("main", retrieved?.branch)
        assertTrue(retrieved?.isGitRepo == true)

        // Flow query
        val list = dao.getAllRepositories().first()
        assertEquals(1, list.size)
        assertEquals("TestProject", list[0].name)
    }

    @Test
    fun `test search and deletion in Room DAO`() = runBlocking {
        dao.insert(LocalRepoEntity(name = "AppOne", path = "/sdcard/AppOne", branch = "main"))
        dao.insert(LocalRepoEntity(name = "AppTwo", path = "/sdcard/AppTwo", branch = "dev"))
        dao.insert(LocalRepoEntity(name = "BackendApi", path = "/sdcard/BackendApi", branch = "master"))

        assertEquals(3, dao.getCount())

        // Search query
        val searchResults = dao.searchRepositories("App").first()
        assertEquals(2, searchResults.size)

        // Delete by path
        val deletedCount = dao.deleteByPath("/sdcard/AppOne")
        assertEquals(1, deletedCount)
        assertEquals(2, dao.getCount())

        val remaining = dao.getAllRepositories().first()
        assertFalse(remaining.any { it.name == "AppOne" })
    }

    @Test
    fun `test LocalRepoRepository insertOrUpdate`() = runBlocking {
        val folder = tempFolder.newFolder("MyRepo")
        val id1 = repository.insertOrUpdate(
            path = folder.absolutePath,
            name = "MyRepo",
            remoteUrl = "https://github.com/test/myrepo.git",
            branch = "main"
        )
        assertTrue(id1 > 0)

        // Update with new remote
        repository.insertOrUpdate(
            path = folder.absolutePath,
            remoteUrl = "https://github.com/test/newremote.git"
        )

        val updated = repository.getRepositoryByPath(folder.absolutePath)
        assertNotNull(updated)
        assertEquals("https://github.com/test/newremote.git", updated?.remoteUrl)
    }

    @Test
    fun `test RepositoryPathsViewModel operations`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = RepositoryPathsViewModel(app, repository)

        val projectDir = tempFolder.newFolder("SampleProject")
        repository.insertOrUpdate(
            path = projectDir.absolutePath,
            name = "SampleProject",
            remoteUrl = "https://github.com/example/sample.git",
            branch = "main",
            notes = "Work project"
        )

        val list = repository.allRepositories.first()
        assertEquals(1, list.size)
        assertEquals("SampleProject", list[0].name)
        assertEquals(projectDir.absolutePath, list[0].path)

        // Test search query filtering
        viewModel.setSearchQuery("Sample")
        assertEquals("Sample", viewModel.searchQuery.value)

        viewModel.setSearchQuery("")
        assertEquals("", viewModel.searchQuery.value)
    }
}
