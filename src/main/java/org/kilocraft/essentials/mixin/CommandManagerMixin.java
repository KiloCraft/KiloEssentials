package org.kilocraft.essentials.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;
import org.kilocraft.essentials.commands.Commands;
import org.kilocraft.essentials.events.commands.OnCommandExecutionEventImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Shadow
    public abstract int execute(ServerCommandSource serverCommandSource_1, String string_1);

    @Shadow
    public abstract CommandDispatcher<ServerCommandSource> getDispatcher();

    @Inject(method = "literal", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/server/command/CommandManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private static void modify(String string_1, CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource>> cir) {
        if (Commands.isVanillaCommand(string_1)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(Commands.vanillaCommandsPrefix + string_1));
        }
        else if (Commands.isCustomCommand(string_1)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(string_1.replace("ke_", "")));
        }
        else
            cir.setReturnValue(LiteralArgumentBuilder.literal(string_1));
    }

    @Inject(method = "<init>", at = {@At("RETURN")})
    private void CommandManager(boolean boolean_1, CallbackInfo ci) {
        KiloEssentialsImpl.commandDispatcher = this.dispatcher;
        LOGGER.debug("Set the CommandDispatcher (of ServerCommandSource) to: " + this.dispatcher);
    }


    @Redirect(method = "makeTreeForSource", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
    private <S> boolean modifySuggestions(CommandNode<S> commandNode, S source) {
        return Commands.canSourceUse(commandNode, source);
    }

    @Inject(method = "execute", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/server/command/CommandManager;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"))
    private void modifyExecute(ServerCommandSource serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir) {
        cir.cancel();
        OnCommandExecutionEvent event = new OnCommandExecutionEventImpl(serverCommandSource_1, string_1);
        KiloServer.getServer().triggerEvent(event);

        if (!event.isCancelled())
            KiloEssentials.getInstance().getCommandHandler().execute(serverCommandSource_1, string_1);
    }

}
