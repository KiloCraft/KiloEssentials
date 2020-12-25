package org.kilocraft.essentials.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.api.util.ScheduledExecution;
import org.kilocraft.essentials.api.util.ScheduledExecutionThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {

    @Inject(method = "executeQueuedCommands", at = @At(value = "RETURN"))
    public void executeScheduledExecutions(CallbackInfo ci) {
        for (ScheduledExecution scheduledExecution : ScheduledExecutionThread.scheduledExecutions) {
            System.out.println("Applying queue");
            scheduledExecution.apply();
        }
        ScheduledExecutionThread.scheduledExecutions.clear();
    }

}
