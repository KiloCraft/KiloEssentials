package org.kilocraft.essentials.extensions.playtimecommands;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.feature.ReloadableConfigurableFeature;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.playtimecommands.config.PlaytimeCommandConfigSection;
import org.kilocraft.essentials.extensions.playtimecommands.config.PlaytimeCommandsConfig;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.text.Texter;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Path;

public class PlaytimeCommands implements ReloadableConfigurableFeature {
    private static boolean enabled = false;
    private static PlaytimeCommands instance = null;
    private static PlaytimeCommandsConfig config;

    @Override
    public boolean register() {
        enabled = true;
        instance = this;

        this.load();
        return true;
    }

    @Override
    public void load() {
        Path path = KiloEssentials.getEssentialsPath().resolve("playtimeCommands.conf");
        final HoconConfigurationLoader hoconLoader = HoconConfigurationLoader.builder()
                .path(path)
                .build();
        try {
            final CommentedConfigurationNode rootNode = hoconLoader.load(KiloConfig.configurationOptions().header(PlaytimeCommandsConfig.HEADER));
            config = rootNode.get(PlaytimeCommandsConfig.class, new PlaytimeCommandsConfig());
            if (!path.toFile().exists()) hoconLoader.save(rootNode);
        } catch (ConfigurateException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file!", e);
        }
    }

    public void onUserPlaytimeUp(OnlineServerUser user, int ticksPlayed) {
        int played = ticksPlayed / 20;

        for (PlaytimeCommandConfigSection section : config.sections) {
            if (played == section.seconds) {
                for (String command : section.commands) {
                    this.runCommand(user, command);
                }
            }
        }
    }

    private void runCommand(OnlineServerUser user, String command) {
        String cmd = command.replace("${user.name}", user.getUsername())
                .replace("${user.displayname}", user.getFormattedDisplayName())
                .replace("${user.ranked_displayname}", Texter.Legacy.toFormattedString(user.getRankedDisplayName()));

        CommandUtils.runCommandWithFormatting(user.getCommandSource(), cmd);
    }

    public static PlaytimeCommands getInstance() {
        if (!enabled || instance == null) {
            throw new IllegalStateException("EventCommands is not enabled yet!");
        }

        return instance;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
