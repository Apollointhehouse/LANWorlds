package io.github.apollointhehouse.server

import io.github.apollointhehouse.LANWorlds
import io.github.apollointhehouse.LANWorlds.LOGGER
import io.github.apollointhehouse.mixin.WorldAccessor
import io.github.apollointhehouse.server.ServerUtils.downloadFile
import io.github.apollointhehouse.server.ServerUtils.saveTo
import io.github.apollointhehouse.utils.Result
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiConnecting
import net.minecraft.core.world.World
import java.io.File
import kotlin.concurrent.thread

class Server private constructor(val hostUser: String, val name: String) {
	private val mc: Minecraft = Minecraft.getMinecraft(this)
	private var process: Process? = null

	fun startServer(): Result<Server> {
		val serverFolder = File("${mc.minecraftDir.path}/servers/$name").apply {
			if (!exists()) {
				LOGGER.error("Server directory does not exist!")
				return Result.Error("Failed to start server!")
			}
		}

		process = ProcessBuilder()
			.command("java", "-jar", "${serverFolder.path}/server.jar")
			.directory(serverFolder)
			.start()

		val out = process?.inputReader() ?: return Result.Error("Failed to create buffered reader!")

		thread {
			while (true) {
				val line = out.readLine() ?: break
				println(line)
				if (line.isEmpty()) break
				if (line.contains("Done")) break
			}
			mc.displayGuiScreen(null)
			mc.displayGuiScreen(GuiConnecting(mc, "localhost", 25565))
		}

		LOGGER.info("Started server jar!")
		return Result.Success(this)
	}

	fun stopServer(): Result<Server> {
		val out = process?.outputWriter() ?: return Result.Error("Failed to create buffered writer!")
		runCatching {
			out.write("stop\n")
			out.flush()
		}.onFailure {
			LOGGER.error("Failed to write to process out!")
			return Result.Error("Failed to write to process out!")
		}
		LANWorlds.server = null
		LOGGER.info("Stopped server!")
		return Result.Success(this)
	}

	companion object {
		fun createServer(world: World): Result<Server> {
			val levelData = (world as WorldAccessor).levelData
			val worldName = levelData.worldName
			val player = world.players[0]
			val serverFolder = ServerUtils.createDirectory("${ServerUtils.SERVERS_PATH}/$worldName").let { when (it) {
				is Result.Success -> {
					LOGGER.info("Created server folder!")
					it.value
				}
				is Result.Error -> {
					return Result.Error("Failed to create server folder!")
				}
			}}

			ServerUtils.serverJarURL.downloadFile("${serverFolder.path}/server.jar").let { when (it) {
				is Result.Success -> LOGGER.info("Downloaded server.jar!")
				is Result.Error -> {
					return Result.Error("Failed to download server.jar!")
				}
			}}

			val props = ServerUtils.createServerProperties(levelData, world).let { when (it) {
				is Result.Success -> {
					LOGGER.info("Created server properties!")
					it.value
				}
				is Result.Error -> return it
			}}

			props.saveTo("${serverFolder.path}/server.properties").let { when (it) {
				is Result.Success -> {
					LOGGER.info("Saved server properties!")
					it.value
				}
				is Result.Error -> return it
			}}

			world.saveTo(serverFolder.path).let { when (it) {
				is Result.Success -> {
					LOGGER.info("Saved world!")
					it.value
				}
				is Result.Error -> return it
			}}

			player.saveTo("${serverFolder.path}/$worldName/players/${player.username}.dat").let { when (it) {
				is Result.Success -> {
					LOGGER.info("Saved player data!")
					it.value
				}
				is Result.Error -> return it
			}}

			ServerUtils.createFile("${serverFolder.path}/ops.txt").getOrNull()?.writeText(player.username)
				?: return Result.Error("Failed to create ops.txt!")

			LOGGER.info("Created server!")
			return Result.Success(Server(player.username, levelData.worldName))
		}
	}

}
