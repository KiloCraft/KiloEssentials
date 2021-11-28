package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.kilocraft.essentials.mixin.accessor.ShulkerBoxMenuAccessor;
import org.kilocraft.essentials.patch.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    // TODO: Did mojang ever fix this?
    @Inject(
            method = "handleContainerClick",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void patchShulkerDupe(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof ShulkerBoxMenuAccessor screenHandler && screenHandler.getContainer() instanceof ShulkerBoxBlockEntity shulker) {
            ChunkAccess chunk = ChunkManager.getChunkIfVisible(this.player.getLevel(), shulker.getBlockPos());
            if (chunk == null || chunk.getBlockEntity(shulker.getBlockPos()) != shulker) {
                ci.cancel();
            }
        }
    }
}
