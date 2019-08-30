package org.kilocraft.essentials;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Mod {
    public static Properties properties = new Properties();
    public static Properties lang = new Properties();

    public Mod() {
        try {
            properties.load(Mod.class.getClassLoader().getResourceAsStream("Mod.properties"));
            lang.load(Mod.class.getClassLoader().getResourceAsStream("assets" + File.separator + "Lang.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
