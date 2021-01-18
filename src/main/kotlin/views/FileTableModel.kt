package views

import models.wrappers.FileWrapper
import java.lang.IllegalArgumentException
import java.text.DecimalFormat
import java.util.*
import javax.swing.ImageIcon
import javax.swing.table.AbstractTableModel
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.math.log10
import kotlin.math.pow

/**
 * AbstractTableModel implementation to display table of files
 */
internal class FileTableModel constructor(private var fileNodes: Array<DefaultMutableTreeNode>) : AbstractTableModel() {
    private val columns = arrayOf(
        "Icon",
        "File",
        "Absolute Path",
        "Size",
        "Last Modified"
    )

    override fun getValueAt(row: Int, column: Int): Any {
        val file = fileNodes[row].userObject as FileWrapper
        return when (column) {
            0 -> file.getIcon()
            1 -> file.getName()
            2 -> file.getAbsolutePath()
            3 -> readableFileSize(file.size())
            4 -> file.lastModified()
            else -> throw IllegalArgumentException("Column with index $column does not exist")
        }
    }

    override fun getColumnCount(): Int {
        return columns.size
    }

    override fun getColumnClass(column: Int): Class<*> {
        when (column) {
            0 -> return ImageIcon::class.java
            4 -> return Date::class.java
        }
        return String::class.java
    }

    override fun getColumnName(column: Int): String {
        return columns[column]
    }

    override fun getRowCount(): Int {
        return fileNodes.size
    }

    fun getFileNodeByIndex(index: Int): DefaultMutableTreeNode {
        return fileNodes[index]
    }

    fun setFileNodes(files: Array<DefaultMutableTreeNode>) {
        this.fileNodes = files
        fireTableDataChanged()
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return "${DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))} ${units[digitGroups]}"
    }
}