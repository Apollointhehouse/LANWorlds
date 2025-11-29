package dev.apollointhehouse.server.actions

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScreenConnecting

class PlayerJoin(
    private val mc: Minecraft
) : Action {
    override fun run() {
        mc.changeWorld(null)
        mc.displayScreen(null)
        mc.displayScreen(ScreenConnecting(mc, "localhost", 25565))
    }
}
