package org.kilocraft.essentials.extensions.warps;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.NBTStorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpManager implements ConfigurableFeature, NBTStorage {
    public static WarpManager INSTANCE = new WarpManager();
    private static ArrayList<String> byName = new ArrayList<>();
    private static List<Warp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        NBTStorageUtil.addCallback(this);
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

    public static int teleport(ServerCommandSource source, Warp warp) throws CommandSyntaxException {
        ServerWorld world = source.getMinecraftServer().getWorld(DimensionType.byId(warp.getLocation().getDimension()));
        source.getPlayer().teleport(world, warp.getLocation().getX(), warp.getLocation().getY(), warp.getLocation().getZ(),
                warp.getLocation().getRotation().getYaw(), warp.getLocation().getRotation().getPitch());

        return 1;
    }

    public static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(warps.stream().map(Warp::getName), builder);
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("warps.dat", KiloEssentials.getDataDirectory());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        for (Warp warp : warps) {
            tag.put(warp.getName(), warp.toTag());
        }

        return tag;
    }

    @Override
    public void deserialize(CompoundTag compoundTag) {
        warps.clear();
        byName.clear();
        compoundTag.getKeys().forEach((key) -> {
            warps.add(new Warp(key, compoundTag.getCompound(key)));
            byName.add(key);
        });
    }
}
