package org.kilocraft.essentials.extensions.rankmanager.api;

import org.jetbrains.annotations.Nullable;

public interface RankMeta extends Comparable<RankMeta> {
    boolean isAfter(RankMeta meta);
    boolean isBefore(RankMeta meta);

    int getWeight();

    void setWeight(int weight);

    @Nullable String getPrefix();
    @Nullable String getSuffix();

    void setPrefix(String prefix);
    void setSuffix(String suffix);
}
