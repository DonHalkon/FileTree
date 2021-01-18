package models.wrappers

import org.apache.commons.net.ftp.FTPFile

/**
 * FileWrapper implementation for FTP files
 */
class FTPFileWrapper(private val ftpFile: FTPFile, private val absolutePath: String): FileWrapper {

    private var localCopyPath: String = ""

    override fun isDirectory() = ftpFile.isDirectory

    override fun isFile() = ftpFile.isFile

    override fun getAbsolutePath(): String {
        return absolutePath
    }

    override fun getName(): String = ftpFile.name

    override fun isHidden(): Boolean {
        return false
    }

    override fun size(): Long {
        return ftpFile.size
    }

    override fun lastModified(): Long {
        return ftpFile.timestamp.timeInMillis
    }

    override fun getFileContentPath(): String {
        return if (localCopyPath != "") localCopyPath else absolutePath
    }

    override fun setFileContentPath(path: String) {
        localCopyPath = path
    }

    override fun toString(): String {
        return if (ftpFile.name != null) ftpFile.name else ""
    }

}