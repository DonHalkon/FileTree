package controllers.browsers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.filechooser.FileSystemView

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalFileBrowserTest {

    private val localFileBrowser = LocalFileBrowser()
    private val testDirectoryString = "src/test/resources"
    private val resourcesDirectory = Paths.get(testDirectoryString)

    @Test
    fun `gets existing path`() {
        val result = localFileBrowser.getPath(testDirectoryString)
        assertNotNull(result)
    }

    @Test
    fun `gets not existing path`() {
        val result = localFileBrowser.getPath("I do not exist")
        assertNull(result)
    }


    @Test
    fun `gets parent for existing path`() {
        val result = localFileBrowser.getParentPath(testDirectoryString)
        assertEquals(resourcesDirectory.parent.toAbsolutePath().toString(), result)
    }

    @Test
    fun `gets parent for non existing path`() {
        val result = localFileBrowser.getParentPath("I do not exist")
        assertNull(result)
    }

    @Test
    fun `gets parent for root path`() {
        val result = localFileBrowser.getParentPath(FileSystemView.getFileSystemView().roots[0].toString())
        assertNull(result)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun `gets directory content`() = runBlockingTest {
        val result = localFileBrowser.getPathContent(testDirectoryString).map { it.getAbsolutePath() }.toList()
        val expectedResult = resourcesDirectory.toFile().walk().map { it.absolutePath }.toList()
        assert(expectedResult.containsAll(result))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `gets empty directory content`() = runBlockingTest {
        val result = localFileBrowser.getPathContent("$testDirectoryString/empty-directory").toList()
        assertTrue(result.isEmpty())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `gets non existing directory content`() = runBlockingTest {
        val result = localFileBrowser.getPathContent("I do not exist").toList()
        assert(result.isEmpty())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `does not get content from existing directory without permissions`() = runBlockingTest {
        val directory = Files.createTempDirectory("test")
        val file = Files.createTempFile(directory, "test", "test")
        file.toFile().deleteOnExit()
        directory.toFile().deleteOnExit()
        directory.toFile().setReadable(false)
        val result = localFileBrowser.getPathContent(directory.toAbsolutePath().toString()).toList()
        assert(result.isEmpty())
    }
}