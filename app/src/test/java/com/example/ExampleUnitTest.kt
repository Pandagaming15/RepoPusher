package com.example

import com.example.core.models.LocalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLocalRepositoryModel() {
        val repo = LocalRepository("TestProject", "/storage/emulated/0/TestProject", "https://github.com/user/test.git", "main", true)
        assertEquals("TestProject", repo.name)
        assertEquals("main", repo.currentBranch)
        assertTrue(repo.isGitInitialized)
        assertEquals("https://github.com/user/test.git", repo.remoteUrl)
    }
}
