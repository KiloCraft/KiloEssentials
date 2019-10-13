package org.kilocraft.essentials.craft.data;

import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.provider.KiloFile;

import java.util.ArrayList;
import java.util.List;

public class KiloData {
    static String workingDir = System.getProperty("user.dir") + "/";
    static String dataDir = workingDir + "/KiloEssentials/data/";

    static List<String> files = new ArrayList<String>(){{
    }};


    public KiloData() {
        files.forEach((file) -> {
            KiloFile kiloFile = new KiloFile(file, getDataDirectoryPath());
            kiloFile.tryToLoad();

            if (kiloFile.exists()) {
                KiloEssentials.getLogger().info("Loaded data file \"" + file + "\"");
            }
        });

    }

    public static String getDataDirectoryPath() {
        return dataDir;
    }

}
