package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.events.CommandEvents;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.kilocraft.essentials.util.commands.LiteralCommandModified.*;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "literal", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/server/command/CommandManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private static void modify(String command, CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource>> cir) {
        if (shouldRenameVanillaCommand(command)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(getNMSCommandName(command)));
        } else if (shouldRenameCustomCommand(command)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(getKECommandName(command)));
        } else
            cir.setReturnValue(LiteralArgumentBuilder.literal(command));
    }

    @Shadow
    public abstract int execute(ServerCommandSource src, String string);

    @Shadow
    public abstract CommandDispatcher<ServerCommandSource> getDispatcher();

    @Inject(method = "<init>", at = {@At("RETURN")})
    private void CommandManager(CommandManager.RegistrationEnvironment env, CallbackInfo ci) {
        CommandEvents.REGISTER_COMMAND.invoker().register(this.dispatcher, env);
    }

    @Redirect(method = "makeTreeForSource", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
    private <S> boolean modifySuggestions(CommandNode<S> node, S source) {
        return canSourceUse(node, source);
    }

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void socialSpy(ServerCommandSource src, String command, CallbackInfoReturnable<Integer> cir) {
        command = command.startsWith("/") ? command.substring(1) : command;
        if (KiloCommands.isCommandDisabled(src, command.split(" ")[0])) {
            cir.setReturnValue(0);
        }
        KiloCommands.onCommand(src, command);
    }

}
