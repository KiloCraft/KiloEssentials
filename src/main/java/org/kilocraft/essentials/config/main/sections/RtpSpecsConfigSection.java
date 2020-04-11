package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RtpSpecsConfigSection {

    @Setting(value = "minimumX")
    public int minX = -15000;

    @Setting(value = "minimumZ")
    public int minZ = -15000;

    @Setting(value = "maximumX")
    public int maxX = 30000;

    @Setting(value = "maximumZ")
    public int maxZ = 30000;

    @Setting(value = "maxTries", comment = "The amount of time the server tries to find a random location, Default: 20")
    public int maxTries = 20;

    @Setting(value = "broadcast", comment = "Broadcasts a message when someone uses RTP, Values: %s, Leave it empty to disable it")
    public String broadcastMessage = "&4%s &cis taking a random teleport, expect some lag!";

    @Setting(value = "defaultAmount", comment = "The Default amount of RTPs for users")
    public int defaultRTPs = 3;

}
