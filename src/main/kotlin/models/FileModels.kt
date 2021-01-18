package models

import java.awt.image.BufferedImage
import java.io.Reader

/**
 * Data models for file content representations
 */

sealed class FileModel(val path: String) {
    class TextFileModel(path: String, val reader: Reader) : FileModel(path)
    class ZipFileModel(path: String, val text: String) : FileModel(path)
    class ImageFileModel(path: String, val image: BufferedImage) : FileModel(path)
    class ErrorFileModel(val message: String) : FileModel("")
}

