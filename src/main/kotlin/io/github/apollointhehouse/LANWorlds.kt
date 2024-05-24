package io.github.apollointhehouse

//import io.github.apollointhehouse.gui.GuiCreatingServer
import io.github.apollointhehouse.gui.creatingServer
import io.github.apollointhehouse.server.Server
import net.fabricmc.api.ModInitializer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.options.components.ShortcutComponent
import net.minecraft.client.gui.options.data.OptionsPages
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import turniplabs.halplibe.util.GameStartEntrypoint
import io.github.apollointhehouse.utils.Result

@Suppress("unused")
object LANWorlds: ModInitializer, GameStartEntrypoint {
    const val MOD_ID: String = "lanworlds"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("LANWorlds Initialised!")
    }

	override fun beforeGameStart() {

	}

	override fun afterGameStart() {
		val mc = Minecraft.getMinecraft(this)

		OptionsPages.GENERAL.withComponent(ShortcutComponent("lanworlds.openLAN") {
			val world = mc.theWorld ?: run {
				LOGGER.error("No world loaded!")
				return@ShortcutComponent
			}

			mc.displayGuiScreen(null)
			mc.displayGuiScreen(creatingServer)

			val server = Server.createServer(world).let { when (it) {
				is Result.Success -> it.value
				is Result.Error -> {
					LOGGER.error(it.message)
					return@ShortcutComponent
				}
			}}

			server.startServer().let { when (it) {
				is Result.Success -> it.value
				is Result.Error -> {
					LOGGER.error(it.message)
					return@ShortcutComponent
				}
			}}
		})
	}
}
