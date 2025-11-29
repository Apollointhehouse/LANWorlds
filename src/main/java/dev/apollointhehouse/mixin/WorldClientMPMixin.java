package dev.apollointhehouse.mixin;

import dev.apollointhehouse.events.StopServer;
import net.minecraft.client.world.WorldClientMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.apollointhehouse.LANWorlds.EVENT_BUS;

@Mixin(value = WorldClientMP.class, remap = false)
public abstract class WorldClientMPMixin {
	@Inject(method = "sendQuittingDisconnectingPacket", at = @At("RETURN"))
	public void sendQuittingDisconnectingPacket(CallbackInfo ci) {
		EVENT_BUS.post(StopServer.INSTANCE);
	}
}
