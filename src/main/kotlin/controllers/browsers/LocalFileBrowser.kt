package controllers.browsers

import controllers.files.openFile
import models.FileModel
import models.wrappers.FileWrapper
import models.wrappers.LocalFileWrapper
import java.awt.Desktop
import java.io.File


/**
 * Browser implementation for working with local files
 */
class LocalFileBrowser : Browser {

    override suspend fun getPathContent(path: String) = sequence<FileWrapper> {
        val directory = File(path)
        directory.walk()
            .maxDepth(1)
            .drop(1)
            .sorted()
            .forEach { yield(LocalFileWrapper(it)) }
    }

    override fun getPath(path: String): FileWrapper? {
        val file = File(path)
        return if (file.exists()) LocalFileWrapper(file) else null
    }

    override fun getParentPath(path: String): String? {
        val directory = File(path)
        return if (directory.parent != null) directory.parentFile.absolutePath else null
    }

    override suspend fun getFileContent(path: String): FileModel {
        return openFile(File(path))
    }

    override fun openOnOS(path: FileWrapper) {
        Desktop.getDesktop().edit(File(path.getAbsolutePath()))
    }

    override fun close() {
        // nothing to do
    }
}
