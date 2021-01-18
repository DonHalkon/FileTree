package controllers.browsers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import models.FileModel
import models.utils.FTPConnectionData
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.fake.UserAccount
import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem
import java.io.IOException
import java.net.URL

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FTPBrowserTest {
    private val username = "user"
    private val password = "password"
    private val host = "localhost"
    private val fakeFtpServer: FakeFtpServer = FakeFtpServer()
    private val files = mapOf(Pair("/data/foobar.txt", "abcdef 1234567890"), Pair("/data/text.txt", "I am a text file"))

    @BeforeAll
    @Throws(IOException::class)
    fun setup() {
        fakeFtpServer.addUserAccount(UserAccount(username, password, "/"))
        val fileSystem: FileSystem = UnixFakeFileSystem()
        fileSystem.add(DirectoryEntry("/data"))
        for (file in files) {
            fileSystem.add(FileEntry(file.key, file.value))
        }
        fakeFtpServer.fileSystem = fileSystem
        fakeFtpServer.serverControlPort = 0
        fakeFtpServer.start()
    }

    @AfterAll
    @Throws(IOException::class)
    fun tearDown() {
        fakeFtpServer.stop()
    }

    @Test
    fun `establishes ftp connection`() {
        val connectionData = FTPConnectionData(URL("ftp://$host:${fakeFtpServer.serverControlPort}"), username, password)
        val ftpBrowser = FTPBrowser(connectionData)
        assertTrue(ftpBrowser.isConnected())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `gets list of files from directory`() = runBlockingTest {
        val connectionData = FTPConnectionData(URL("ftp://$host:${fakeFtpServer.serverControlPort}"), username, password)
        val ftpBrowser = FTPBrowser(connectionData)
        val ftpFileList = ftpBrowser.getPathContent("/data").map { it.getAbsolutePath() }.toList()
        assertTrue(files.map{it.key}.containsAll(ftpFileList))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `gets txt file content`() = runBlockingTest {
        val connectionData = FTPConnectionData(URL("ftp://$host:${fakeFtpServer.serverControlPort}"), username, password)
        val ftpBrowser = FTPBrowser(connectionData)
        val textFileModel = ftpBrowser.getFileContent("/data/text.txt") as FileModel.TextFileModel
        assertEquals("I am a text file", textFileModel.reader.readText())
    }
}