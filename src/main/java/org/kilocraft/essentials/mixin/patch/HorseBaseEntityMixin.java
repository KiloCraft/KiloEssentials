package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@Mixin(HorseBaseEntity.class)
public class HorseBaseEntityMixin {

    HashMap<UUID, ArrayList<UUID>> horseToPlayers = new HashMap<>();

    @Inject(method = "openInventory", at = @At("HEAD"))
    public void onOpenInventory(PlayerEntity player, CallbackInfo info) {
        HorseBaseEntity horse = (HorseBaseEntity) (Object) this;
        ArrayList<UUID> players = horseToPlayers.getOrDefault(horse.getUuid(), new ArrayList<>());
        players.add(player.getUuid());
        horseToPlayers.put(horse.getUuid(), players);
    }

    @Inject(method = "putPlayerOnBack", at = @At("HEAD"))
    public void closeInventories(PlayerEntity player, CallbackInfo info) {
        if (!ServerSettings.getBoolean("patch.donkey_dupe")) return;
        HorseBaseEntity horse = (HorseBaseEntity) (Object) this;
        ArrayList<UUID> players = horseToPlayers.getOrDefault(horse.getUuid(), new ArrayList<>());
        for (UUID uuid : players) {
            ServerPlayerEntity serverPlayerEntity = player.getServer().getPlayerManager().getPlayer(uuid);
            if (serverPlayerEntity != null) serverPlayerEntity.closeHandledScreen();
        }
    }
}
