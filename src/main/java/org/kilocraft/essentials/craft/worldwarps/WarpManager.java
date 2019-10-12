package org.kilocraft.essentials.craft.worldwarps;

import io.github.indicode.fabric.worlddata.NBTWorldData;
import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.commands.essentials.WarpCommand;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WarpManager extends NBTWorldData implements ConfigurableFeature {
    public static WarpManager INSTANCE;

    private static ArrayList<String> byName = new ArrayList<>();
    private static List<Warp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        fromConfig();
        WarpCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    private void fromConfig() {

//        ArrayList<String> warpNames = data.get("warp_names");
//
//        warpNames.forEach((name) -> {
//            Config config = data.get("warps." + name);
//            Warp warp = new Warp(
//                    name,
//                    new BlockPos(
//                            config.getIntOrElse("pos.x", 0),
//                            config.getIntOrElse("pos.y", 0),
//                            config.getIntOrElse("pos.z", 0)
//                    ),
//                    config.getOrElse("permission_required", false)
//            );
//
//            byWarp.add(warp);
//        });
    }

    private void toConfig(Warp warp) {
//        Config config = data.get("warps." + warp.getName());
//
//        config.set("pos.x", warp.getBlockPos().getX());
//        config.set("pos.y", warp.getBlockPos().getY());
//        config.set("pos.z", warp.getBlockPos().getZ());
//        config.set("permission_required", warp.doesRequirePermission());
//
//        data.add("warp_names", warp.getName());
//        data.save();
    }

    public List<Warp> getWarps() {
        return warps;
    }

    public ArrayList<String> getWarpsByName() {
        return byName;
    }

    public void addWarp(Warp warp) {
        toConfig(warp);
        warps.add(warp);
    }

    public void reload() {
        fromConfig();
    }

    @Override
    public File getSaveFile(File file, File file1, boolean b) {
        return new File(KiloConifg.getWorkingDirectory() + "/warps." + (b ? "dat_old" : "dat"));
    }

    @Override
    public CompoundTag toNBT(CompoundTag compoundTag) {
        warps.forEach((warp) -> {
            
        });

        return null;
    }

    @Override
    public void fromNBT(CompoundTag compoundTag) {
        warps.clear();
        compoundTag.getKeys().forEach((key) -> {

        });
    }
}
