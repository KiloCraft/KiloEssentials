package org.kilocraft.essentials.api.world;

import java.util.ArrayList;
import java.util.List;

public class Structures { // We can do better than this, to allow custom structures to appear we should read the registry and or the Structures map inside of nms Features
    public static List<String> list = new ArrayList<String>(){{
        add("Pillager_Outpost");
        add("Mineshaft");
        add("Igloo");
        add("Desert_Pyramid");
        add("Jungle_Pyramid");
        add("Swamp_Hut");
        add("Stronghold");
        add("Monument");
        add("Fortress");
        add("EndCity");
        add("Ocean_Ruin");
        add("Buried_Treasure");
        add("Shipwreck");
        add("Village");
        add("Mansion");
    }};

    public static boolean isValid(String name) {
        for (String s : list) {
            if (s.toLowerCase().equals(name.toLowerCase()))
                return true;
        }

        return false;
    }
}
