package org.kilocraft.essentials.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class FeaturesConfigSection {
    @Setting(value = "playerHomes", comment = "Let players to set homes where ever they want!")
    public boolean playerHomes = true;

    @Setting(value = "serverWideWarps", comment = "Set different warps and allow players to teleport to them!")
    public boolean serverWideWarps = true;

    @Setting(value = "betterChairs", comment = "Enjoy seating on stairs and slabs! You can sit using \"/sit on\" command")
    public boolean betterChairs = true;

    @Setting(value = "magicalParticles", comment = "Animated Magical Particles, You can use the \"/mp\" command for more info")
    public boolean magicalParticles = true;
}
