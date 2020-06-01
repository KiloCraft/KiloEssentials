package org.kilocraft.essentials.mixin.events;

import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.*;
import org.kilocraft.essentials.events.player.*;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler$PlayerEvents {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private Vec3d requestedTeleportPos;

    private static boolean shouldContinueUsingItem(ServerPlayerEntity serverPlayerEntity, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else {
            Item item = itemStack.getItem();
            return (item instanceof BlockItem || item instanceof BucketItem) && !serverPlayerEntity.getItemCooldownManager().isCoolingDown(item);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "onDisconnected")
    private void ke$triggerEvent$onDisconnect(Text text, CallbackInfo ci) {
        KiloServer.getServer().triggerEvent(new PlayerDisconnectEventImpl(this.player));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"), method = "onDisconnected")
    private void ke$remove$sendToAll(PlayerManager playerManager, Text text, MessageType messageType, UUID uUID) {
        //Ignored
    }

    @Inject(method = "onPlayerInteractItem", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onPlayerInteractItem(Lnet/minecraft/network/packet/c2s/play/PlayerInteractItemC2SPacket;)V"))
    private void ke$modify$onPlayerInteractItem(PlayerInteractItemC2SPacket playerInteractItemC2SPacket, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(playerInteractItemC2SPacket, (ServerPlayPacketListener) this, this.player.getServerWorld());
        ServerWorld serverWorld = this.server.getWorld(RegistryUtils.dimensionTypeToRegistryKey(this.player.getServerWorld().getDimension()));
        Hand hand = playerInteractItemC2SPacket.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        this.player.updateLastActionTime();

        if (!itemStack.isEmpty()) {
            PlayerInteractItemStartEvent event = new PlayerInteractItemStartEventImpl(
                    player, player.getEntityWorld(), playerInteractItemC2SPacket.getHand(), player.getStackInHand(playerInteractItemC2SPacket.getHand())
            );

            if (KiloServer.getServer().triggerEvent(event).isCancelled()) {
                this.player.updateLastActionTime();
                this.player.inventory.updateItems();
            } else {
                this.player.interactionManager.interactItem(this.player, serverWorld, itemStack, hand);
            }
        }
    }

    @Inject(method = "onPlayerInteractBlock", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onPlayerInteractBlock(Lnet/minecraft/network/packet/c2s/play/PlayerInteractBlockC2SPacket;)V"))
    private void ke$modify$onInteractBlock(PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(playerInteractBlockC2SPacket, player.networkHandler, this.player.getServerWorld());
        ServerWorld serverWorld = this.player.getServerWorld();
        Hand hand = playerInteractBlockC2SPacket.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        BlockHitResult blockHitResult = playerInteractBlockC2SPacket.getHitY();
        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getSide();
        this.player.updateLastActionTime();

        PlayerInteractBlockEvent event = new PlayerInteractBlockEventImpl(player, playerInteractBlockC2SPacket.getHitY(), hand);
        KiloServer.getServer().triggerEvent(event);
        if (!event.isCancelled()) {
            if (blockPos.getY() < this.server.getWorldHeight()) {
                if (this.requestedTeleportPos == null && this.player.squaredDistanceTo((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) < 64.0D && serverWorld.canPlayerModifyAt(this.player, blockPos)) {
                    ActionResult actionResult = this.player.interactionManager.interactBlock(this.player, serverWorld, itemStack, hand, blockHitResult);
                    if (direction == Direction.UP && actionResult != ActionResult.SUCCESS && blockPos.getY() >= this.server.getWorldHeight() - 1 && shouldContinueUsingItem(this.player, itemStack)) {
                        Text text = (new TranslatableText("build.tooHigh", this.server.getWorldHeight())).formatted(Formatting.RED);
                        this.player.networkHandler.sendPacket(new GameMessageS2CPacket(text, MessageType.GAME_INFO, Util.field_25140));
                    } else if (actionResult.shouldSwingHand()) {
                        this.player.swingHand(hand, true);
                    }
                }
            } else {
                Text text = (new TranslatableText("build.tooHigh", this.server.getWorldHeight())).formatted(Formatting.RED);
                this.player.networkHandler.sendPacket(new GameMessageS2CPacket(text, MessageType.GAME_INFO, Util.field_25140));
            }
        }

        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos));
        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos.offset(direction)));
    }

    @Inject(method = "onClientCommand", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V"))
    private void modifyOnClientCommand(ClientCommandC2SPacket clientCommandC2SPacket, CallbackInfo ci) {
        if (KiloServer.getServer().triggerEvent(new PlayerClientCommandEventImpl(this.player, clientCommandC2SPacket.getMode())).isCancelled()) {
            ci.cancel();
        }
    }

}
