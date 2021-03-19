package org.kilocraft.essentials.mixin.events;

import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerInteractItemStartEvent;
import org.kilocraft.essentials.events.player.PlayerClientCommandEventImpl;
import org.kilocraft.essentials.events.player.PlayerDisconnectEventImpl;
import org.kilocraft.essentials.events.player.PlayerInteractItemStartEventImpl;
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
    long lastInteraction = 0;
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

    //TODO:
/*    @Inject(method = "onPlayerInteractEntity", at = @At(value = "HEAD"))
    private void ke$onPlayerInteractEntity(PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket, CallbackInfo ci) {
        if (lastInteraction + 50 > Util.getMeasuringTimeMs()) return;
        NetworkThreadUtils.forceMainThread(playerInteractEntityC2SPacket, (ServerPlayNetworkHandler) (Object) this, this.player.getServerWorld());
        ServerWorld serverWorld = this.player.getServerWorld();
        Entity entity = playerInteractEntityC2SPacket.getEntity(serverWorld);
        if (entity != null) {
            EntityDataObject entityDataObject = new EntityDataObject(entity);
            CompoundTag tag = entityDataObject.getTag();
            String command = tag.getString("command");
            playerInteractEntityC2SPacket.method_34209();
            String specificCommand = playerInteractEntityC2SPacket.getType() ==
                    PlayerInteractEntityC2SPacket.InteractionType.ATTACK ?
                    tag.getString("leftCommand") :
                    tag.getString("rightCommand");
            boolean success = false;
            if (!command.equals("")) {
                CommandUtils.runCommandWithFormatting(this.player.getCommandSource(), command);
                success = true;
            }
            if (!specificCommand.equals("")) {
                CommandUtils.runCommandWithFormatting(this.player.getCommandSource(), specificCommand);
                success = true;
            }
            if (success) this.player.swingHand(playerInteractEntityC2SPacket.getHand(), true);
        }
        lastInteraction = Util.getMeasuringTimeMs();
    }*/


    @Inject(method = "onClientCommand", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V"))
    private void modifyOnClientCommand(ClientCommandC2SPacket clientCommandC2SPacket, CallbackInfo ci) {
        if (KiloServer.getServer().triggerEvent(new PlayerClientCommandEventImpl(this.player, clientCommandC2SPacket.getMode())).isCancelled()) {
            ci.cancel();
        }
    }

}
