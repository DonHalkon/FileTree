package controllers.browsers

import controllers.files.openFile
import models.*
import models.utils.FTPConnectionData
import models.wrappers.FTPFileWrapper
import models.wrappers.FileWrapper
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPConnectionClosedException
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files

/**
 * Browser implementation for working with FTP servers
 */
class FTPBrowser(ftpConnectionData: FTPConnectionData) : Browser {
    private val ftpClient = FTPClient()

    init {
        val port = if (ftpConnectionData.url.port > 0) ftpConnectionData.url.port else ftpClient.defaultPort
        ftpClient.connect(ftpConnectionData.url.host, port)
        val reply = ftpClient.replyCode
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect()
            throw IOException("Exception in connecting to FTP Server")
        }
        try {
            ftpClient.login(ftpConnectionData.username, ftpConnectionData.password)
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
            ftpClient.keepAlive = true
        } catch (ex: IOException) {
            ftpClient.disconnect()
            throw ex
        }
    }

    override fun getPath(path: String): FileWrapper {
        // always start from root directory
        val ftpFile = FTPFile()
        ftpFile.name = ""
        ftpFile.type = 1
        return FTPFileWrapper(ftpFile, "")
    }

    override suspend fun getPathContent(path: String) = sequence<FileWrapper> {
        ftpClient.listFiles(path).forEach {
            try {
                yield(FTPFileWrapper(it, "$path/${it.name}"))
            } catch (ex: FTPConnectionClosedException) {
                FileModel.ErrorFileModel("Can not open file '$path'.<br>Error message: FTP connection closed")
            }
        }
    }

    override fun getParentPath(path: String): String? {
        // always start from root directory
        return null
    }

    override suspend fun getFileContent(path: String): FileModel {
        val file = File(path)
        if (file.exists()) {
            return openFile(file)
        }

        val localCopy = Files.createTempFile("local_copy_of", file.name).toFile()
        return try {
            localCopy.deleteOnExit()
            val fileOutputStream = FileOutputStream(localCopy)
            ftpClient.retrieveFile(path, fileOutputStream)
            fileOutputStream.close()
            openFile(localCopy)
        } catch (ex: IOException) {
            localCopy.delete()
            FileModel.ErrorFileModel("Can not open file '$path'.<br>Error message:${ex.message}")
        } catch (ex: NullPointerException) {
            localCopy.delete()
            FileModel.ErrorFileModel("Can not open file '$path'.<br>Error message: Connection can not be opened")
        } catch (ex: FTPConnectionClosedException) {
            localCopy.delete()
            FileModel.ErrorFileModel("Can not open file '$path'.<br>Error message: FTP connection closed")
        }
    }

    override fun openOnOS(path: FileWrapper) {
        val file = path as FTPFileWrapper
        if (file.getAbsolutePath() != file.getFileContentPath()) {
            Desktop.getDesktop().edit(File(file.getFileContentPath()))
        }
    }

    override fun close() {
        if (ftpClient.isConnected) {
            ftpClient.disconnect()
        }
    }

    fun isConnected(): Boolean {
        return ftpClient.isConnected
    }
}
