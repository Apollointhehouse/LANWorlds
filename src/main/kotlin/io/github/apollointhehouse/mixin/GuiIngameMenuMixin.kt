package io.github.apollointhehouse.mixin

import io.github.apollointhehouse.LANWorlds
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiIngameMenu
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [GuiIngameMenu::class], remap = false)
abstract class GuiIngameMenuMixin {
	@Inject(method = ["buttonPressed"], at = [At("RETURN")])
	private fun buttonPressed(button: GuiButton, ci: CallbackInfo) {
		if (button.id == 1) LANWorlds.server?.stopServer()
	}
}
