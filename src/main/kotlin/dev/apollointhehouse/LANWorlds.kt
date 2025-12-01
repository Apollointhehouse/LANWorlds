package dev.apollointhehouse

import dev.apollointhehouse.gui.ScreenCreatingServer
import dev.apollointhehouse.logging.withEvent
import me.apollointhehouse.raywire.api.Bus
import net.fabricmc.api.ModInitializer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.options.components.ShortcutComponent
import net.minecraft.client.gui.options.data.OptionsPages
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import turniplabs.halplibe.util.GameStartEntrypoint

object LANWorlds : ModInitializer, GameStartEntrypoint {
    const val MOD_ID: String = "lanworlds"

    @JvmField val EVENT_BUS = Bus()
    val LOGGER: Logger =
        LoggerFactory
            .getLogger(MOD_ID)
            .withEvent()

    override fun onInitialize() {
        LOGGER.info("LANWorlds Initialised!")
    }

    override fun beforeGameStart() {}

    override fun afterGameStart() {
        val mc = Minecraft.getMinecraft()

        OptionsPages.GENERAL.withComponent(
            ShortcutComponent("lanworlds.openLAN") {
                mc.displayScreen(null)
                mc.displayScreen(ScreenCreatingServer())
            },
        )
    }
}
