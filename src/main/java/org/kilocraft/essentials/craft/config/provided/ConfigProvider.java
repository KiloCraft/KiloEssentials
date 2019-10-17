package org.kilocraft.essentials.craft.config.provided;

import org.kilocraft.essentials.craft.config.KiloConifg;

public class ConfigProvider {
    private ConfigValueGetter main;
    private ConfigValueGetter messages;

    public ConfigProvider() {
        main = new ConfigValueGetter(KiloConifg.getFileConfigOfMain());
        messages = new ConfigValueGetter(KiloConifg.getFileConfigOfMessages());
    }

    public ConfigValueGetter getMain() {
        return main;
    }

    public ConfigValueGetter getMessages() {
        return messages;
    }

}
