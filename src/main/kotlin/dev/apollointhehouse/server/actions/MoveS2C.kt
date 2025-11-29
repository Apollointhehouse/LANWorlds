package dev.apollointhehouse.server.actions

import com.b100.utils.FileUtils
import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.events.TickServer
import dev.apollointhehouse.gui.ScreenSavingServer
import dev.apollointhehouse.server.ServerController
import me.apollointhehouse.raywire.api.EventHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScreenMainMenu
import java.io.File

class MoveS2C(
    private val server: ServerController,
    private val mc: Minecraft,
    private val worldName: String,
    private val proc: Process,
    private val serverPath: String
) : Action {
    override fun run() {
        EVENT_BUS.subscribe(this)
        mc.displayScreen(ScreenSavingServer())
    }

    context(_: TickServer)
    @EventHandler
    fun tick() {
        if (proc.isAlive) return

        moveWorld()
        LOGGER.info("Moved world from server to client!")

        mc.displayScreen(null)
        mc.displayScreen(ScreenMainMenu())

        EVENT_BUS.unsubscribe(server)
        EVENT_BUS.unsubscribe(this)
    }

    private fun moveWorld(): File {
        val from = File("${serverPath}/${worldName}")
        val to = File("${ServerController.SAVES_PATH}/${worldName}").also {
            if (it.exists()) it.deleteRecursively()
            it.mkdirs()
        }

        runCatching {
            FileUtils.copyAll(from, to)
        }.onFailure {
            error("Failed to save world to: ${ServerController.SAVES_PATH}/${worldName}")
        }

        return to
    }
}
