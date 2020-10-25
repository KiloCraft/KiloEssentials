package org.kilocraft.essentials.util;

import com.google.common.collect.Maps;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.util.text.Texter;

import java.util.Map;

public class LuckPermsCompatibility {
    private final Map<String, Team> teams;
    private LuckPerms api;
    private Scoreboard scoreboard;

    public LuckPermsCompatibility() {
        this.teams = Maps.newHashMap();
    }

    private void init(@NotNull final Server server) {
        this.api = LuckPermsProvider.get();
        this.scoreboard = server.getMinecraftServer().getScoreboard();

        for (Group group : api.getGroupManager().getLoadedGroups()) {
            addTeam(group);
        }
    }

    private Team addTeam(@NotNull final Group group) {
        String name = groupToTeam(group);
        if (name == null) {
            return null;
        }

        if (scoreboard.getTeam(name) != null) {
            return scoreboard.getTeam(name);
        }

        Team team = scoreboard.addTeam(name);
        teams.put(group.getIdentifier().toString(), team);

        if (group.getDisplayName() != null) {
            team.setDisplayName(Texter.newText(group.getDisplayName()));
        }
        return team;
    }

    public void onUserJoin(final OnlineServerUser user) {
        if (this.api == null) {
            this.init(KiloServer.getServer());
        }

        User luckUser = api.getUserManager().getUser(user.getId());
        if (luckUser == null) {
            return;
        }

        Group group = api.getGroupManager().getGroup(luckUser.getPrimaryGroup());
        if (group == null) {
            return;
        }

        String name = groupToTeam(group);
        Team team = teams.containsKey(name) ? teams.get(name) : addTeam(group);

        if (team != null) {
            this.scoreboard.addPlayerToTeam(user.asPlayer().getGameProfile().getName(), team);
        } else {
            KiloEssentials.getLogger().warn(
                    "LuckPermsCompatibility: Cannot add Player {} to the team {} for group {}",
                    user.getName(), name, group.getName()
            );
        }
    }

    private static String groupToTeam(@NotNull final Group group) {
        if (!group.getWeight().isPresent()) {
            return null;
        }
        String string = group.getWeight().getAsInt() + "_" + group.getName();
        return string.length() > 16 ? string.substring(0, 16) : string;
    }

}
