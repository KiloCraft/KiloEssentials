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
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUserManager;
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

    @Inject(method = "onChatMessage", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onChatMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V"))
    private void modify(ChatMessageC2SPacket chatMessageC2SPacket, CallbackInfo ci) {
        if (KiloConfig.main().chat().useVanillaChat &&
                        KiloServer.getServer().getOnlineUser(player).getUpstreamChannelId().equals(GlobalChat.getChannelId())) {
            return;
        }

        ci.cancel();
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onChatMessage(player, chatMessageC2SPacket);
    }

    @Inject(method = "onSignUpdate", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;)V"))
    private void modify(UpdateSignC2SPacket updateSignC2SPacket, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(updateSignC2SPacket, player.networkHandler, this.player.getServerWorld());
        this.player.updateLastActionTime();
        ServerWorld serverWorld = this.player.getServerWorld();
        BlockPos blockPos = updateSignC2SPacket.getPos();
        if (serverWorld.isChunkLoaded(blockPos)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
            if (!(blockEntity instanceof SignBlockEntity)) {
                return;
            }

            SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
            if (!signBlockEntity.isEditable() || signBlockEntity.getEditor() != this.player) {
                this.server.warn("Player " + this.player.getName().getString() + " just tried to change non-editable sign");
                return;
            }

            String[] strings = updateSignC2SPacket.getText();

            boolean canUseFormats = KiloEssentials.hasPermissionNode(this.player.getCommandSource(), EssentialPermission.SIGN_COLOR);
            for(int i = 0; i < strings.length; ++i) {
                String str = TextFormat.translate(strings[i], canUseFormats);
                signBlockEntity.setTextOnRow(i, new LiteralText(str));
            }

            signBlockEntity.markDirty();
            serverWorld.updateListeners(blockPos, blockState, blockState, 3);
        }
    }

}
