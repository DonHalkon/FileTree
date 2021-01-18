package views

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame

/**
 * Frame with default settings
 */
class MyFrame: JFrame() {

    init {
        title = "Hello JetBrains!"
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane.layout = BorderLayout(10, 10)
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(800, 600)
        isVisible = true
    }
}