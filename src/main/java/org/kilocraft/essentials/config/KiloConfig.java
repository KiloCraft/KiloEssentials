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
import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.config.messages.Messages;
import org.kilocraft.essentials.provided.KiloFile;

import java.io.IOException;

public class KiloConfig {
    private static Config config;
    private static Messages messages;
    private static String configPath = KiloEssentials.getEssentialsDirectory();
    private static ConfigurationNode mainNode;
    private static ConfigurationNode messagesNode;

    public KiloConfig() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("config.hocon", configPath);
            KiloFile MESSAGES_FILE = new KiloFile("messages.hocon", configPath);

            ConfigurationLoader<CommentedConfigurationNode> mainLoader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();
            ConfigurationLoader<CommentedConfigurationNode> messagesLoader = HoconConfigurationLoader.builder()
                    .setFile(MESSAGES_FILE.getFile()).build();

            CONFIG_FILE.createFile();
            MESSAGES_FILE.createFile();

            mainNode = mainLoader.load(configurationOptions());
            messagesNode = messagesLoader.load(configurationOptions());

            config = mainNode.getValue(TypeToken.of(Config.class), new Config());
            messages = messagesNode.getValue(TypeToken.of(Messages.class), new Messages());

            mainLoader.save(mainNode);
            messagesLoader.save(messagesNode);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }

    }

    public static Config main() {
        return config;
    }

    public static Messages messages() {
        return messages;
    }

    public static ConfigurationNode getMainNode() {
        return mainNode;
    }

    public static ConfigurationNode getMessagesNode() {
        return messagesNode;
    }

    public static String getMessage(String key, Object... objects) {
        return objects.length == 0 ? messagesNode.getString(key) : String.format(messagesNode.getString(key), objects);
    }


    private ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults()
                .setHeader(Config.HEADER)
                .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                .setShouldCopyDefaults(true);
    }

}
