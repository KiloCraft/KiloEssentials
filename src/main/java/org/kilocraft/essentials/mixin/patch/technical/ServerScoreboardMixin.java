package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.network.Packet;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.PlayerManager;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin {

    @Redirect(method = "addPlayerToTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void changeTabOrderAddPlayerToTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "removePlayerFromTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void changeTabOrderRemovePlayerFromTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "updateScoreboardTeamAndPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void changeTabOrderUpdateTeamAndPlayers(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "updateScoreboardTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void changeTabOrderUpdateScoreboardTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }

    @Redirect(method = "updateRemovedTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void changeTabOrderRemovedTeam(PlayerManager playerManager, Packet<?> packet) {
        if (!KiloConfig.main().playerList().customOrder) playerManager.sendToAll(packet);
    }



}
