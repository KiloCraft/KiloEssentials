package org.kilocraft.essentials.extensions.rankmanager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.extensions.rankmanager.api.RankMeta;

public class ServerRankMeta implements RankMeta {
    private int weight;
    private String prefix;
    private String suffix;

    public ServerRankMeta(final int weight) {
        this(weight, null, null);
    }

    public ServerRankMeta(final int weight, @Nullable final String prefix, @Nullable final String suffix) {
        this.weight = weight;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public boolean isAfter(RankMeta meta) {
        return meta.getWeight() < this.weight;
    }

    @Override
    public boolean isBefore(RankMeta meta) {
        return meta.getWeight() > this.weight;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Nullable
    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Nullable
    @Override
    public String getSuffix() {
        return this.suffix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public int compareTo(@NotNull RankMeta o) {
        return Integer.compare(o.getWeight(), this.weight);
    }
}
