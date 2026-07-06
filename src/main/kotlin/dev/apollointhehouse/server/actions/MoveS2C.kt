package dev.apollointhehouse.server.actions

import com.b100.utils.FileUtils
import com.mojang.nbt.NbtIo
import com.smushytaco.event_library.api.EventHandler
import dev.apollointhehouse.Config.MC_SAVES_PATH
import dev.apollointhehouse.Config.SERVER_PATH
import dev.apollointhehouse.Config.mc
import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.events.TickServer
import dev.apollointhehouse.gui.ScreenSavingServer
import dev.apollointhehouse.server.ServerController
import net.minecraft.client.gui.ScreenMainMenu
import java.io.File
import java.util.UUID

class MoveS2C(
    private val server: ServerController,
    private val worldName: String,
    private val proc: Process,
    private val uuid: UUID,
) : Action {
    override fun run() {
        mc.displayScreen(ScreenSavingServer())
        EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun tick(event: TickServer) {
        if (proc.isAlive) return

        moveWorld()
        LOGGER.info("Moved world from server to client!")

        movePlayerData()

        mc.displayScreen(null)
        mc.displayScreen(ScreenMainMenu())

        EVENT_BUS.unsubscribe(server)
        EVENT_BUS.unsubscribe(this)
    }

    private fun movePlayerData() {
        val from = File("$SERVER_PATH/$worldName/players/$uuid.dat").inputStream()

        val playerTag = NbtIo.readCompressed(from)

        val to = File("${MC_SAVES_PATH}/$worldName/level.dat")
        val levelTag = NbtIo.readCompressed(to.inputStream())

        levelTag.putCompound("Player", playerTag)
        NbtIo.writeCompressed(levelTag, to.outputStream())
    }

    private fun moveWorld() {
        val from = File("$SERVER_PATH/$worldName")
        val to =
            File("${MC_SAVES_PATH}/$worldName").also {
                if (it.exists()) it.deleteRecursively()
                it.mkdirs()
            }

        runCatching {
            FileUtils.copyAll(from, to)
        }.onFailure {
            error("Failed to save world to: ${MC_SAVES_PATH}/$worldName")
        }
    }
}
