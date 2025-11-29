package dev.apollointhehouse.server

import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.events.ConsoleMessage
import dev.apollointhehouse.events.StartServer
import dev.apollointhehouse.events.StopServer
import dev.apollointhehouse.events.TickServer
import dev.apollointhehouse.server.actions.Action
import dev.apollointhehouse.server.actions.MoveC2S
import dev.apollointhehouse.server.actions.MoveS2C
import dev.apollointhehouse.server.actions.PlayerJoin
import me.apollointhehouse.raywire.api.EventHandler
import net.minecraft.client.Minecraft
import net.minecraft.core.world.World
import java.io.File
import java.net.URL
import java.util.*
import kotlin.io.readBytes

class ServerController(val name: String, world: World) {
	private var process: Process? = null
    private val queue: Queue<Action> = ArrayDeque()

    init {
        File(PATH).also {
            if (it.exists()) it.deleteRecursively()
            it.mkdirs()
        }

        LOGGER.info("Created server folder!")

        File("${PATH}/server.jar").also {
            if (!it.exists()) it.createNewFile()
            it.writeBytes(SERVER_JAR_URL.readBytes())
        }

        LOGGER.info("Downloaded server.jar!")

        queue += MoveC2S(world, PATH)
    }

    context(_: TickServer)
    @EventHandler
    fun tick() {
        if (queue.isEmpty()) return

        val action = queue.remove()

        action.run()
    }

    context(_: StartServer)
    @EventHandler
	fun startServer() {
		val serverFolder = File(PATH).apply {
			if (!exists()) {
				LOGGER.error("Server directory does not exist!")
                EVENT_BUS.unsubscribe(this)
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

        queue += PlayerJoin(mc)

        return
	}

    context(_: StopServer)
    @EventHandler
	fun stopServer() {
        val proc = process ?: return
        val out = proc.outputStream?.bufferedWriter() ?: error("Failed to create buffered writer!")

        queue += MoveS2C(this, mc, name, proc, PATH)

        runCatching {
			out.write("stop\n")
			out.flush()
		}.onFailure {
            error("Failed to write to process out!")
        }


		LOGGER.info("Stopped server!")
    }

    companion object {
        private val mc: Minecraft = Minecraft.getMinecraft()
        private val VERSION = mc.minecraftVersion
        private val SERVER_JAR_URL = URL("https://downloads.betterthanadventure.net/bta-server/release/v${VERSION}/server.jar")
        private val PATH = "${mc.minecraftDir.path}/lan-server"

        val SAVES_PATH = "${mc.minecraftDir.path}/saves"
    }
}
