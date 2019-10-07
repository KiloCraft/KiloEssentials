package org.kilocraft.essentials.craft.homesystem;

import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.config.NbtFile;
import org.kilocraft.essentials.craft.config.KiloConifg;
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

public class PlayerHomeManager extends NBTWorldData implements ConfigurableFeature {
    NbtFile nbt = new NbtFile("/KiloEssentials/data/", "homes");
    public static PlayerHomeManager INSTANCE = null;
    private HashMap<String, Home> hashMap = new HashMap<>();

    @Override
    public boolean register() {
        WorldDataLib.addIOCallback(this);
        HomeCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
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

    @Override
    public CompoundTag toNBT(CompoundTag tag) {
        hashMap.values().forEach(home -> {
            if (tag.containsKey(home.owner_uuid)) {
                ListTag listTag =  (ListTag) tag.getTag(home.owner_uuid);
                listTag.add(home.toTag());
            }
        });
        return tag;
    }

    @Override
    public void fromNBT(CompoundTag tag) {
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

    @Override
    public File getSaveFile(File worldDir, File rootDir, boolean backup) {
        return new File(KiloConifg.getWorkingDirectory() + "/data/homes." + (backup ? "dat_old" : "dat"));
    }
}

