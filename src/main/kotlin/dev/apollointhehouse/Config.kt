package dev.apollointhehouse

import net.minecraft.client.Minecraft
import java.net.URL

object Config {
    val mc: Minecraft = Minecraft.getMinecraft()
    val MC_VERSION: String = mc.minecraftVersion
    val SERVER_JAR_URL = URL("https://downloads.betterthanadventure.net/bta-server/release/v$MC_VERSION/server.jar")
    val SERVER_PATH = "${mc.minecraftDir.path}/lan-server"
    val MC_SAVES_PATH = "${mc.minecraftDir.path}/saves"
    val NO_GUI = false
}
