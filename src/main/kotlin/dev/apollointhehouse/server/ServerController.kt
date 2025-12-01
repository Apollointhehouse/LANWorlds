package dev.apollointhehouse.server

import dev.apollointhehouse.Config.NO_GUI
import dev.apollointhehouse.Config.SERVER_PATH
import dev.apollointhehouse.Config.mc
import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.SaveProgess
import dev.apollointhehouse.events.ConsoleMessage
import dev.apollointhehouse.events.StartServer
import dev.apollointhehouse.events.StopServer
import dev.apollointhehouse.events.TickServer
import dev.apollointhehouse.server.actions.Action
import dev.apollointhehouse.server.actions.MoveC2S
import dev.apollointhehouse.server.actions.MoveS2C
import dev.apollointhehouse.server.actions.PlayerJoin
import me.apollointhehouse.raywire.api.EventHandler
import java.io.File
import java.util.ArrayDeque
import java.util.Queue

class ServerController(
    val name: String,
) {
    private var process: Process? = null
    private val queue: Queue<Action> = ArrayDeque()

    init {
        mc.currentWorld.saveWorldIndirectly(
            SaveProgess(onFinish = MoveC2S(mc.currentWorld)),
        )
    }

    @EventHandler
    context(_: TickServer)
    fun tick() {
        if (queue.isEmpty()) return

        queue.remove().run()
    }

    @EventHandler
    context(_: StartServer)
    fun startServer() {
        val serverFolder =
            File(SERVER_PATH).apply {
                if (!exists()) {
                    LOGGER.error("Server directory does not exist!")
                    EVENT_BUS.unsubscribe(this)
                    error("Failed to start server!")
                }
            }

        process =
            ProcessBuilder()
                .command("java", "-jar", "$SERVER_PATH/server.jar", if (NO_GUI) "nogui" else "")
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

    @EventHandler
    context(_: StopServer)
    fun stopServer() {
        val proc = process ?: return
        val out = proc.outputStream?.bufferedWriter() ?: error("Failed to create buffered writer!")

        queue += MoveS2C(this, name, proc, mc.thePlayer.uuid)

        runCatching {
            out.write("stop\n")
            out.flush()
        }.onFailure {
            error("Failed to write to process out!")
        }

        LOGGER.info("Stopped server!")
    }
}
