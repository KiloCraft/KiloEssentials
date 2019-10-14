package org.kilocraft.essentials.craft.worldwarps;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.commands.essentials.WarpCommand;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WarpManager extends NBTWorldData implements ConfigurableFeature {
    private static ArrayList<String> byName = new ArrayList<>();
    private static List<Warp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        WorldDataLib.addIOCallback(this);
        WarpCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    public static List<Warp> getWarps() {
        return warps;
    }

    public static ArrayList<String> getWarpsByName() {
        return byName;
    }

    public static void addWarp(Warp warp) {
        warps.add(warp);
    }

    public static void removeWarp(Warp warp) {
        warps.remove(warp);
    }

    public static void removeWarp(String warp) {
        warps.forEach((var) -> {
            if (var.getName().equals(warp)) warps.remove(var);
        });
    }

    public static Warp getWarp(String warp) {
        AtomicReference<Warp> var = null;
        warps.stream().filter((var2) -> {
            if (var2.getName().equals(warp)) {
                var.set(var2);
            }
            return var.get().getName().equals(var2.getName());
        });

        return var.get();
    }

    public static void reload() {
    }

    public static String[] getWarpsAsArray() {
        return warps.stream().toArray(String[]::new);
    }

    public static SuggestionProvider<ServerCommandSource> suggestWarps = ((context, builder) -> CommandSource.suggestMatching(warps.stream().map(Warp::getName), builder));

    @Override
    public File getSaveFile(File file, File file1, boolean b) {
        return new File(KiloConifg.getWorkingDirectory() + "/warps." + (b ? "dat_old" : "dat"));
    }

    @Override
    public CompoundTag toNBT(CompoundTag compoundTag) {
        warps.forEach(warp -> compoundTag.put(warp.getName(), warp.toTag()));

        return compoundTag;
    }

    @Override
    public void fromNBT(CompoundTag compoundTag) {
        warps.clear();
        compoundTag.getKeys().forEach((key) -> warps.add(new Warp(key, compoundTag.getCompound(key))));
    }
}
