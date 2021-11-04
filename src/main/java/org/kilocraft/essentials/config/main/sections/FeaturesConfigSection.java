package org.kilocraft.essentials.config.main.sections;


import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class FeaturesConfigSection {
    @Setting(value = "playtimeCommands")
    @Comment("Schedule commands to run at a specific amount of playtime!")
    public boolean playtimeCommands = true;

    @Setting(value = "playerHomes")
    @Comment("Let players to set homes where ever they want! **WARNING: YOU WILL LOSE ALL THE HOME DATA IF YOU DISABLE THIS AND RE-LOAD THE SERVER")
    public boolean playerHomes = true;

    @Setting(value = "serverWideWarps")
    @Comment("Set different warps and allow players to teleport to them!")
    public boolean serverWideWarps = true;

    @Setting(value = "playerWarps")
    @Comment("Allow players to set Warps!")
    public boolean playerWarps = true;

    @Setting(value = "betterChairs")
    @Comment("Enjoy seating on stairs and slabs! You can sit using \"/sit on\" command")
    public boolean betterChairs = true;

    @Setting(value = "magicalParticles")
    @Comment("Animated Magical Particles, You can use the \"/mp\" command for more info")
    public boolean magicalParticles = true;

    @Setting(value = "customCommands")
    @Comment("Add custom commands for your server!")
    public boolean customCommands = true;

}
