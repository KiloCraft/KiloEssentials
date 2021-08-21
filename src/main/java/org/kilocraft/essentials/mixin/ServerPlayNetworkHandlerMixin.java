package org.kilocraft.essentials.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.EssentialPermission;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final
    static Logger LOGGER;

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void modify(ChatMessageC2SPacket chatMessageC2SPacket, CallbackInfo ci) {
        OnlineUser user = KiloEssentials.getUserManager().getOnline(this.player);
        if (!KiloConfig.main().chat().useVanillaChat) {
            KiloEssentials.getUserManager().onChatMessage(user, chatMessageC2SPacket);
            ci.cancel();
        }
    }


    @Inject(method = "onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    private void modify(UpdateSignC2SPacket updateSignC2SPacket, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(updateSignC2SPacket, player.networkHandler, this.player.getServerWorld());
        this.player.updateLastActionTime();
        ServerWorld serverWorld = this.player.getServerWorld();
        BlockPos blockPos = updateSignC2SPacket.getPos();
        if (serverWorld.isChunkLoaded(blockPos)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
            if (!(blockEntity instanceof SignBlockEntity signBlockEntity)) {
                return;
            }

            if (!signBlockEntity.isEditable() || signBlockEntity.getEditor() != this.player.getUuid()) {
                LOGGER.warn("Player {} just tried to change non-editable sign", this.player.getEntityName());
                return;
            }

            String[] strings = updateSignC2SPacket.getText();

            boolean canUseFormats = KiloEssentials.hasPermissionNode(this.player.getCommandSource(), EssentialPermission.SIGN_COLOR);
            for (int i = 0; i < strings.length; ++i) {
                String s = strings[i];
                if (!canUseFormats) s = ComponentText.clearFormatting(s);
                signBlockEntity.setTextOnRow(i, ComponentText.toText(s));
            }

            signBlockEntity.markDirty();
            serverWorld.updateListeners(blockPos, blockState, blockState, 3);
        }
    }

}
