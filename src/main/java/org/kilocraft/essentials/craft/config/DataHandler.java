package org.kilocraft.essentials.craft.config;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonPrimitive;
import io.github.indicode.fabric.tinyconfig.DefaultedJsonArray;
import io.github.indicode.fabric.tinyconfig.ModConfig;
import net.fabricmc.loader.FabricLoader;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.api.Mod;

import java.io.File;
import java.util.*;

public class DataHandler {
    public enum DataType {
        FLYING, VANISH, GODMODE, STAFFCHAT
    }
    public static final ModConfig CONFIG = new ModConfig(null) {
        @Override
        public File getConfigFile() {
            return new File(FabricLoader.INSTANCE.getGameDirectory() + "/KiloEssentials/cache/players.json5");
        }
    };
    private static Map<DataType, List<UUID>> DATA_MAP = new HashMap<>();
    public static void handle(boolean overwrite) {
        CONFIG.configure(overwrite, config -> Arrays.stream(DataType.values()).forEach(type -> {
                JsonArray array = config.getArray(type.name().toLowerCase(), () -> {
                    DefaultedJsonArray defaultArray = new DefaultedJsonArray();
                    if (DATA_MAP.containsKey(type)) {
                        DATA_MAP.get(type).forEach(uuid -> defaultArray.add(new JsonPrimitive(uuid.toString())));
                    }
                    return defaultArray;
                });
                if (!DATA_MAP.containsKey(type)) DATA_MAP.put(type, new ArrayList<>());
                List<UUID> playerList = DATA_MAP.get(type);
                array.forEach(element -> {
                    playerList.add(UUID.fromString(((JsonPrimitive)element).asString()));
                });
            })
        );

        KiloEssentials.getLogger.info(String.format(Mod.getLang().getProperty("datahandler.load.successfull"), DataType.values().length));
    }
    public static void setActive(UUID player, DataType type,  boolean active) {
        if (!DATA_MAP.containsKey(type)) DATA_MAP.put(type, new ArrayList<>());
        List<UUID> playerList = DATA_MAP.get(type);
        playerList.remove(player);
        if (active) playerList.add(player);
    }
    public static boolean isActive(UUID player, DataType type) {
        if (!DATA_MAP.containsKey(type)) DATA_MAP.put(type, new ArrayList<>());
        List<UUID> playerList = DATA_MAP.get(type);
        return playerList.contains(player);
    }
}
