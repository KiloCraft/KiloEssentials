package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.Chunk;
import org.kilocraft.essentials.mixin.accessor.ShulkerBoxScreenHandlerAccessor;
import org.kilocraft.essentials.patch.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onClickSlot", at = @At(value = "HEAD"), cancellable = true)
    private void patchShulkerDupe(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof ShulkerBoxScreenHandlerAccessor screenHandler && screenHandler.getInventory() instanceof ShulkerBoxBlockEntity shulker) {
            Chunk chunk = ChunkManager.getChunkIfVisible(this.player.getServerWorld(), shulker.getPos());
            if (chunk == null || chunk.getBlockEntity(shulker.getPos()) != shulker) {
                ci.cancel();
            }
        }
    }
}
