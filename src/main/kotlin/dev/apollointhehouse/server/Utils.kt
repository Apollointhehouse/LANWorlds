package dev.apollointhehouse.server

import java.io.File
import java.net.URL

object Utils {
    fun createDirectory(path: String): File {
        val dir = File(path)
        if (dir.exists()) dir.deleteRecursively()
        dir.mkdirs()
        return dir
    }

    fun createFile(path: String): File {
        val file = File(path)
        runCatching {
            if (!file.exists()) file.createNewFile()
        }.onFailure {
            error("Failed to create file: $path!")
        }
        return file
    }

    fun URL.downloadFile(to: String): File {
        val localFile = File(to)
        runCatching {
            if (!localFile.exists()) localFile.createNewFile()
            localFile.writeBytes(readBytes())
        }.onFailure {
            error("Failed to download file: ${it.stackTrace}!")
        }
        return localFile
    }
}
