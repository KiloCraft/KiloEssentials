package org.kilocraft.essentials.craft.homesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
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

public class HomeManager extends NBTWorldData implements ConfigurableFeature {
    public static HomeManager INSTANCE = new HomeManager();
    private static List<Home> homes = new ArrayList<>();

    @Override
    public boolean register() {
        WorldDataLib.addIOCallback(this);
        HomeCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    public static void addHome(Home home) {
        homes.add(home);
    }

    public static Home getHome(UUID uuid, String name) {
        for (Home home : getHomes(uuid)) {
            if (home.getName().equals(name))
                return home;
        }

        return null;
    }


    public static List<Home> getHomes(UUID uuid) {
        List<Home> var = new ArrayList<>();
        for (Home var2 : homes) {
            if (var2.getOwner().equals(uuid))
                var.add(var2);
        }

        return var;
    }

    public static boolean hasHome(UUID uuid, String name) {
        boolean bool = false;
        for (Home home : getHomes(uuid)) {
            if (home.getName().equals(name)) bool = true;
        }
        
        return bool;
    }

    public static List<Home> getPlayerHomes(UUID uuid) {
        List<Home> list = new ArrayList<>();
        homes.forEach((home) -> {
            if (home.getOwner().equals(uuid))
                list.add(home);
        });

        return list;
    }

    public static void removeHome(Home home) {
        homes.remove(home);
    }

    public static int teleport(ServerCommandSource source, Home home) throws CommandSyntaxException {
        ServerWorld world = source.getMinecraftServer().getWorld(Registry.DIMENSION.get(home.getDimension() + 1));
        source.getPlayer().teleport(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());

        return 1;
    }

    @Override
    public CompoundTag toNBT(CompoundTag tag) {
        homes.forEach((home) -> {
            CompoundTag playerTag;
            if (tag.contains(home.getOwner().toString())) {
                playerTag = tag.getCompound(home.getOwner().toString());
            } else {
                playerTag = new CompoundTag();
            }

            playerTag.put(home.getName(), home.toTag());
            tag.put(home.getOwner().toString(), playerTag);
        });
        return tag;
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        homes.clear();

        tag.getKeys().forEach(key -> {
            CompoundTag playerTag = tag.getCompound(key);
            playerTag.getKeys().forEach(homeKey -> {
                Home home = new Home(tag.getCompound(key).getCompound(homeKey));
                home.setName(homeKey);
                home.setOwner(UUID.fromString(key));
                homes.add(home);
            });
        });
    }

    public void reload() {
        WorldDataLib.triggerCallbackLoad(this);
    }

    public void save() {
        WorldDataLib.triggerCallbackSave(this);
    }

    public static List<Home> getHomes() {
        return homes;
    }

    @Override
    public File getSaveFile(File worldDir, File rootDir, boolean backup) {
        return new File(KiloConifg.getWorkingDirectory() + "/homes." + (backup ? "dat_old" : "dat"));
    }

    public static SuggestionProvider<ServerCommandSource> suggestHomes = ((context, builder) -> CommandSource.suggestMatching(getHomes(context.getSource().getPlayer().getUuid()).stream().map(Home::getName), builder));

}

