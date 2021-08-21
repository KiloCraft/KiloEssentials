package org.kilocraft.essentials.mixin.vanish;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ListCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(ListCommand.class)
public abstract class ListCommandMixin {

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;getPlayerList()Ljava/util/List;"))
    private static List<ServerPlayerEntity> removeVanishedPlayers(PlayerManager playerManager) {
        List<ServerPlayerEntity> list = new ArrayList<>();
        for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList(false)) {
            list.add(user.asPlayer());
        }
        return list;
    }

}
