package org.kilocraft.essentials.extensions.rankmanager.api;

import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.extensions.rankmanager.api.permission.RankPermissions;

public interface Rank {
    String getId();
    @Nullable String getDisplayName();
    RankMeta getMeta();
    RankPermissions getPermission();
}
