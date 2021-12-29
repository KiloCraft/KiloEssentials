package org.kilocraft.essentials.mixin.patch.vanish;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(ListPlayersCommand.class)
public abstract class ListPlayersCommandMixin {

    @Redirect(
            method = "format",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"
            )
    )
    private static List<ServerPlayer> removeVanishedPlayers(PlayerList playerManager) {
        List<ServerPlayer> list = new ArrayList<>();
        for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList(false)) {
            list.add(user.asPlayer());
        }
        return list;
    }

}
