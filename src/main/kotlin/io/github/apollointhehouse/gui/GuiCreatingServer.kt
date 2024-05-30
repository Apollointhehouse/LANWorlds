package io.github.apollointhehouse.gui

import net.minecraft.client.gui.GuiMainMenu

val creatingServer = gui {
	init {
		text(
			"lanworlds.creatingServer",
			width / 2,
			height / 2 - 50
		)

		button(
			"gui.connecting.button.cancel",
			width / 2 - 100,
			height / 4 + 120 + 12,
		) {
			mc.displayGuiScreen(GuiMainMenu())
		}
	}

	onDraw { _, _, _ ->
		drawBackground()
	}
}
