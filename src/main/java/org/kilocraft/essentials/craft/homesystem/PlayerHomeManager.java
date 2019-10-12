package org.kilocraft.essentials.craft.homesystem;

import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author CODY_AI
 */

public class PlayerHomeManager extends NBTWorldData implements ConfigurableFeature {
    public static PlayerHomeManager INSTANCE = new PlayerHomeManager();
    private List<Home> homes = new ArrayList<>();

    @Override
    public boolean register() {
        WorldDataLib.addIOCallback(this);
        HomeCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    public void addHome(Home home) {
        homes.add(home);
    }

    public List<Home> getPlayerHomes(UUID uuid) {
        return getPlayerHomes(uuid.toString());
    }

    public List<Home> getPlayerHomes(String uuid) {
        List<Home> list = new ArrayList<>();
        homes.forEach((home) -> {
            if (home.owner_uuid.equals(uuid))
                list.add(home);
        });

        return list;
    }

    @Override
    public CompoundTag toNBT(CompoundTag tag) {
        homes.forEach(home -> {
            if (tag.contains(home.owner_uuid)) {
                ListTag listTag =  (ListTag) tag.get(home.owner_uuid);
                listTag.add(home.toTag());
            }
        });
        return tag;
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        homes.clear();
        tag.getKeys().forEach(key -> {
            ListTag playerTag = (ListTag) tag.get(key);
            playerTag.forEach(homeTag -> {
                Home home = new Home((CompoundTag) homeTag);
                home.owner_uuid = key;
                homes.add(home);
            });
        });
    }

    public List<Home> getHomes() {
        return homes;
    }

    @Override
    public File getSaveFile(File worldDir, File rootDir, boolean backup) {
        return new File(KiloConifg.getWorkingDirectory() + "/homes." + (backup ? "dat_old" : "dat"));
    }
}

