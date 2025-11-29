package dev.apollointhehouse.server.actions

import com.b100.utils.FileUtils
import com.mojang.nbt.NbtIo
import com.mojang.nbt.tags.CompoundTag
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.server.Server
import dev.apollointhehouse.server.Utils
import dev.apollointhehouse.server.Utils.createDirectory
import net.minecraft.core.entity.player.Player
import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import java.io.File
import java.util.*

class MoveC2S(val world: World, val path: String) : Action {
    override fun run() {
        val data = world.levelData

        val player = world.players[0]
        val props = createProps()
        LOGGER.info("Created server properties!")

        props.saveTo("${path}/server.properties")
        LOGGER.info("Saved server properties!")

        data.saveTo(path)
        LOGGER.info("Saved world!")

        player.saveTo("${path}/${data.worldName}/players/${player.username}.dat")
        LOGGER.info("Saved player data!")

        Utils.createFile("${path}/ops.txt").writeText(player.username)

        LOGGER.info("Created server!")
    }

    private fun createProps(): Properties {
        val data = world.levelData
        val worldName = data.worldName
        val gamemode = when (data.gamemode) {
            0 -> "Survival"
            1 -> "Creative"
            2 -> "Adventure"
            3 -> "Spectator"
            else -> error("Invalid gamemode!")
        }
        val worldType = "minecraft:" + world.worldType.languageKey.substringAfter('.')

        val props = ClassLoader.getSystemResourceAsStream("server.properties")
            ?.let { Properties().apply { load(it) } }
            ?: error("Failed to create server properties!")

        props.setProperty("default-gamemode", gamemode)
        props.setProperty("level-seed", data.randomSeed.toString())
        props.setProperty("world-type", worldType)
        props.setProperty("level-name", worldName)
        props.setProperty("motd", worldName)
        props.setProperty("difficulty", world.difficulty.toString())
        props.setProperty("online-mode", "false")

        return props
    }

    private fun Properties.saveTo(path: String): File {
        val to = File(path)
        runCatching {
            if (!to.exists()) to.createNewFile()
            this.store(to.outputStream(), "")
        }.onFailure {
            error("Failed to save server properties file to: $path")
        }
        return to
    }

    private fun LevelData.saveTo(path: String): File {
        val from = File("${Server.SAVES_PATH}/${worldName}")
        val to = createDirectory("$path/$worldName")

        runCatching {
            FileUtils.copyAll(from, to)
        }.onFailure {
            error("Failed to save world to: $path")
        }

        return to
    }

    private fun Player.saveTo(path: String): File {
        val to = File(path)
        val playerData = CompoundTag()
        runCatching {
            saveWithoutId(playerData)
            to.parentFile.mkdirs()
            if (!to.exists()) to.createNewFile()
            NbtIo.writeCompressed(playerData, to.outputStream())
        }.onFailure {
            error("Failed to save player data to: $path")
        }

        return to
    }
}
