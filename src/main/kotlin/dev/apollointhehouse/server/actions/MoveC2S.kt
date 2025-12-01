package dev.apollointhehouse.server.actions

import com.b100.utils.FileUtils
import com.mojang.nbt.NbtIo
import com.mojang.nbt.tags.CompoundTag
import dev.apollointhehouse.Config.MC_SAVES_PATH
import dev.apollointhehouse.Config.SERVER_JAR_URL
import dev.apollointhehouse.Config.SERVER_PATH
import dev.apollointhehouse.Config.mc
import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.events.StartServer
import net.minecraft.core.entity.player.Player
import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import java.io.File
import java.util.Properties

class MoveC2S(
    private val world: World,
) : Action {
    override fun run() {
        val data = world.levelData
        val player = mc.thePlayer

        File(SERVER_PATH).also {
            if (it.exists()) it.deleteRecursively()
            it.mkdirs()
        }
        LOGGER.info("Created server folder!")

        LOGGER.info("Downloading server jar...")
        File("$SERVER_PATH/server.jar").also {
            if (!it.exists()) it.createNewFile()
            it.writeBytes(SERVER_JAR_URL.readBytes())
        }
        LOGGER.info("Downloaded server jar!")

        val props = createProps()
        LOGGER.info("Created server properties!")

        props.saveTo("$SERVER_PATH/server.properties")
        LOGGER.info("Saved server properties!")

        data.saveTo(SERVER_PATH)
        LOGGER.info("Saved world!")

        player.saveTo("$SERVER_PATH/${data.worldName}/players/${player.uuid}.dat")
        LOGGER.info("Saved player data!")

        File("$SERVER_PATH/ops.txt").also {
            if (!it.exists()) it.createNewFile()
            it.writeText(player.username)
        }

        LOGGER.info("Created server!")

        EVENT_BUS.post(StartServer)
    }

    private fun createProps(): Properties {
        val data = world.levelData
        val worldName = data.worldName
        val gamemode =
            when (data.gamemode) {
                0 -> "Survival"
                1 -> "Creative"
                2 -> "Adventure"
                3 -> "Spectator"
                else -> error("Invalid gamemode!")
            }
        val worldType = "minecraft:" + world.worldType.languageKey.substringAfter('.')

        val props =
            ClassLoader
                .getSystemResourceAsStream("server.properties")
                ?.let { Properties().apply { load(it) } }
                ?: error("Failed to create server properties!")

        props["default-gamemode"] = gamemode
        props["level-seed"] = data.randomSeed.toString()
        props["world-type"] = worldType
        props["level-name"] = worldName
        props["motd"] = worldName
        props["difficulty"] = world.difficulty.toString()
        props["online-mode"] = "false"

        return props
    }

    private operator fun Properties.set(
        key: String,
        value: String,
    ) = setProperty(key, value)

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
        val from = File("${MC_SAVES_PATH}/$worldName")
        val to =
            File("$path/$worldName").also {
                if (it.exists()) it.deleteRecursively()
                it.mkdirs()
            }

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
