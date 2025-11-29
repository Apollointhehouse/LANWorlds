package dev.apollointhehouse.server.actions

import com.b100.utils.FileUtils
import com.mojang.nbt.NbtIo
import com.mojang.nbt.tags.CompoundTag
import dev.apollointhehouse.Config.MC_SAVES_PATH
import dev.apollointhehouse.LANWorlds.LOGGER
import net.minecraft.core.entity.player.Player
import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import java.io.File
import java.util.Properties

class MoveC2S(
    private val world: World,
    private val path: String,
) : Action {
    override fun run() {
        val data = world.levelData
        val player = world.players[0]
        val props = createProps()
        LOGGER.info("Created server properties!")

        props.saveTo("$path/server.properties")
        LOGGER.info("Saved server properties!")

        data.saveTo(path)
        LOGGER.info("Saved world!")

        player.saveTo("$path/${data.worldName}/players/${player.username}.dat")
        LOGGER.info("Saved player data!")

        File("$path/ops.txt").also {
            if (!it.exists()) it.createNewFile()
            it.writeText(player.username)
        }

        LOGGER.info("Created server!")
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

        with(props) {
            setProperty("default-gamemode", gamemode)
            setProperty("level-seed", data.randomSeed.toString())
            setProperty("world-type", worldType)
            setProperty("level-name", worldName)
            setProperty("motd", worldName)
            setProperty("difficulty", world.difficulty.toString())
            setProperty("online-mode", "false")
        }

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
            if (to.exists()) to.delete()
            to.createNewFile()

            NbtIo.writeCompressed(playerData, to.outputStream())
        }.onFailure {
            error("Failed to save player data to: $path")
        }

        return to
    }
}
