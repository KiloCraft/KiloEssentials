package org.kilocraft.essentials.craft.customcommands;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.KiloConfig;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomCommands {
    private static FileConfig config = KiloConfig.getCustomCommands();
    private HashMap<String, CCommand> commandsMap = new HashMap<>();

    public static void handle() {
        config.load();
        new CustomCommands();
    }

    private CustomCommands() {
        ArrayList<String> list = new ArrayList<>();
        Object configCommands = config.get("Commands");


        System.out.println(configCommands);

        config.close();
    }
}
