package org.kilocraft.essentials.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUserManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private static Logger LOGGER;

    @Inject(
            method = "onGameMessage", cancellable = true,
            at = @At(
                    value = "HEAD",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V"
            )
    )
    private void modify(ChatMessageC2SPacket chatMessageC2SPacket, CallbackInfo ci) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(this.player);

        if (!KiloConfig.main().chat().useVanillaChat) {
            ci.cancel();
            ((ServerUserManager) KiloServer.getServer().getUserManager()).onChatMessage(user, chatMessageC2SPacket);
        }
    }

//    @Redirect(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Z)V"))
//    private void redirect(PlayerManager playerManager, Text text, boolean bl) {
//        playerManager.broadcastChatMessage(new LiteralText(TextFormat.translate(Texter.Legacy.toFormattedString(text))), MessageType.CHAT, Util.field_25140);
//    }

    @Inject(
            method = "onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;)V", cancellable = true,
            at = @At(
                    value = "HEAD",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;)V"
            )
    )
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
                LOGGER.warn("Player {} just tried to change non-editable sign", this.player.getEntityName());
                return;
            }

            String[] strings = updateSignC2SPacket.getText();

            boolean canUseFormats = KiloEssentials.hasPermissionNode(this.player.getCommandSource(), EssentialPermission.SIGN_COLOR);
            for (int i = 0; i < strings.length; ++i) {
                String str = TextFormat.translate(strings[i], canUseFormats);
                signBlockEntity.setTextOnRow(i, new LiteralText(str));
            }

            signBlockEntity.markDirty();
            serverWorld.updateListeners(blockPos, blockState, blockState, 3);
        }
    }

    @Redirect(method = "onBookUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;"))
    public ListTag weDontNeedThesePages(CompoundTag compoundTag, String string, int i) {
        ListTag listTag = compoundTag.getList("pages", 8);
        boolean dupeAttempt = false;
        for (int j = 0; j < listTag.size(); j++) {
            Tag tag = listTag.get(j);
            if (tag instanceof StringTag) {
                StringTag stringTag = (StringTag) tag;
                final String s = stringTag.asString();
                if (s.length() > 300) {
                    stringTag = StringTag.of(s.substring(0, 300));
                    listTag.set(j, stringTag);
                    dupeAttempt = true;
                }
            }
        }
        if (dupeAttempt) KiloEssentials.getLogger().warn(player.getEntityName() + " attempted to dupe!");
        return listTag;
    }

}
