package controllers.files

import models.FileModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenFilesUtilKtTest {
    private val testDirectoryString = "src/test/resources"
    private val textFileDirectoryString = "$testDirectoryString/text_files_directory"
    private val zipFileDirectoryString = "$testDirectoryString/zip_files_directory"
    private val imagesDirectoryString = "$testDirectoryString/images_directory"

    @Test
    fun `reads text file`() {
        val reader = readTextFile(File("$textFileDirectoryString/text.txt")).reader
        val content = reader.readText()
        assertEquals("I am a TXT file!", content)
    }

    @Test
    fun `reads text file with PDF extension`() {
        val reader = readTextFile(File("$textFileDirectoryString/text.pdf")).reader
        val content = reader.readText()
        assertEquals("I am a PDF file!", content)
    }

    @Test
    fun `does not read file without permission`() {
        val file = Files.createTempFile("test", "test").toFile()
        file.deleteOnExit()
        file.setReadable(false)
        assertThrows(FileNotFoundException::class.java) {
            readTextFile(file)
        }
    }

    @Test
    fun `does not read non existing file`() {
        assertThrows(FileNotFoundException::class.java) {
            readTextFile(File("I do not exist"))
        }
    }

    @Test
    fun `reads png file`() {
        val image = readImage(File("$imagesDirectoryString/png_file.png")).image
        assertNotNull(image)
    }

    @Test
    fun `reads zipped directory`() {
        val zipDirectoryContent = readZipFileContent(File("$zipFileDirectoryString/zipped_directory.zip")).text
        println(zipDirectoryContent)
        assertEquals("zip_directory/\nzip_directory/1.txt\nzip_directory/2.jpg\nzip_directory/3", zipDirectoryContent)
    }

    @Test
    fun `does not read not real pdf file`() {
        val result = openFile(File("$textFileDirectoryString/real.pdf"))
        assert(result is FileModel.ErrorFileModel)
    }
}