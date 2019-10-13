package org.kilocraft.essentials.craft.data;

import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.provider.KiloFile;

import java.util.ArrayList;
import java.util.List;

public class KiloData {
    static String workingDir = System.getProperty("user.dir") + "/";
    static List<String> files = new ArrayList<String>(){{
        add("players.dat");
    }};


    public KiloData() {
        files.forEach((file) -> {
            KiloFile kiloFile = new KiloFile(file, workingDir);
            kiloFile.tryToLoad();

            if (kiloFile.exists()) {
                KiloEssentials.getLogger().info("Loaded data file \"" + file + "\"");
            }
        });

    }

    public static String getDataDirectoryPath() {
        return workingDir + "/KiloEssentials/data/";
    }


}
