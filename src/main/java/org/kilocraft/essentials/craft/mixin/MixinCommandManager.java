package org.kilocraft.essentials.craft.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class MixinCommandManager {

    @Inject(
            method = "literal", cancellable = true,
            at = @At(value = "RETURN", target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;")
    )

    private static void modify(String string_1, CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource>> cir) {
        cir.setReturnValue(LiteralArgumentBuilder.literal("minecraft:" + string_1));
    }
}
