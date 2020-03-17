package org.kilocraft.essentials.extensions.warps.playerwarps;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.NBTStorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerWarpsManager implements ConfigurableFeature, NBTStorage {
    private static boolean enabled = false;
    private static ArrayList<String> byName = new ArrayList<>();
    private static List<PlayerWarp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        enabled = true;
        NBTStorageUtil.addCallback(this);
        //TODO: Add the Commands
        return true;
    }

    public static void load() {
        //TODO: Add the Commands
    }

    public static List<PlayerWarp> getWarps() { // TODO Move all access to Feature Types in future.
        return warps;
    }

    public static ArrayList<String> getWarpsByName() {
        return byName;
    }

    public static void addWarp(PlayerWarp warp) {
        warps.add(warp);
        byName.add(warp.getName());
    }

    public static void removeWarp(PlayerWarp warp) {
        warps.remove(warp);
        byName.remove(warp.getName());
    }

    public static void removeWarp(String name) {
        PlayerWarp warp = getWarp(name);

        if (warp != null) {
            removeWarp(warp);
        }
    }

    @Nullable
    public static PlayerWarp getWarp(String warp) {
        for (PlayerWarp w : warps) {
            if (w.getName().toLowerCase(Locale.ROOT).equals(warp.toLowerCase(Locale.ROOT))) {
                return w;
            }
        }

        return null;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("player_warps.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        return null;
    }

    @Override
    public void deserialize(@NotNull CompoundTag compoundTag) {

    }
}
