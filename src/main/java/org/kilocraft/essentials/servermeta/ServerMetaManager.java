package org.kilocraft.essentials.servermeta;

import io.netty.buffer.Unpooled;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerMetaManager {

    //TODO refactor

    static List<Team> cachedTeams = new ArrayList<>();

    public static void updateAll() {
        KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList().forEach(ServerMetaManager::update);
    }

    public static void updateDisplayName(ServerPlayerEntity player) {
        if (player != null) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
            KiloEssentials.getInstance().sendGlobalPacket(packet);
        }
    }

    public static void onPlayerJoined(ServerPlayerEntity player) {
        ServerMetaManager.update(player);
    }

    public static void updateForAll() {
        if (KiloConfig.main().playerList().customOrder)
            updateForAll(KiloEssentials.getMinecraftServer().getScoreboard());
    }


    private static void updateForAll(ServerScoreboard scoreboard) {
        List<Team> teams = getTeams(scoreboard);
        for (Team cachedTeam : cachedTeams) {
            KiloEssentials.getMinecraftServer().getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(cachedTeam));
        }
        cachedTeams.clear();
        for (Team team : teams) {
            KiloEssentials.getMinecraftServer().getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
            cachedTeams.addAll(teams);
        }
    }

    public static void update(ServerPlayerEntity player) {
        if (player == null || player.networkHandler == null) return;

        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getHeader())),
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getFooter()))
        );

        player.networkHandler.sendPacket(packet);
    }

    private static String formatFor(@NotNull final ServerPlayerEntity player, @NotNull final String string) {
        final OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        String s = ConfigVariableFactory.replaceServerVariables(string);
        return ConfigVariableFactory.replaceOnlineUserVariables(s, user);
    }

    public static List<Team> getTeams(ServerScoreboard scoreboard) {
        HashMap<Long, Team> weightToTeam = new HashMap<>();
        ArrayList<Team> teams = new ArrayList<>();
        try {
            LuckPerms api = LuckPermsProvider.get();
            for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
                User luckUser = api.getUserManager().getUser(player.getUuid());
                if (luckUser == null) continue;
                Group group = api.getGroupManager().getGroup(luckUser.getPrimaryGroup());
                if (group == null) continue;
                long weight = group.getWeight().isPresent() ? group.getWeight().getAsInt() : 0;
                Team team;
                if (!weightToTeam.containsKey(weight)) {
                    String name = String.format("%016d", KiloConfig.main().playerList().topToBottom ? weight : 1000000000000000L - weight);
                    team = new Team(scoreboard, name);
                    weightToTeam.put(weight, team);
                } else {
                    team = weightToTeam.get(weight);
                }
                team.getPlayerList().add(player.getEntityName());
            }
            for (Map.Entry<Long, Team> entry : weightToTeam.entrySet()) {
                teams.add(entry.getValue());
            }
            return teams;
        } catch (NoClassDefFoundError error) {
            return teams;
        }
    }


}
