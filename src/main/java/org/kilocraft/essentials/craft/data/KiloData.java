package org.kilocraft.essentials.craft.data;

import com.electronwill.nightconfig.core.file.FileConfig;

public class KiloData {
    private static FileConfig PLAYERS = FileConfig.of(DataFiles.workingDir + "players.json");

    public KiloData() {
        DataFiles.handle();

    }

    public static FileConfig getPlayersData() {
        return PLAYERS;
    }
}
