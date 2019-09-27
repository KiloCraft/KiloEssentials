package org.kilocraft.essentials.craft.homesystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.kilocraft.essentials.api.config.NbtFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @Author CODY_AI
 */

public class PlayerHomeManager {
    NbtFile nbt = new NbtFile("/KiloEssentials/data/", "homes");
    protected static PlayerHomeManager INSTANCE = null;
    private HashMap<String, Home> hashMap = new HashMap<>();

    public void addHome(Home home) {
        hashMap.put(home.name, home);
    }

    public List<Home> getPlayerHomes(UUID uuid) {
        List<Home> homes = new ArrayList<>();
        hashMap.values().forEach((home) -> {
            if (home.owner_uuid.equals(uuid)) homes.add(home);
        });

        return homes;
    }


    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        hashMap.values().forEach(home -> {
            ListTag listTag = tag.containsKey(home.owner_uuid) ? (ListTag) tag.getTag(home.owner_uuid) : new ListTag();
            listTag.add(home.toTag());
        });
        return tag;
    }

    public void fronNbt(CompoundTag tag) {
        hashMap.clear();
        tag.getKeys().forEach(key -> {
            ListTag playerTag = (ListTag) tag.getTag(key);
            playerTag.forEach(homeTag -> {
                Home home = new Home((CompoundTag) homeTag);
                home.owner_uuid = key;
                hashMap.put(home.name, home);
            });
        });
    }




    public static PlayerHomeManager getInstance() {
        return INSTANCE;
    }

    public HashMap<String, Home> getHashMap() {
        return hashMap;
    }
}

