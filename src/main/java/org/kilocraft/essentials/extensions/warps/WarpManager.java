package org.kilocraft.essentials.extensions.warps;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.config.KiloConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpManager extends NBTWorldData implements ConfigurableFeature {
    public static WarpManager INSTANCE = new WarpManager();
    private static ArrayList<String> byName = new ArrayList<>();
    private static List<Warp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        WorldDataLib.addIOCallback(this);
        WarpCommand.register(KiloCommands.getDispatcher());
        return true;
    }

    public static void load() {
        WarpCommand.registerAliases();
    }

    public static List<Warp> getWarps() { // TODO Move all access to Feature Types in future.
        return warps;
    }

    public static ArrayList<String> getWarpsByName() {
        return byName;
    }

    public static void addWarp(Warp warp) {
        warps.add(warp);
        byName.add(warp.getName());
    }

    public static void removeWarp(Warp warp) {
        warps.remove(warp);
        byName.remove(warp.getName());
    }

    public static void removeWarp(String warp) {
        byName.remove(getWarp(warp).getName());
        warps.remove(getWarp(warp));
    }

    public static Warp getWarp(String warp) {
        @Nullable Warp var = null;
        for (Warp var2 : warps) {
            if (var2.getName().toLowerCase().equals(warp.toLowerCase()))
                var = var2;
        }

        return var;
    }

    public void save() {
        WorldDataLib.triggerCallbackSave(this);
    }

    public static int teleport(ServerCommandSource source, Warp warp) throws CommandSyntaxException {
        ServerWorld world = source.getMinecraftServer().getWorld(DimensionType.byId(warp.getDimId()));
        source.getPlayer().teleport(world, warp.getX(), warp.getY(), warp.getZ(), warp.getYaw(), warp.getPitch());

        return 1;
    }

    public static String[] getWarpsAsArray() {
        return warps.stream().toArray(String[]::new);
    }

    public static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(warps.stream().map(Warp::getName), builder);
    }

    @Override
    public File getSaveFile(File file, File file1, boolean b) {
        return new File(KiloConfig.getWorkingDirectory() + "/warps." + (b ? "dat_old" : "dat"));
    }

    @Override
    public CompoundTag toNBT(CompoundTag compoundTag) {
        warps.forEach(warp -> compoundTag.put(warp.getName(), warp.toTag()));

        return compoundTag;
    }

    @Override
    public void fromNBT(CompoundTag compoundTag) {
        warps.clear();
        byName.clear();
        compoundTag.getKeys().forEach((key) -> {
            warps.add(new Warp(key, compoundTag.getCompound(key)));
            byName.add(key);
        });
    }
}
