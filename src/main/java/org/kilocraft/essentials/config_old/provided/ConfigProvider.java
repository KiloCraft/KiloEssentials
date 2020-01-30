package org.kilocraft.essentials.config_old.provided;

import org.kilocraft.essentials.config_old.ConfigValueGetter;
import org.kilocraft.essentials.config_old.KiloConfigOLD;

public class ConfigProvider {
    private ConfigValueGetter main;
    private ConfigValueGetter messages;
    private ConfigValueGetter commands;

    public ConfigProvider() {
        main = new ConfigValueGetter(KiloConfigOLD.getFileConfigOfMain());
        messages = new ConfigValueGetter(KiloConfigOLD.getFileConfigOfMessages());
        //commands = new ConfigValueGetter(KiloConfig.getFileConfigOfCommands());
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
