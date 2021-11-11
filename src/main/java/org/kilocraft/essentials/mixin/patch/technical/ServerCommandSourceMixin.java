package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.util.ExtraGameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin {

    @Shadow @Final private MinecraftServer server;

    @Redirect(
            method = "sendToOps",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendSystemMessage(Lnet/minecraft/text/Text;Ljava/util/UUID;)V"
            )
    )
    private void shouldBroadcastToOps(ServerPlayerEntity playerEntity, Text message, UUID sender) {
        if (this.server.getGameRules().getBoolean(ExtraGameRules.BROADCAST_ADMIN_COMMANDS)) playerEntity.sendSystemMessage(message, sender);
    }

}
