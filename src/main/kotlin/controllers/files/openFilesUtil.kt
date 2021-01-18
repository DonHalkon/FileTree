package controllers.files

import models.*
import java.io.File
import java.io.IOException
import java.lang.NullPointerException
import java.nio.file.Files
import java.util.zip.ZipFile
import javax.imageio.ImageIO

fun openFile(path: File): FileModel {
    if (!path.canRead()) return FileModel.ErrorFileModel("File '$path' is not readable.")
    return try {
        val contentType = Files.probeContentType(path.toPath())

        if ("application/text" == contentType) return readTextFile(path)
        if ("text/plain" == contentType) return readTextFile(path)
        if ("application/zip" == contentType) return readZipFileContent(path)
        if ("application/java-archive" == contentType) return readZipFileContent(path)
        if (contentType.startsWith("image/")) return readImage(path)

        throw IOException("Unsupported file type: $contentType")
    } catch (ex: IOException) {
        FileModel.ErrorFileModel("Can not read file '$path'.<br>Error message: ${ex.message}")
    } catch (ex: NullPointerException) {
        FileModel.ErrorFileModel("Can not read file '$path'.<br>Error message: Unsupported file type")
    }
}

fun readImage(path: File): FileModel.ImageFileModel {
    val image = ImageIO.read(path)
    return FileModel.ImageFileModel(path.absolutePath, image)
}

fun readTextFile(path: File): FileModel.TextFileModel {
    return FileModel.TextFileModel(path.absolutePath, path.bufferedReader())
}

fun readZipFileContent(path: File): FileModel.ZipFileModel {
    val zip = ZipFile(path)
    return FileModel.ZipFileModel(path.absolutePath, zip.entries().asSequence().joinToString("\n"))
}