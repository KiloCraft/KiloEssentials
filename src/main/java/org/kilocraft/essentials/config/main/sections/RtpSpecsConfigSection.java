package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RtpSpecsConfigSection {

    @Setting(value = "simpleRTP", comment = "Simple rtp just generates random coordinates and drops the player from the sky instead of heavy calculations")
    public boolean simpleRTP = false;

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

    @Setting(value = "showTries", comment = "If set to true you will see the amount of tries in the action bar while performing an RTP")
    public boolean showTries = true;

}
