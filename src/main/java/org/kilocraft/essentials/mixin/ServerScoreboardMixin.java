package org.kilocraft.essentials.mixin;

import net.minecraft.network.Packet;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.PlayerManager;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {

    @Redirect(method = "addPlayerToTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void ke$changeTabOrder$AddToTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "removePlayerFromTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void ke$changeTabOrder$RemoveFromTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "updateScoreboardTeamAndPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void ke$changeTabOrder$UpdateTeamAndPlayers(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "updateScoreboardTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void ke$changeTabOrder$UpdateTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "updateRemovedTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void ke$changeTabOrder$UpdateRemovedTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }



}
