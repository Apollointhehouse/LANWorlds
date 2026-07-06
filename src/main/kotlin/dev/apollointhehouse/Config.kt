package dev.apollointhehouse

import net.minecraft.client.Minecraft
import net.minecraft.core.Global
import java.io.InputStream
import java.net.URL

object Config {
    val mc: Minecraft = Minecraft.getMinecraft()
    val VERSION: String = Global.VERSION
    val BUILD_CHANNEL: String = Global.BUILD_CHANNEL.toString().lowercase()
    val SERVER_JAR_URL = URL("https://downloads.betterthanadventure.net/bta-server/$BUILD_CHANNEL/v$VERSION/server.jar")
    val SERVER_PATH = "${mc.minecraftDir.path}/lan-server"
    val MC_SAVES_PATH = "${mc.minecraftDir.path}/saves"
    val NO_GUI = true
    val SERVER_PROPS: InputStream? = LANWorlds::class.java.getResourceAsStream("/server.properties")
}
