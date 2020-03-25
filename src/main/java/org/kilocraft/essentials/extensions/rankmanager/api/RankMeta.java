package org.kilocraft.essentials.extensions.rankmanager.api;

public interface RankMeta extends Comparable<Integer> {
    boolean isAfter(RankMeta meta);
    boolean isBefore(RankMeta meta);
    int getWeight();
    String getPrefix();
    String getSuffix();
}
