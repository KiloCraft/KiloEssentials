package org.kilocraft.essentials.mixin.patch.util;

import net.minecraft.server.dedicated.DedicatedServer;
import org.kilocraft.essentials.api.util.schedule.AbstractScheduler;
import org.kilocraft.essentials.api.util.schedule.ScheduledExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    /**
     * Run scheduled commands on main thread
     */
    @Inject(
            method = "handleConsoleInputs",
            at = @At("RETURN")
    )
    public void executeScheduledExecutions(CallbackInfo ci) {
        for (ScheduledExecution scheduledExecution : AbstractScheduler.scheduledExecutions) {
            scheduledExecution.apply();
        }
        AbstractScheduler.scheduledExecutions.clear();
    }

}
