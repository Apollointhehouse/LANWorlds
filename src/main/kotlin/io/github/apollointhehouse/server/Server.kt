package io.github.apollointhehouse.server

import io.github.apollointhehouse.LANWorlds.EVENT_BUS
import io.github.apollointhehouse.LANWorlds.LOGGER
import io.github.apollointhehouse.events.ConsoleMessage
import io.github.apollointhehouse.events.StartServer
import io.github.apollointhehouse.events.StopServer
import io.github.apollointhehouse.events.TickServer
import io.github.apollointhehouse.server.Utils.createDirectory
import io.github.apollointhehouse.server.Utils.createFile
import io.github.apollointhehouse.server.Utils.downloadFile
import io.github.apollointhehouse.server.Utils.saveTo
import me.apollointhehouse.raywire.api.EventHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScreenConnecting
import net.minecraft.core.world.World
import java.io.File
import java.net.URL
import java.util.*

class Server(val world: World) {
	private var process: Process? = null
    private var saveQueued = false
    private var joinQueued = false

    init {
        createDirectory(PATH)
        LOGGER.info("Created server folder!")

        SERVER_JAR_URL.downloadFile("${PATH}/server.jar")
        LOGGER.info("Downloaded server.jar!")

        saveQueued = true
    }

    context(_: TickServer)
    @EventHandler
    fun tick() {
        when {
            joinQueued -> {
                mc.changeWorld(null)
                mc.displayScreen(null)
                mc.displayScreen(ScreenConnecting(mc, "localhost", 25565))

                joinQueued = false
            }
            saveQueued -> {
                val data = world.levelData

                val player = world.players[0]
                val props = createProps()
                LOGGER.info("Created server properties!")

                props.saveTo("${PATH}/server.properties")
                LOGGER.info("Saved server properties!")

                data.saveTo(PATH)
                LOGGER.info("Saved world!")

                player.saveTo("${PATH}/${data.worldName}/players/${player.username}.dat")
                LOGGER.info("Saved player data!")

                createFile("${PATH}/ops.txt").writeText(player.username)

                LOGGER.info("Created server!")

                saveQueued = false
            }
        }
    }

    context(_: StartServer)
    @EventHandler
	fun startServer() {
		val serverFolder = File(PATH).apply {
			if (!exists()) {
				LOGGER.error("Server directory does not exist!")
				error("Failed to start server!")
			}
		}

		process = ProcessBuilder()
			.command("java", "-jar", "${PATH}/server.jar", "nogui")
			.directory(serverFolder)
			.start()

		val out = process?.inputStream?.bufferedReader() ?: error("Failed to create buffered reader!")

        LOGGER.info("Started server jar!")

        while (true) {
            val line = out.readLine() ?: break
            println(line)

            EVENT_BUS.post(ConsoleMessage(line))

            if (line.isEmpty()) break
            if (line.contains("Done", ignoreCase = true)) break
        }

        joinQueued = true

        return
	}

    context(_: StopServer)
    @EventHandler
	fun stopServer() {
		val out = process?.outputStream?.bufferedWriter() ?: error("Failed to create buffered writer!")
		runCatching {
			out.write("stop\n")
			out.flush()
		}.onFailure {
			error("Failed to write to process out!")
		}

		EVENT_BUS.unsubscribe(this)
		LOGGER.info("Stopped server!")
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

    companion object {
        private val mc: Minecraft = Minecraft.getMinecraft()
        private val VERSION = mc.minecraftVersion
        private val SERVER_JAR_URL = URL("https://downloads.betterthanadventure.net/bta-server/release/v${VERSION}/server.jar")
        private val PATH = "${mc.minecraftDir.path}/lan-server"

        val SAVES_PATH = "${mc.minecraftDir.path}/saves"
    }
}
