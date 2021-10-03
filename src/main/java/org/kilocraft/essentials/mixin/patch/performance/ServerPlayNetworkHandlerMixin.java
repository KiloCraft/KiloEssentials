package org.kilocraft.essentials.mixin.patch.performance;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.patch.ChunkManager;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private boolean loggedIn = false;

    // Only tick players once their chunks are fully loaded
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;playerTick()V"))
    private void loadLoginChunksAsync(ServerPlayerEntity serverPlayer) {
        if (!ServerSettings.notick_player_login) {
            if (this.loggedIn) {
                serverPlayer.playerTick();
            } else if (ChunkManager.isChunkVisible(serverPlayer.getServerWorld(), serverPlayer.getBlockPos())) {
                this.loggedIn = true;
                serverPlayer.playerTick();
            }
        } else {
            serverPlayer.playerTick();
        }
    }

}
