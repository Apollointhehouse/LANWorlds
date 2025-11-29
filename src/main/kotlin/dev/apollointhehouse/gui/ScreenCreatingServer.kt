package dev.apollointhehouse.gui

import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.LANWorlds.LOGGER
import dev.apollointhehouse.events.ConsoleMessage
import dev.apollointhehouse.events.StartServer
import dev.apollointhehouse.server.ServerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.apollointhehouse.raywire.api.EventHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ButtonElement
import net.minecraft.client.gui.Screen
import net.minecraft.core.lang.I18n

@Environment(EnvType.CLIENT)
class ScreenCreatingServer(mc: Minecraft) : Screen() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val world = mc.currentWorld ?: error("Not in world you dumbass!")
    private val server by lazy { ServerController(world.levelData.worldName, world) }

    private var line = ""

    init {
        EVENT_BUS.subscribe(server)
        EVENT_BUS.subscribe(this)

        scope.launch {
            try {
                EVENT_BUS.post(StartServer)
            } catch (e: Throwable) {
                LOGGER.error(e.stackTrace.toString())
            }
        }
    }

    @EventHandler
    fun onConsoleMessage(event: ConsoleMessage) {
        line = event.message

        if (line.isEmpty() || line.contains("Done", ignoreCase = true)) EVENT_BUS.unsubscribe(this)
    }

    override fun tick() {}

    override fun keyPressed(eventCharacter: Char, eventKey: Int, mx: Int, my: Int) {}

    override fun init() {
        buttons.clear()
    }

    override fun buttonClicked(button: ButtonElement) {}

    override fun render(mx: Int, my: Int, partialTick: Float) {
        this.renderBackground()
        val trans = I18n.getInstance()

        drawStringCentered(font, trans.translateKey("lanworlds.creating"), width / 2, height / 2 - 50, 16777215)
        drawStringCentered(font, line, width / 2, height / 2 - 10, 16777215)

        super.render(mx, my, partialTick)
    }
}
