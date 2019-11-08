package org.kilocraft.essentials.customcommands;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.config.ConfigIOProvider;
import org.kilocraft.essentials.config.KiloConifg;
import org.kilocraft.essentials.config.provided.ConfigProvider;
import org.kilocraft.essentials.registry.ConfigurableFeature;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandManager implements ConfigIOProvider, ConfigurableFeature {
    private static FileConfig fileConfig = FileConfig.of(KiloConifg.getConfigPath() + "/Commands.yaml");
    private static HashMap<String, Command> commandsMap = new HashMap<>();

    private CommandManager() {
        ArrayList<String> list = new ArrayList<>();


    }

    @Override
    public boolean register() {
        new CommandManager();
        KiloConifg.registerIOCallBaack(this);
        return true;
    }

    @Override
    public void toConifg(ConfigProvider config) {

    }

    @Override
    public void fromConfig(ConfigProvider config) {

    }
}
