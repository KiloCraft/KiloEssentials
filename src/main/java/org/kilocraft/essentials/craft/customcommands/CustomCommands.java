package org.kilocraft.essentials.craft.customcommands;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.KiloConfig;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomCommands implements ConfigurableFeature {
    private static FileConfig config = KiloConfig.getCustomCommands();
    private HashMap<String, CCommand> commandsMap = new HashMap<>();

    public static void handle() {
        new CustomCommands();
    }

    public CustomCommands() {
        ArrayList<String> list = new ArrayList<>();
        Object configCommands = config.get("Commands");


        System.out.println(configCommands);

    }

    @Override
    public boolean register() {
        handle();
        return true;
    }
}
