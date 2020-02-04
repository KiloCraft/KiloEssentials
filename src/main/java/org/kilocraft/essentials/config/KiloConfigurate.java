package org.kilocraft.essentials.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.provided.KiloFile;

import java.io.IOException;

public class KiloConfigurate {
    private static Config config;
    private static String configPath = KiloEssentials.getConfigDirectory();

    public KiloConfigurate() {

        try {
            KiloFile CONFIG_FILE = new KiloFile("config.hocon", KiloEssentials.getEssentialsDirectory());

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();

            CONFIG_FILE.createFile();
            ConfigurationNode rootNode = loader.load(configurationOptions());

            config = rootNode.getValue(TypeToken.of(Config.class), new Config());

            loader.save(rootNode);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }

    }

    public static Config main() {
        return config;
    }

    private ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults()
                .setHeader(Config.HEADER)
                .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                .setShouldCopyDefaults(true);
    }

}
