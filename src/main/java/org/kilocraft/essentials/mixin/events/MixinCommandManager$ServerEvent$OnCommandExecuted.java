package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutedEvent;
import org.kilocraft.essentials.events.commands.OnCommandExecutedEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class MixinCommandManager$ServerEvent$OnCommandExecuted {

    @Inject(at = @At(value = "RETURN", target = "Lnet/minecraft/server/command/CommandManager;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"), method = "execute")
    public void executed(ServerCommandSource serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir) {
        OnCommandExecutedEvent event = new OnCommandExecutedEventImpl(serverCommandSource_1, string_1, cir);
        KiloServer.getServer().triggerEvent(event);
        if (event.isCancelled()) cir.cancel();
    }


}
