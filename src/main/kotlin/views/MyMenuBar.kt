package views

import java.awt.event.ActionListener
import javax.swing.*
import kotlin.system.exitProcess

class MyMenuBar(ftpConnectionActionListener: ActionListener) : JMenuBar() {
    init {
        val menu = JMenu("File")
        val openFTPItem = JMenuItem("Open FTP address")
        val exitItem = JMenuItem("Exit")
        menu.add(openFTPItem)
        menu.add(JSeparator())
        menu.add(exitItem)

        add(menu)
        exitItem.addActionListener { exitProcess(0) }
        openFTPItem.addActionListener(ftpConnectionActionListener)
    }
}