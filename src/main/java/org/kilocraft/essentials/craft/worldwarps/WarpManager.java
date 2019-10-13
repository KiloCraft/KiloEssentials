package org.kilocraft.essentials.craft.worldwarps;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.commands.essentials.WarpCommand;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.util.ArrayList;
import java.util.List;

public class WarpManager implements ConfigurableFeature {
    private static FileConfig data = KiloConifg.getWarps();

    private static ArrayList<String> byName = new ArrayList<>();
    private static List<Warp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        fromConfig();
        WarpCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    private static void fromConfig() {
        ArrayList<String> warpNames = data.get("warp_names");

        warpNames.forEach((name) -> {
            Config config = data.get("warps." + name);
            Warp warp = new Warp(
                    name,
                    new BlockPos(
                            config.getIntOrElse("pos.x", 0),
                            config.getIntOrElse("pos.y", 0),
                            config.getIntOrElse("pos.z", 0)
                    ),
                    config.getOrElse("dir.pitch", 0),
                    config.getOrElse("dir.yaw", 90),
                    config.getOrElse("permission_required", false)
            );

            warps.add(warp);
        });
    }

    private static void toConfig(Warp warp) {
        Config config = data.get("warps." + warp.getName());

        config.set("pos.x", warp.getBlockPos().getX());
        config.set("pos.y", warp.getBlockPos().getY());
        config.set("pos.z", warp.getBlockPos().getZ());
        config.set("permission_required", warp.doesRequirePermission());
        config.set("dir.pitch", warp.getPitch());
        config.set("dir.yaw", warp.getYaw());

        data.add("warp_names", warp.getName());
        data.save();
    }

    public static List<Warp> getWarps() {
        return warps;
    }

    public static ArrayList<String> getWarpsByName() {
        return byName;
    }

    public static void addWarp(Warp warp) {
        warps.add(warp);
        toConfig(warp);
    }

    public static void reload() {
        fromConfig();
    }

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
