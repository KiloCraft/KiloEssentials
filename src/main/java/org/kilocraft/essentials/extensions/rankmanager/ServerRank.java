package org.kilocraft.essentials.extensions.rankmanager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.extensions.rankmanager.api.Rank;
import org.kilocraft.essentials.extensions.rankmanager.api.RankMeta;
import org.kilocraft.essentials.extensions.rankmanager.api.permission.RankPermissions;

public class ServerRank implements Rank {
    private final String id;
    private String displayName;
    private RankMeta meta;
    private RankPermissions permission;

    public ServerRank(@NotNull final String id, @Nullable final String displayName, final int weight) {
        this.id = id;
        this.displayName = displayName;
        this.meta = new ServerRankMeta(weight);
        this.permission = new ServerRankPermissions();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public RankMeta getMeta() {
        return this.meta;
    }

    @Override
    public RankPermissions getPermission() {
        return this.permission;
    }
}
