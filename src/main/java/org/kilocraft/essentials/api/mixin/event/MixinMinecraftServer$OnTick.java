package org.kilocraft.essentials.api.mixin.event;

import java.util.function.BooleanSupplier;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl.ServerEvent$OnTickImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer$OnTick {

	@Inject(method = "tick", at = @At(value = "INVOKE"))
	protected void tick(BooleanSupplier booleanSupplier_1) {
		KiloServer.getServer().triggerEvent(new ServerEvent$OnTickImpl((MinecraftServer) (Object) this));
	}
}
