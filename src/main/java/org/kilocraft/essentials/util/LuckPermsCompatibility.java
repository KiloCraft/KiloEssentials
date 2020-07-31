package org.kilocraft.essentials.util;

import com.google.common.collect.Maps;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
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
            if (!group.getWeight().isPresent()) {
                continue;
            }
            String string = group.getWeight().getAsInt() + "_" + group.getName();
            String name = string.length() > 16 ? string.substring(0, 16) : string;

            if (scoreboard.getTeam(name) != null) {
                continue;
            }

            Team team = scoreboard.addTeam(name);

            if (group.getDisplayName() != null) {
                team.setDisplayName(Texter.newText(group.getDisplayName()));
            }

            teams.put(group.getIdentifier().toString(), team);
        }
    }

    public void onUserJoin(final OnlineServerUser user) {
        if (this.api == null) {
            this.init(KiloServer.getServer());
        }

        User luckUser = api.getUserManager().getUser(user.getId());
        if (luckUser == null) {
            return;
        }

        if (teams.containsKey(luckUser.getPrimaryGroup())) {
            Team team = teams.get(luckUser.getPrimaryGroup());
            if (team != null) {
                this.scoreboard.addPlayerToTeam(user.asPlayer().getGameProfile().getName(), team);
            }
        }
    }

}
