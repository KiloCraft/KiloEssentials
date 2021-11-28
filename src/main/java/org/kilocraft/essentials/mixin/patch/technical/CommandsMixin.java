package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.kilocraft.essentials.util.commands.LiteralCommandModified.*;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    // TODO: Rework

    @Inject(
            method = "literal",
            cancellable = true,
            at = @At("HEAD")
    )
    private static void modify(String command, CallbackInfoReturnable<LiteralArgumentBuilder<CommandSourceStack>> cir) {
        if (shouldRenameVanillaCommand(command)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(getNMSCommandName(command)));
        } else if (shouldRenameCustomCommand(command)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(getKECommandName(command)));
        } else
            cir.setReturnValue(LiteralArgumentBuilder.literal(command));
    }

    @Shadow
    public abstract CommandDispatcher<CommandSourceStack> getDispatcher();

    @Redirect(
            method = "fillUsableCommands",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"
            )
    )
    private <S> boolean modifySuggestions(CommandNode<S> node, S source) {
        return canSourceUse(node, source);
    }

    @Inject(
            method = "performCommand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void socialSpy(CommandSourceStack src, String command, CallbackInfoReturnable<Integer> cir) {
        command = command.startsWith("/") ? command.substring(1) : command;
        if (KiloCommands.isCommandDisabled(src, command.split(" ")[0])) {
            cir.setReturnValue(0);
        }
        KiloCommands.onCommand(src, command);
    }

}
