package io.github.apollointhehouse.gui

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen

class CustomGuiScreen(
	private val controls: List<Button>,
	private val texts: List<Text>,
	private val init: (GuiScreen.() -> Unit)?,
	private val onDraw: (GuiScreen.(mouseX: Int, mouseY: Int, delta: Float) -> Unit)?
) : GuiScreen() {
	override fun init() {
		init?.invoke(this)

		controlList.clear()
		controls.fold(0) { id, btn ->
			controlList.add(GuiButton(id, btn.x, btn.y, btn.width, btn.height, btn.text))
			id + 1
		}
	}

	override fun drawScreen(mouseX: Int, mouseY: Int, delta: Float) {
		onDraw?.invoke(this, mouseX, mouseY, delta)
		texts.forEach { drawStringCentered(fontRenderer, it.text, it.x, it.y, 0xFFFFFF) }
		super.drawScreen(mouseX, mouseY, delta)
	}
}
