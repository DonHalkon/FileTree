package models.wrappers

import java.io.File

/**
 * FileWrapper implementation for local files
 */
class LocalFileWrapper(private val path: File) : FileWrapper {

    override fun getName(): String {
        return path.name
    }

    override fun isDirectory(): Boolean {
        return path.isDirectory
    }

    override fun isFile(): Boolean {
        return path.isFile
    }

    override fun getAbsolutePath(): String {
        return path.absolutePath.toString()
    }

    override fun isHidden(): Boolean {
        return path.isHidden
    }

    override fun size(): Long {
        return path.length()
    }

    override fun lastModified(): Long {
        return path.lastModified()
    }

    override fun getFileContentPath(): String {
        return path.absolutePath
    }

    override fun setFileContentPath(path: String) {
        // nothing to do
        // could be used for file links
    }

    override fun toString(): String {
        return path.name
    }
}