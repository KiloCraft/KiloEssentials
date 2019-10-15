package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl.ServerEvent$OnCommandExecutionImpl;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnCommandExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class MixinCommandManager$ServerEvent$OnCommandExecution {

    @Inject(at = @At(value = "RETURN", target = "Lnet/minecraft/server/command/CommandManager;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"), method = "execute")
    public void commandExecutor(ServerCommandSource serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir) {
        ServerEvent$OnCommandExecution event = new ServerEvent$OnCommandExecutionImpl(serverCommandSource_1, string_1, cir);
        KiloServer.getServer().triggerEvent(event);
        if (event.isCancelled()) cir.cancel();
    }

}
