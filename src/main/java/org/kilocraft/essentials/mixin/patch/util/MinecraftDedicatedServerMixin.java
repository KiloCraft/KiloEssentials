package org.kilocraft.essentials.mixin.patch.util;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.api.util.schedule.AbstractScheduler;
import org.kilocraft.essentials.api.util.schedule.ScheduledExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    /**
     * Run scheduled commands on main thread
     */
    @Inject(
            method =
                    "executeQueuedCommands",
            at = @At("RETURN")
    )
    public void executeScheduledExecutions(CallbackInfo ci) {
        for (ScheduledExecution scheduledExecution : AbstractScheduler.scheduledExecutions) {
            scheduledExecution.apply();
        }
        AbstractScheduler.scheduledExecutions.clear();
    }

}
