package org.kilocraft.essentials.mixin.events;

import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerInteractItemStartEvent;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.events.player.PlayerClientCommandEventImpl;
import org.kilocraft.essentials.events.player.PlayerDisconnectEventImpl;
import org.kilocraft.essentials.events.player.PlayerInteractItemStartEventImpl;
import org.kilocraft.essentials.util.InteractionHandler;
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


    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(at = @At(value = "HEAD"), method = "onDisconnected")
    private void ke$triggerEvent$onDisconnect(Text text, CallbackInfo ci) {
        KiloServer.getServer().triggerEvent(new PlayerDisconnectEventImpl(this.player, KiloServer.getServer().getOnlineUser(this.player.getUuid())));
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
                this.player.getInventory().updateItems();
            } else {
                this.player.interactionManager.interactItem(this.player, serverWorld, itemStack, hand);
            }
        }
    }

    @Inject(method = "onClientCommand", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V"))
    private void modifyOnClientCommand(ClientCommandC2SPacket clientCommandC2SPacket, CallbackInfo ci) {
        if (KiloServer.getServer().triggerEvent(new PlayerClientCommandEventImpl(this.player, clientCommandC2SPacket.getMode())).isCancelled()) {
            ci.cancel();
        }
    }

}
