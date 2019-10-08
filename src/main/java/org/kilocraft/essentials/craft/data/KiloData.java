package org.kilocraft.essentials.craft.data;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.provider.KiloFile;

import java.util.ArrayList;
import java.util.List;

public class KiloData {
    static String workingDir = System.getProperty("user.dir") + "/";
    static List<String> files = new ArrayList<String>(){{
        add("players.dat");
    }};
    private static FileConfig PLAYERS;

    public KiloData() {
        files.forEach((file) -> {
            KiloFile kiloFile = new KiloFile(file, workingDir);
            kiloFile.tryToLoad();

            if (kiloFile.exists()) {
                KiloEssentials.getLogger().info("Loaded data file \"" + file + "\"");
                load();
            }
        });

    }

    private void load() {

    }




    public static FileConfig getPlayersData() {
        return PLAYERS;
    }
}
