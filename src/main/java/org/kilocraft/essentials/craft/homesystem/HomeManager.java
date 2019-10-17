package org.kilocraft.essentials.craft.homesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @Author CODY_AI
 */

public class HomeManager extends NBTWorldData implements ConfigurableFeature {
    public static HomeManager INSTANCE = new HomeManager();
    private static List<Home> homes = new ArrayList<>();
    private static HashMap<String, String> byName = new HashMap<>();

    @Override
    public boolean register() {
        WorldDataLib.addIOCallback(this);
        HomeCommand.register(KiloServer.getServer().getCommandRegistry().getDispatcher());
        return true;
    }

    public static void addHome(Home home) {
        homes.add(home);
    }

    public static Home getHome(String uuid, String name) {
        @Nullable Home var = null;
        for (Home var2 : homes) {
            if (var2.getOwner().equals(uuid)) {
                if (var2.getName().equals(name))
                    var = var2;
            }
        }

        return var;
    }

    public static HashMap<String, String> getHomesByName() {
        return byName;
    }

    public static List<Home> getHomes(String uuid) {
        List<Home> var = new ArrayList<>();
        for (Home var2 : homes) {
            if (var2.getOwner().equals(uuid))
                var.add(var2);
        }

        return var;
    }

    public static List<Home> getPlayerHomes(UUID uuid) {
        return getPlayerHomes(uuid.toString());
    }

    public static List<Home> getPlayerHomes(String uuid) {
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

    public static void teleportToHome(ServerCommandSource source, Home home) throws CommandSyntaxException {
        ServerWorld world = source.getMinecraftServer().getWorld(Registry.DIMENSION.get(home.getDimension() + 1));
        source.getPlayer().teleport(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
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
        byName.clear();
        homes.clear();
        tag.getKeys().forEach(key -> {
            CompoundTag playerTag = tag.getCompound(key);
            playerTag.getKeys().forEach(homeKey -> {
                Home home = new Home(tag.getCompound(key).getCompound(homeKey));
                home.setName(homeKey);
                home.setOwner(UUID.fromString(key));
                homes.add(home);
                byName.put(homeKey, home.getName());
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

    public static SuggestionProvider<ServerCommandSource> suggestHomesOLD = ((context, builder) -> {
        return CommandSource.suggestMatching(homes.stream().filter((var) -> {
            try {
                return var.getOwner().equals(context.getSource().getPlayer().getUuidAsString());
            } catch (CommandSyntaxException e) {
                context.getSource().sendError(new LiteralText("An exception happened when getting the auto-completion suggestions"));
                return false;
            }
        }).map(Home::getName), builder);
    });

    public static SuggestionProvider<ServerCommandSource> suggestHomes = ((context, builder) -> {
        getHomes(context.getSource().getPlayer().getUuidAsString()).forEach((home) -> {
            builder.suggest(home.getName());
        });

        return builder.buildFuture();
    });


}

