package org.kilocraft.essentials.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.api.util.schedule.AbstractScheduler;
import org.kilocraft.essentials.api.util.schedule.ScheduledExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    // Run scheduled commands (usually timed teleports) on main thread
    @Inject(method = "executeQueuedCommands", at = @At(value = "RETURN"))
    public void executeScheduledExecutions(CallbackInfo ci) {
        for (ScheduledExecution scheduledExecution : AbstractScheduler.scheduledExecutions) {
            scheduledExecution.apply();
        }
        AbstractScheduler.scheduledExecutions.clear();
    }

}
