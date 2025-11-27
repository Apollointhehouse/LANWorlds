package io.github.apollointhehouse.server

import io.github.apollointhehouse.LANWorlds
import io.github.apollointhehouse.LANWorlds.LOGGER
import io.github.apollointhehouse.server.ServerUtils.downloadFile
import io.github.apollointhehouse.server.ServerUtils.saveTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import java.io.File

class Server private constructor(val data: LevelData) {
	private val mc: Minecraft = Minecraft.getMinecraft()
	private var process: Process? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    val name: String = data.worldName

    constructor(world: World) : this(world.levelData) {
        val player = world.players[0]
        val serverFolder = ServerUtils.createDirectory("${ServerUtils.SERVERS_PATH}/$name")
        LOGGER.info("Created server folder!")

        ServerUtils.serverJarURL.downloadFile("${serverFolder.path}/server.jar")
        LOGGER.info("Downloaded server.jar!")

        val props = ServerUtils.createServerProperties(data, world)
        LOGGER.info("Created server properties!")

        props.saveTo("${serverFolder.path}/server.properties")
        LOGGER.info("Saved server properties!")

        data.saveTo(serverFolder.path)
        LOGGER.info("Saved world!")

        player.saveTo("${serverFolder.path}/$name/players/${player.username}.dat")
        LOGGER.info("Saved player data!")

        ServerUtils.createFile("${serverFolder.path}/ops.txt").writeText(player.username)

        LOGGER.info("Created server!")
    }

	fun startServer(): Job {
		val serverFolder = File("${mc.minecraftDir.path}/servers/$name").apply {
			if (!exists()) {
				LOGGER.error("Server directory does not exist!")
				error("Failed to start server!")
			}
		}

		process = ProcessBuilder()
			.command("java", "-jar", "${serverFolder.path}/server.jar")
			.directory(serverFolder)
			.start()

		val out = process?.inputStream?.bufferedReader() ?: error("Failed to create buffered reader!")

        LOGGER.info("Started server jar!")

        return scope.launch {
            while (true) {
                val line = out.readLine() ?: break
                println(line)
                if (line.isEmpty()) break
                if (line.contains("Done", ignoreCase = true)) break
            }
        }
	}

	fun stopServer(): Server {
		val out = process?.outputStream?.bufferedWriter() ?: error("Failed to create buffered writer!")
		runCatching {
			out.write("stop\n")
			out.flush()
		}.onFailure {
			error("Failed to write to process out!")
		}

		LANWorlds.server = null
		LOGGER.info("Stopped server!")
		return this
	}
}
