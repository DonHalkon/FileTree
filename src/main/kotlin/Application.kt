import controllers.browsers.Browser
import controllers.browsers.FTPBrowser
import controllers.browsers.LocalFileBrowser
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import models.*
import models.utils.FTPConnectionData
import models.wrappers.FTPFileWrapper
import models.wrappers.FileWrapper
import models.wrappers.LocalFileWrapper
import views.*
import views.FileTableModel
import views.FileTreeCellRenderer
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.*

import javax.swing.JTree

import java.io.File
import java.io.IOException
import java.io.Reader

import javax.swing.JPanel
import javax.swing.JSplitPane.HORIZONTAL_SPLIT
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.JOptionPane
import javax.swing.event.DocumentEvent

import javax.swing.event.DocumentListener
import javax.swing.JLabel
import javax.swing.tree.TreePath


class Application {

    private val frame = MyFrame()

    // nav panel
    private val navPanel = JPanel()
    private val upButton = JButton("Up")
    private val homeButton = JButton("Home")
    private val extensionFilter = JTextField()
    private val showHidden = JCheckBox("Show hidden")
    private val currentPathLabel = JLabel()

    // file tree
    private val treeModel: DefaultTreeModel
    private val tree: JTree
    private val treeScrollPanel: JScrollPane

    // preview
    private val fileListTable = JTable()
    private val fileListPreview = JScrollPane(fileListTable)
    private val fileTableModel = FileTableModel(emptyArray())

    // main view
    private val mainView: JSplitPane

    // backend
    private var browser: Browser
    private var previewJob: Job? = null

    init {
        initMenu()
        initNavigationPanel()
        browser = LocalFileBrowser()
        // init tree
        val root = DefaultMutableTreeNode(LocalFileWrapper(File("stub")))
        treeModel = DefaultTreeModel(root)
        tree = JTree(treeModel)
        treeScrollPanel = JScrollPane(tree)
        initFileTree()

        mainView = JSplitPane(HORIZONTAL_SPLIT, treeScrollPanel, fileListPreview)
        mainView.resizeWeight = 0.5
        frame.contentPane.add(mainView)
        frame.contentPane.add(navPanel, BorderLayout.NORTH)
    }

    /**
     *  Sets gif while preview been loading
     */
    private fun loadingView() {
        val icon = Application::class.java.getResource("loading_200px.gif")
        val imageIcon = ImageIcon(icon)
        mainView.rightComponent = JLabel(imageIcon)
    }

    /**
     * Shows directory preview with details in the table
     */
    private fun previewDirectory(node: DefaultMutableTreeNode) {
        val children = node.children()
            .toList()
            .filter { treeNode ->
                val file = (treeNode as DefaultMutableTreeNode).userObject as FileWrapper
                (showHidden.isSelected || !file.isHidden()
                        && file.getName().substringAfterLast(".", "").contains(getExtensionFilter()))
            }
            .map { treeNode -> treeNode as DefaultMutableTreeNode }
            .toTypedArray()
        fileTableModel.setFileNodes(children)
        setColumnWidth(0, -1)
        setColumnWidth(3, 60)
        fileListTable.columnModel.getColumn(3).maxWidth = 120
        setColumnWidth(4, -1)
        mainView.rightComponent = fileListPreview
    }

    /**
     * Defines file type and shows file content
     */
    private suspend fun viewFile(fileModel: FileModel) {
        val preview = when (fileModel) {
            is FileModel.TextFileModel -> textFileView(fileModel.reader)
            is FileModel.ZipFileModel -> zipFileView(fileModel.text)
            is FileModel.ImageFileModel -> imageFileView(fileModel.image)
            is FileModel.ErrorFileModel -> errorFileView(fileModel.message)
            else -> errorFileView("Unknown error") // Just in case
        }
        mainView.rightComponent = preview
    }

    /**
     * Displays zip file content
     */
    private fun zipFileView(text: String): JComponent {
        val jTextArea = JTextArea(text)
        val jPanel = JScrollPane(jTextArea)
        jTextArea.isEditable = false
        return jPanel
    }


    /**
     * Displays image file
     */
    private fun imageFileView(image: BufferedImage): JComponent {
        val icon = ImageIcon(image)
        val label = JLabel()
        label.icon = icon
        return JScrollPane(label)
    }


    /**
     * Loads and displays text file content
     */
    private suspend fun textFileView(reader: Reader): JComponent {
        val jTextArea = JTextArea()
        val jScrollPane = JScrollPane(jTextArea)
        withContext(IO) {
            // see https://youtrack.jetbrains.com/issue/KTIJ-838
            jTextArea.read(reader, null)
        }
        jTextArea.isEditable = false
        return jScrollPane
    }


    /**
     * Displays message with error during file reading
     */
    private fun errorFileView(errorMessage: String): JComponent {
        val errorText = JLabel("<html><font color=\"red\">$errorMessage</font></html>")
        val jPanel = JPanel(GridBagLayout())
        jPanel.add(errorText)
        return jPanel
    }

    /**
     * Adds children to all children for file tree node
     */
    private suspend fun expandNode(node: DefaultMutableTreeNode) {
        for (child in node.children()) {
            addChildren(child as DefaultMutableTreeNode)
        }
        tree.treeDidChange()
    }

    /**
     * Checks if file tree node has children and if not loads them
     */
    private suspend fun addChildren(node: DefaultMutableTreeNode) {
        if (node.children() == DefaultMutableTreeNode.EMPTY_ENUMERATION) {
            val fileWrapper = node.userObject as FileWrapper
            if (fileWrapper.isDirectory()) {
                val directory = fileWrapper.getAbsolutePath()
                val directoryContent = browser.getPathContent(directory)
                directoryContent.forEach { path ->
                    if (showHidden.isSelected || !path.isHidden()) {
                        val dir = DefaultMutableTreeNode(path)
                        node.add(dir)
                    }
                }
            }
        }
    }

    /**
     * Opens directory and adds it file tree
     */
    private fun openDirectory(directory: String) {
        GlobalScope.launch(IO) {
            val fileWrapper = browser.getPath(directory)
            val newRoot = DefaultMutableTreeNode(fileWrapper)
            treeModel.setRoot(newRoot)
            addChildren(newRoot)
            expandNode(newRoot)
        }
    }

    /**
     * Tris to open FTP connection and shows error message if failed
     */
    private fun openFTPConnection(ftpConnectionData: FTPConnectionData): Boolean {
        try {
            val ftpBrowser = FTPBrowser(ftpConnectionData)
            if (ftpBrowser.isConnected()) {
                browser.close()
                browser = ftpBrowser
                return true
            }
        } catch (ex: IOException) {
            JOptionPane.showMessageDialog(
                frame,
                ex.message,
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
        return false
    }

    /**
     * Returns to user home directory
     */
    private fun goHome() {
        openDirectory(System.getProperty("user.home"))
    }

    /**
     * Adjusts column width
     */
    private fun setColumnWidth(column: Int, width: Int) {
        var newWidth = width
        val tableColumn = fileListTable.columnModel.getColumn(column)
        if (newWidth < 0) {
            val label = JLabel(tableColumn.headerValue as String)
            val preferred = label.preferredSize
            newWidth = preferred.getWidth().toInt() + 10
        }
        tableColumn.preferredWidth = newWidth
        tableColumn.maxWidth = newWidth
        tableColumn.minWidth = newWidth
    }

    private fun getExtensionFilter(): String {
        val text = extensionFilter.text
        return text.removePrefix(".")
    }

    private fun initMenu() {
        val ftpConnectionActionListener = ActionListener {
            val connectionData = FTPLoginDialog(frame).getConnectionData()
            if (connectionData != null) {
                val isConnected = openFTPConnection(connectionData)
                if (isConnected) {
                    openDirectory(connectionData.url.path)
                }
            }
        }

        val myMenuBar = MyMenuBar(ftpConnectionActionListener)
        frame.jMenuBar = myMenuBar
    }

    private fun initNavigationPanel() {
        // init UpButton
        upButton.addActionListener {
            val root = treeModel.root as DefaultMutableTreeNode
            val rootFile = root.userObject as FileWrapper
            val parent = browser.getParentPath(rootFile.getAbsolutePath())
            if (parent != null) openDirectory(parent)
        }
        upButton.icon = UIManager.getIcon("FileChooser.upFolderIcon")

        // init Home Button
        homeButton.addActionListener {
            if (browser is FTPBrowser) {
                browser.close()
                browser = LocalFileBrowser()
            }
            goHome()
        }
        homeButton.text = "Home"
        homeButton.icon = UIManager.getIcon("FileChooser.homeFolderIcon")

        showHidden.addActionListener {
            val rootNode = treeModel.root as DefaultMutableTreeNode
            val rootFileWrapper = rootNode.userObject as FileWrapper
            openDirectory(rootFileWrapper.getAbsolutePath())
        }

        // init Extension Filter
        extensionFilter.columns = 8
        extensionFilter.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {/*no action*/
            }

            override fun removeUpdate(e: DocumentEvent?) {
                val node = tree.selectionPath?.lastPathComponent as DefaultMutableTreeNode
                previewDirectory(node)
            }

            override fun insertUpdate(e: DocumentEvent?) {
                val node = tree.selectionPath?.lastPathComponent as DefaultMutableTreeNode
                previewDirectory(node)
            }

        })
        val extensionFilterLabel = JLabel("Extension filter:")
        extensionFilterLabel.labelFor = extensionFilter

        currentPathLabel.preferredSize = Dimension(300, 15)
        navPanel.add(homeButton)
        navPanel.add(upButton)
        navPanel.add(showHidden)
        navPanel.add(JSeparator(SwingConstants.VERTICAL))
        navPanel.add(extensionFilterLabel)
        navPanel.add(extensionFilter)
        navPanel.add(JSeparator(SwingConstants.VERTICAL))
        navPanel.add(currentPathLabel)
        navPanel.layout = FlowLayout(FlowLayout.LEFT)
        frame.contentPane.add(navPanel, BorderLayout.NORTH)
    }

    private fun initFileTree() {
        tree.cellRenderer = FileTreeCellRenderer()
        fileListTable.model = fileTableModel
        tree.addTreeSelectionListener { tse ->
            val node = tse.path.lastPathComponent as DefaultMutableTreeNode
            val file = node.userObject as FileWrapper
            if (node.children() != DefaultMutableTreeNode.EMPTY_ENUMERATION) {
                if (file.isDirectory()) {
                    previewJob?.cancel()
                    loadingView()
                    previewJob = GlobalScope.launch(IO) {
                        addChildren(node)
                        previewDirectory(node)
                    }
                }

            } else {
                if (file.isFile()) {
                    previewJob?.cancel()
                    previewJob = GlobalScope.launch(IO) {
                        loadingView()
                        val fileModel = browser.getFileContent(file.getFileContentPath())
                        if (fileModel !is FileModel.ErrorFileModel) file.setFileContentPath(fileModel.path)
                        viewFile(fileModel)
                    }
                } else {
                    mainView.rightComponent = JPanel()
                }
            }
            currentPathLabel.text = file.getAbsolutePath()
        }

        tree.addTreeExpansionListener(
            object : TreeExpansionListener {
                override fun treeExpanded(event: TreeExpansionEvent) {
                    val node = event.path.lastPathComponent as DefaultMutableTreeNode
                    GlobalScope.launch {
                        expandNode(node)
                    }
                }

                override fun treeCollapsed(event: TreeExpansionEvent?) { /* nothing to do */
                }
            }
        )
        tree.showsRootHandles = true
        treeScrollPanel.setViewportView(tree)
        goHome()

        fileListTable.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedRow = fileListTable.selectedRow
                    val fileNode = fileTableModel.getFileNodeByIndex(selectedRow)
                    val file = fileNode.userObject as FileWrapper
                    val nodes = treeModel.getPathToRoot(fileNode)
                    val path = TreePath(nodes)
                    tree.scrollPathToVisible(path)
                    tree.selectionPath = path

                    if (file.isFile()) {
                        browser.openOnOS(file)
                    }
                }
            }
        })
    }
}
