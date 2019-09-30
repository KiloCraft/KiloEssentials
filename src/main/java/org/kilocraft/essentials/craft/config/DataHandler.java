package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.config.ConfigFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    private static final String dir = "^KiloEssentials^data^".replace("^", File.separator);
    private List<String> dataFiles = new ArrayList<String>(){{
        add("homes.dat");
    }};

    public DataHandler() {
        dataFiles.forEach((name) -> new ConfigFile(
                name,
                dir,
                "",
                false,
                false
        ));


        Mod.getLogger().info("Loading the data files...");
    }

}
