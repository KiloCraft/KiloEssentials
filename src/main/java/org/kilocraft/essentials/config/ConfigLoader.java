package org.kilocraft.essentials.config;

import java.io.*;

public class ConfigLoader {
    public static void load(File cfg) throws FileNotFoundException {
        try {
            Config.general.load(new FileInputStream(cfg.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
