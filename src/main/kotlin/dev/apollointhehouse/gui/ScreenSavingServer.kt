package dev.apollointhehouse.gui

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.ButtonElement
import net.minecraft.client.gui.Screen
import net.minecraft.core.lang.I18n

@Environment(EnvType.CLIENT)
class ScreenSavingServer : Screen() {
    override fun tick() {
    }

    override fun keyPressed(
        eventCharacter: Char,
        eventKey: Int,
        mx: Int,
        my: Int,
    ) {}

    override fun init() {
        buttons.clear()
    }

    override fun buttonClicked(button: ButtonElement) {}

    override fun render(
        mx: Int,
        my: Int,
        partialTick: Float,
    ) {
        this.renderBackground()
        val trans = I18n.getInstance()

        drawStringCenteredNoShadow(fontRenderer, trans.translateKey("lanworlds.saving"), width / 2, height / 2 - 50, 16777215)
//        drawStringCentered(font, line, width / 2, height / 2 - 10, 16777215)

        super.render(mx, my, partialTick)
    }
}
