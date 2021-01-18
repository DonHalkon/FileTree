package views

import models.wrappers.FileWrapper
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * DefaultTreeCellRenderer extension for file tree nodes rendering
 */
class FileTreeCellRenderer : DefaultTreeCellRenderer() {
    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        val jLabel = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel
        val node = value as DefaultMutableTreeNode
        val file = node.userObject as FileWrapper
        jLabel.transferHandler
        jLabel.icon = file.getIcon()
        if (file.isHidden()) {
            jLabel.text = "<html><font color='gray'>$text</font></html>"
        } else {
            jLabel.text = file.getName()
        }
        jLabel.toolTipText = file.getAbsolutePath()
        return jLabel
    }

}