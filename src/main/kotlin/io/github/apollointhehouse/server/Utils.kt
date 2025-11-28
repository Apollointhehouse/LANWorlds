package io.github.apollointhehouse.server

import com.b100.utils.FileUtils
import com.mojang.nbt.NbtIo
import com.mojang.nbt.tags.CompoundTag
import net.minecraft.core.entity.player.Player
import net.minecraft.core.world.save.LevelData
import java.io.File
import java.net.URL
import java.util.*

object Utils {
    fun Properties.saveTo(path: String): File {
        val out = File(path)
        runCatching {
            if (!out.exists()) out.createNewFile()
            this.store(out.outputStream(), "")
        }.onFailure {
            error("Failed to save server properties file to: $path")
        }
        return out
    }

    fun LevelData.saveTo(path: String): File {
        val world = File("${Server.SAVES_PATH}/${worldName}")
        val out = createDirectory("$path/$worldName")

        runCatching {
            FileUtils.copyAll(world, out)
        }.onFailure {
            error("Failed to save world to: $path")
        }

        return out
    }

    fun Player.saveTo(path: String): File {
        val out = File(path)
        val playerData = CompoundTag()
        runCatching {
            saveWithoutId(playerData)
            out.parentFile.mkdirs()
            if (!out.exists()) out.createNewFile()
            NbtIo.writeCompressed(playerData, out.outputStream())
        }.onFailure {
            error("Failed to save player data to: $path")
        }

        return out
    }

    fun createDirectory(path: String): File {
        val dir = File(path)
        if (dir.exists()) dir.deleteRecursively()
        dir.mkdirs()
        return dir
    }

    fun URL.downloadFile(path: String): File {
        val localFile = File(path)
        runCatching {
            if (!localFile.exists()) localFile.createNewFile()
            localFile.writeBytes(readBytes())
        }.onFailure {
            error("Failed to download file: ${it.stackTrace}!")
        }
        return localFile
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

}
