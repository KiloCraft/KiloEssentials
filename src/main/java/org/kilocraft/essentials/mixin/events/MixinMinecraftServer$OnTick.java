package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.events.server.ServerTickEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer$OnTick {

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
		KiloServer.getServer().triggerEvent(new ServerTickEventImpl((MinecraftServer) (Object) this));
	}
}
