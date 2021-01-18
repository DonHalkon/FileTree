package controllers.browsers

import models.FileModel
import models.wrappers.FileWrapper

/**
 * General interface for reading files and directories
 */
interface Browser {

    /**
     * Returns FileWrapper for given [path] or null if [path] does not exist
     */
    fun getPath(path: String): FileWrapper?

    /**
     * Returns string representation of given [path] parent or null if parent does not exist
     */
    fun getParentPath(path: String): String?

    /**
     * Returns Sequence of FileWrappers for [path]
     */
    suspend fun getPathContent(path: String): Sequence<FileWrapper>

    /**
     * Returns FileModel for [path]
     */
    suspend fun getFileContent(path: String): FileModel

    /**
     * Opens path using OS resources
     */
    fun openOnOS(path: FileWrapper)

    /**
     * Closes browser and releases resources
     */
    fun close()
}