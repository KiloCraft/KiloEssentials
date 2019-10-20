package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl.ServerEvent$OnTickImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer$OnTick {

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
		KiloServer.getServer().triggerEvent(new ServerEvent$OnTickImpl((MinecraftServer) (Object) this));
	}
}
