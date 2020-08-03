package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReadyEvent;
import org.kilocraft.essentials.api.feature.ConfigurableFeatures;
import org.kilocraft.essentials.commands.misc.DiscordCommand;
import org.kilocraft.essentials.commands.misc.VoteCommand;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.extensions.customcommands.CustomCommands;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.extensions.playtimecommands.PlaytimeCommands;
import org.kilocraft.essentials.extensions.votifier.Votifier;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.ServerWarpManager;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.WarpCommand;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

public class OnServerReady implements EventHandler<ServerReadyEvent> {
    @Override
    public void handle(@NotNull ServerReadyEvent event) {
        NBTStorageUtil.onLoad();
        BrandedServer.set();
        KiloServer.getServer().getMetaManager().load();
        WarpCommand.registerAliases();
        KiloServer.getServer().setName(KiloConfig.main().server().name);
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onServerReady();

        ConfigurableFeatures features = new ConfigurableFeatures();
        features.register(new UserHomeHandler(), "playerHomes");
        features.register(new ServerWarpManager(), "serverWideWarps");
        features.register(new PlayerWarpsManager(), "playerWarps");
        features.register(new SeatManager(), "betterChairs");
        features.register(new CustomCommands(), "customCommands");
        features.register(new ParticleAnimationManager(), "magicalParticles");
        features.register(new DiscordCommand(), "discordCommand");
        features.register(new VoteCommand(), "voteCommand");
        features.register(new PlaytimeCommands(), "playtimeCommands");
        features.register(new Votifier(), "votifier");

        KiloEssentials.getInstance().getFeatures().loadAll(false);
    }
}
