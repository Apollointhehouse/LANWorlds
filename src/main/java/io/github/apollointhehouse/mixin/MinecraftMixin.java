package io.github.apollointhehouse.mixin;

import io.github.apollointhehouse.events.TickServer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.apollointhehouse.LANWorlds.EVENT_BUS;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
	@Inject(method = "runTick", at = @At("HEAD"))
	public void tick(CallbackInfo info) {
		EVENT_BUS.post(TickServer.INSTANCE);
	}
}

