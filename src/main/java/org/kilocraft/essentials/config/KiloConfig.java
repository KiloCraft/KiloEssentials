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

/**
 * KiloConfig - Powered by SpongePowered Configurate
 *
 * @version 2.0
 * @author CODY_AI (OnBlock)
 * @see Config
 * @see Messages
 */

public class KiloConfig {
    private static Config config;
    private static Messages messages;
    private static ConfigurationNode mainNode;
    private static ConfigurationNode messagesNode;

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
        String msg = messagesNode.getNode((Object) key.split("\\.")).getString();
        return objects.length == 0 ? msg : msg != null ? String.format(msg, objects) : "Null<" + key + "?>";
    }

    public static void load() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("essentials.conf", KiloEssentials.getEssentialsPath());
            KiloFile MESSAGES_FILE = new KiloFile("messages.conf", KiloEssentials.getEssentialsPath());

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
            KiloEssentials.getLogger().error("Exception handling a configuration file! " + KiloConfig.class.getName());
            e.printStackTrace();
        }
    }

    public static ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults()
                .setHeader(Config.HEADER)
                .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                .setShouldCopyDefaults(true);
    }

}
