package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class FeaturesConfigSection {
    @Setting(value = "playerHomes", comment = "Let players to set homes where ever they want!")
    public boolean playerHomes = true;

    @Setting(value = "serverWideWarps", comment = "Set different warps and allow players to teleport to them!")
    public boolean serverWideWarps = true;

    @Setting(value = "playerWarps", comment = "Allow players to set Warps!")
    public boolean playerWarps = true;

    @Setting(value = "betterChairs", comment = "Enjoy seating on stairs and slabs! You can sit using \"/sit on\" command")
    public boolean betterChairs = true;

    @Setting(value = "magicalParticles", comment = "Animated Magical Particles, You can use the \"/mp\" command for more info")
    public boolean magicalParticles = true;

    @Setting(value = "customCommands", comment = "Add custom commands for your server!")
    public boolean customCommands = true;

    @Setting(value = "voteCommand")
    public boolean voteCommand = true;

    @Setting(value = "discordCommand")
    public boolean discordCommand = true;
}
