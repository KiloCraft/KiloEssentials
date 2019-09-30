package org.kilocraft.essentials.craft.homesystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.config.ConfigFile;
import org.kilocraft.essentials.api.config.NbtFile;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @Author CODY_AI
 */

public class PlayerHomeManager implements ConfigurableFeature {
    NbtFile nbt = new NbtFile("/KiloEssentials/data/", "homes");
    public static PlayerHomeManager INSTANCE = null;
    private HashMap<String, Home> hashMap = new HashMap<>();

    @Override
    public boolean register() {
        HomeCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    public void load() {
        File homes = new File(ConfigFile.currentDir + "/data/homes.dat");
        File homes_old = new File(ConfigFile.currentDir + "/data/homes_old.dat");

        PlayerHomeManager.INSTANCE = new PlayerHomeManager();
        if (!homes.exists()) {
            if (homes_old.exists()) {}
            else return;
        }
        try {
            if (!homes.exists() && homes_old.exists()) throw new FileNotFoundException();
            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(homes));
            PlayerHomeManager.INSTANCE.fronNbt(tag);
        } catch (IOException e) {
            System.err.println("Could not load homes.dat:");
            e.printStackTrace();
            if (homes_old.exists()) {
                System.out.println("Attempting to load backup homes...");
                try {
                    CompoundTag tag = NbtIo.readCompressed(new FileInputStream(homes));
                    PlayerHomeManager.INSTANCE.fronNbt(tag);
                } catch (IOException e2) {
                    throw new RuntimeException("Could not load homes.dat_old - Crashing server to save data. Remove or fix homes.dat or homes.dat_old to continue");

                }
            }
        }
    }

    public void addHome(Home home) {
        hashMap.put(home.name, home);
    }

    public List<Home> getPlayerHomes(UUID uuid) {
        List<Home> homes = new ArrayList<>();
        hashMap.values().forEach((home) -> {
            if (home.owner_uuid.equals(uuid.toString())) homes.add(home);
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

    public HashMap<String, Home> getHashMap() {
        return hashMap;
    }

}

