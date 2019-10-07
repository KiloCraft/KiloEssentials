package org.kilocraft.essentials.craft.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class MixinCommandManager {

    @Inject(
            method = "literal", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/command/CommandManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V")
    )

    private static void modify(String string_1, CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource>> cir) {
        if (Commands.vanillaCommandsToRename.contains(string_1)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(Commands.vanillaCommandsPrefix + string_1));
        }
        else if (Commands.keCommandsToKeep.contains(string_1)) {
            cir.setReturnValue(LiteralArgumentBuilder.literal(string_1.replace("ke_", "")));
        }
        else
            cir.setReturnValue(LiteralArgumentBuilder.literal(string_1));
    }

}
