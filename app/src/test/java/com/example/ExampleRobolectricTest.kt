package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.GitEngine
import com.example.core.models.GitFileStatus
import com.example.core.models.LocalRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileWriter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RepoStudio", appName)
    }

    @Test
    fun `test GitEngine init, stage, commit, and branch`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val projectDir = tempFolder.newFolder("TestRepo")
        val gitEngine = GitEngine(context)

        assertFalse("Should not be git repo before init", gitEngine.isGitRepository(projectDir))

        // Initialize repository
        gitEngine.initRepository(projectDir)
        assertTrue("Should be git repo after init", gitEngine.isGitRepository(projectDir))
        val initialBranch = gitEngine.getCurrentBranch(projectDir)
        assertTrue(initialBranch == "main" || initialBranch == "master")

        // Create a test file
        val sampleFile = File(projectDir, "README.md")
        FileWriter(sampleFile).use { it.write("# Test Repo\nHello RepoStudio!") }

        // Check status - file should be untracked
        val statusList = gitEngine.getStatus(projectDir)
        assertEquals(1, statusList.size)
        assertEquals("README.md", statusList[0].relativePath)
        assertEquals(GitFileStatus.ChangeType.UNTRACKED, statusList[0].changeType)
        assertFalse(statusList[0].isStaged)

        // Stage all
        gitEngine.stageAll(projectDir)
        val stagedStatus = gitEngine.getStatus(projectDir)
        assertEquals(1, stagedStatus.size)
        assertTrue(stagedStatus[0].isStaged)

        // Commit
        val commitInfo = gitEngine.commit(projectDir, "Initial commit", "Test Developer", "dev@example.com")
        assertNotNull(commitInfo)
        assertEquals("Initial commit", commitInfo.message)
        assertEquals("Test Developer", commitInfo.authorName)

        // Check history
        val history = gitEngine.getCommitHistory(projectDir, 10)
        assertEquals(1, history.size)
        assertEquals(commitInfo.shortId, history[0].shortId)

        // Check status after commit - should be clean
        val cleanStatus = gitEngine.getStatus(projectDir)
        assertTrue("Status should be clean after commit", cleanStatus.isEmpty())

        // Branch creation & checkout
        gitEngine.createBranch(projectDir, "feature-mobile")
        val branches = gitEngine.getBranches(projectDir)
        assertTrue(branches.contains("feature-mobile"))

        gitEngine.checkoutBranch(projectDir, "feature-mobile")
        assertEquals("feature-mobile", gitEngine.getCurrentBranch(projectDir))
    }

    @Test
    fun `test folder name resolution and in-place repository`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val safFileManager = com.example.core.SafFileManager(context)

        // Test extracting folder name from URI
        val testUri = android.net.Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FMyCoolApp")
        val folderName = safFileManager.extractFolderName(testUri)
        assertEquals("MyCoolApp", folderName)

        // Test that external folder paths are maintained with their real name
        val externalFolder = tempFolder.newFolder("WebPortfolio")
        val gitEngine = GitEngine(context)
        gitEngine.initRepository(externalFolder)

        val keystoreManager = com.example.core.KeystoreManager(context)
        keystoreManager.addSavedExternalRepoPath(externalFolder.absolutePath)

        val repoService = com.example.core.RepositoryService.getInstance(context)
        val allRepos = repoService.allLocalRepositories
        val matched = allRepos.find { it.path == externalFolder.absolutePath }
        assertNotNull("External folder should be listed in repositories", matched)
        assertEquals("WebPortfolio", matched?.name)
        assertTrue("Should detect git in external folder", matched?.isGitInitialized == true)
    }
}
