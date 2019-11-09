package org.kilocraft.essentials.config.provided;

import org.kilocraft.essentials.config.ConfigValueGetter;
import org.kilocraft.essentials.config.KiloConfig;

public class ConfigProvider {
    private ConfigValueGetter main;
    private ConfigValueGetter messages;
    private ConfigValueGetter commands;

    public ConfigProvider() {
        main = new ConfigValueGetter(KiloConfig.getFileConfigOfMain());
        messages = new ConfigValueGetter(KiloConfig.getFileConfigOfMessages());
        commands = new ConfigValueGetter(KiloConfig.getFileConfigOfCommands());
    }

    public ConfigValueGetter getMain() {
        return main;
    }

    public ConfigValueGetter getMessages() {
        return messages;
    }

    public ConfigValueGetter getCommands() {
        return commands;
    }

}
