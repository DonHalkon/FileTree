package models.wrappers

import javax.swing.Icon
import javax.swing.UIManager

/**
 * Wrapper interface for various file types
 */
interface FileWrapper {
    fun isDirectory(): Boolean
    fun isFile(): Boolean
    fun getAbsolutePath(): String
    fun getName(): String
    fun isHidden(): Boolean
    fun size(): Long
    fun lastModified(): Long
    fun getFileContentPath(): String
    fun setFileContentPath(path: String)

    fun getIcon(): Icon {
        return if (isDirectory()) {
            UIManager.getIcon("FileView.directoryIcon")
        } else {
            UIManager.getIcon("FileView.fileIcon")
        }
    }


}