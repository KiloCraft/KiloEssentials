package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class RtpSpecsConfigSection {

    @Setting(value = "minimum")
    public int min = 15000;

    @Setting(value = "maximum")
    public int max = 25000;

    @Setting(value = "centerX")
    public int centerX = 0;

    @Setting(value = "centerZ")
    public int centerZ = 0;

    @Setting(value = "maxTries", comment = "The amount of time the server tries to find a random location, Default: 5")
    public int maxTries = 5;

    @Setting(value = "broadcast", comment = "Broadcasts a message when someone uses RTP, Values: %s, Leave it empty to disable it")
    public String broadcastMessage = "&4%s &cis taking a random teleport, expect some lag!";

    @Setting(value = "defaultAmount", comment = "The Default amount of RTPs for users")
    public int defaultRTPs = 3;

    @Setting(value = "blackListedBiomes", comment = "A list of biomes that are blacklisted for rtp locations")
    public List<String> blackListedBiomes = Arrays.asList("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:cold_ocean", "minecraft:deep_warm_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_frozen_ocean", "minecraft:deep_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:river", "minecraft:the_end", "minecraft:small_end_islands", "minecraft:end_barrens");

}
