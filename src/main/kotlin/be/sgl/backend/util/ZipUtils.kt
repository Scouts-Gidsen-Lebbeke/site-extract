package be.sgl.backend.util

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun List<ByteArray>.zipped(): ByteArray {
    val zipByteArrayOutputStream = ByteArrayOutputStream()
    ZipOutputStream(zipByteArrayOutputStream).use { zipOutputStream ->
        forEachIndexed { index, file ->
            val zipEntry = ZipEntry("file$index.pdf")
            zipOutputStream.putNextEntry(zipEntry)
            zipOutputStream.write(file)
            zipOutputStream.closeEntry()
        }
    }
    return zipByteArrayOutputStream.toByteArray()
}