package io.github.apollointhehouse

import io.github.apollointhehouse.server.Server
import net.fabricmc.api.ModInitializer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScreenMainMenu
import net.minecraft.client.gui.options.components.ShortcutComponent
import net.minecraft.client.gui.options.data.OptionsPages
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import turniplabs.halplibe.util.GameStartEntrypoint

object LANWorlds: ModInitializer, GameStartEntrypoint {
    const val MOD_ID: String = "lanworlds"
	var server: Server? = null

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("LANWorlds Initialised!")
    }

	override fun beforeGameStart() {}

	override fun afterGameStart() {
		val mc = Minecraft.getMinecraft()

		OptionsPages.GENERAL.withComponent(ShortcutComponent("lanworlds.openLAN") {
			val world = mc.currentWorld ?: run {
				LOGGER.error("No world loaded!")
				return@ShortcutComponent
			}

			mc.displayScreen(null)
			mc.displayScreen(ScreenMainMenu())
//			mc.displayScreen(creatingServer)

            try {
                server = Server(world)

                server?.startServer()
            } catch (e: Throwable) {
                LOGGER.error(e.stackTrace.toString())
            }
		})
	}
}
