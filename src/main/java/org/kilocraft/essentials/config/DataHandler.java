package org.kilocraft.essentials.config;

import org.kilocraft.essentials.KiloEssentials;
import org.kilocraft.essentials.Mod;

import java.io.File;
import java.util.ArrayList;

public class DataHandler {
    public static void handle() {
        ArrayList<String> dataFiles = new ArrayList<String>(){{
            add("CachedData.json");
            add("onFlymode.json");
            add("onVanishmode.json");
            add("onGodmode.json");
            add("onStaffchat.json");
        }};

        dataFiles.forEach((file) -> new ConfigFile(
                file,
                "^KiloEssentials^Data".replace("^", File.separator),
                "DataFiles",
                false,
                false
        ));

        KiloEssentials.getLogger.info(String.format(Mod.lang.getProperty("datahandler.load.successfull"), dataFiles.size()));
        KiloEssentials.getLogger.info(dataFiles.toString());
    }
}
