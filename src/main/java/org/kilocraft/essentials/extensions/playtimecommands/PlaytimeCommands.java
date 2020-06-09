package org.kilocraft.essentials.extensions.playtimecommands;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.feature.RelodableConfigurableFeature;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.extensions.playtimecommands.config.PlaytimeCommandConfigSection;
import org.kilocraft.essentials.extensions.playtimecommands.config.PlaytimeCommandsConfig;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.util.text.Texter;

import java.io.IOException;

public class PlaytimeCommands implements RelodableConfigurableFeature {
    private static boolean enabled = false;
    private static PlaytimeCommands instance = null;
    private static PlaytimeCommandsConfig config;

    @Override
    public boolean register() {
        enabled = true;
        instance = this;

        load();
        return true;
    }

    @Override
    public void load() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("playtimeCommands.hocon", KiloEssentials.getEssentialsPath());
            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.createFile();
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();

            ConfigurationNode configNode = loader.load(ConfigurationOptions.defaults()
                    .setHeader(PlaytimeCommandsConfig.HEADER)
                    .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                    .setShouldCopyDefaults(true));

            config = configNode.getValue(TypeToken.of(PlaytimeCommandsConfig.class), new PlaytimeCommandsConfig());

            loader.save(configNode);
        } catch (IOException | ObjectMappingException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file! " + PlaytimeCommandsConfig.class.getName());
            e.printStackTrace();
        }
    }

    public void onUserPlaytimeUp(OnlineServerUser user, int ticksPlayed) {
        int played = ticksPlayed / 20;

        for (PlaytimeCommandConfigSection section : config.sections) {
            if (played == section.seconds) {
                for (String command : section.commands) {
                    runCommand(user, command);
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
