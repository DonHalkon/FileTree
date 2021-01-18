package views

import models.utils.FTPConnectionData
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.net.MalformedURLException
import java.net.URL
import javax.swing.*

/**
 * Dialog for getting ftp connection data
 */
class FTPLoginDialog(parent: MyFrame) : JDialog(parent, "Enter FTP address", true), ActionListener {
    private var data: FTPConnectionData? = null
    private val address = JTextField(30)
    private val username = JTextField()
    private val password = JPasswordField()

    private val btnOk = JButton("Ok")
    private val btnCancel = JButton("Cancel")

    init {
        val loc: Point = parent.location
        setLocation(loc.x + 100, loc.y + 100)
        val panel = JPanel()
        panel.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.insets = Insets(2, 2, 2, 2)
        gbc.fill = GridBagConstraints.HORIZONTAL

        // set address label
        val addressLabel = JLabel("FTP address:")
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(addressLabel, gbc)
        // set address field
        gbc.gridwidth = 2
        gbc.gridx = 1
        gbc.gridy = 0
        panel.add(address, gbc)
        // default value for quick tests
        address.text = "ftp://test.rebex.net"

        // set username label
        val usernameLabel = JLabel("Username:")
        gbc.gridwidth = 2
        gbc.gridx = 0
        gbc.gridy = 1
        panel.add(usernameLabel, gbc)
        // set username field
        gbc.gridx = 1
        gbc.gridy = 1
        panel.add(username, gbc)
        // default value for quick tests
        username.text = "demo"

        // set username label
        val passwordLabel = JLabel("Password:")
        gbc.gridwidth = 2
        gbc.gridx = 0
        gbc.gridy = 2
        panel.add(passwordLabel, gbc)
        // set username field
        gbc.gridx = 1
        gbc.gridy = 2
        panel.add(password, gbc)
        // default value for quick tests
        password.text = "password"

        // set buttons
        btnOk.addActionListener(this)
        gbc.gridwidth = 1
        gbc.gridx = 0
        gbc.gridy = 3
        panel.add(btnOk, gbc)

        btnCancel.addActionListener(this)
        gbc.gridx = 1
        gbc.gridy = 3
        panel.add(btnCancel, gbc)
        contentPane.add(panel)

        getRootPane().defaultButton = btnOk
        pack()
        this.isVisible = true
    }

    override fun actionPerformed(event: ActionEvent) {
        if (event.source === btnOk) {
            try {
                val url = URL(address.text)
                if (!url.protocol.startsWith("ftp")) throw MalformedURLException("Only FTP servers supported.")
                data = FTPConnectionData(url, username.text, password.text)
                dispose()
            } catch (ex: MalformedURLException) {
                JOptionPane.showMessageDialog(
                    this,
                    "Incorrect FTP address format.\n${ex.message}",
                    "Error",
                    JOptionPane.WARNING_MESSAGE
                )
            }
        } else {
            data = null
            dispose()
        }
    }

    fun getConnectionData(): FTPConnectionData? {
        return data
    }
}
