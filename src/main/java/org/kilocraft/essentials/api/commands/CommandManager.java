package org.kilocraft.essentials.api.commands;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.config.ConfigIOProvider;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.provided.ConfigProvider;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandManager implements ConfigIOProvider, ConfigurableFeature {
    private static FileConfig fileConfig = FileConfig.of(KiloConfig.getConfigPath() + "/Commands.yaml");
    private static HashMap<String, Command> commandsMap = new HashMap<>();

    private CommandManager() {
        ArrayList<String> list = new ArrayList<>();

    }

    @Override
    public boolean register() {
        new CommandManager();
        KiloConfig.registerIOCallBaack(this);
        return true;
    }

    @Override
    public void toConfig(ConfigProvider config) {

    }

    @Override
    public void fromConfig(ConfigProvider config) {

    }
}
