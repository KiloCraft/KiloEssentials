package org.kilocraft.essentials.config;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.provided.KiloFile;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.MapFactories;

import java.io.IOException;
import java.nio.file.Path;

/**
 * KiloConfig - Powered by SpongePowered Configurate
 *
 * @author CODY_AI (OnBlock)
 * @version 2.0
 * @see Config
 */

public class KiloConfig {
    private static Config config;
    private static ConfigurationNode rootNode;

    public static Config main() {
        return config;
    }

    public static ConfigurationNode getRootNode() {
        return rootNode;
    }

    public static void load() {
        Path path = KiloEssentials.getEssentialsPath().resolve("essentials.conf");
        final HoconConfigurationLoader hoconLoader = HoconConfigurationLoader.builder()
                .path(path)
                .build();
        try {
            rootNode = hoconLoader.load(configurationOptions());
            config = rootNode.get(Config.class, new Config());
            if (!path.toFile().exists()) hoconLoader.save(rootNode);
        } catch (ConfigurateException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file!", e);
        }
    }

    public static ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults()
                .header(Config.HEADER)
                .mapFactory(MapFactories.sortedNatural())
                .shouldCopyDefaults(true);
    }

}
