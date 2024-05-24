package io.github.apollointhehouse.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.core.lang.I18n

fun gui(block: GuiBuilder.() -> Unit) = GuiBuilder().apply(block).build()

class GuiBuilder {
	private val controls = mutableListOf<Button>()
	private val texts = mutableListOf<Text>()
	private val i18n: I18n = I18n.getInstance()
	private var init: (GuiScreen.() -> Unit)? = null
	private var onDraw: (GuiScreen.(mouseX: Int, mouseY: Int, delta: Float) -> Unit)? = null
	val mc: Minecraft = Minecraft.getMinecraft(this)

	fun init(block: GuiScreen.() -> Unit) {
		init = block
	}

	fun onDraw(block: GuiScreen.(mouseX: Int, mouseY: Int, delta: Float) -> Unit) {
		onDraw = block
	}

	fun GuiScreen.text(text: String, x: Int, y: Int) {
		texts.add(Text(x, y, i18n.translateKey(text)))
	}

	fun GuiScreen.button(name: String, x: Int, y: Int, block: GuiScreen.() -> Unit) {
		controls.add(Button(x, y, i18n.translateKey(name), block))
	}

	fun build(): GuiScreen {
		return CustomGuiScreen(controls = controls, texts = texts, init = init, onDraw = onDraw)
	}
}

class Text(val x: Int, val y: Int, val text: String)

class Button(val x: Int, val y: Int, val text: String, val block: GuiScreen.() -> Unit)
