package org.kilocraft.essentials.user;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.extensions.homes.Home;
import org.kilocraft.essentials.extensions.homes.commands.HomeCommand;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author CODY_AI
 * A better way of handeling the User (Instance of player) Homes
 *
 * @see ServerUser
 * @see UserManager
 */


public class UserHomeHandler implements ConfigurableFeature {
    private static boolean isEnabled = false;
    private static List<Home> loadedHomes = new ArrayList<>();
    private List<Home> userHomes;
    private ServerUser serverUser;

    @Override
    public boolean register() {
        isEnabled = true;
        HomeCommand.register(KiloCommands.getDispatcher());
        return true;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public UserHomeHandler() {
    }

    public UserHomeHandler(ServerUser serverUser) {
        if (isEnabled()) {
            this.serverUser = serverUser;
            this.userHomes = new ArrayList<>();
        }

    }

    public void addHome(Home home) {
        this.userHomes.add(home);
        loadedHomes.add(home);
    }

    public void removeHome(String name) {
        removeHome(getHome(name));
    }

    public void removeHome(Home home) {
        this.userHomes.remove(home);
        loadedHomes.remove(home);
    }

    public Home getHome(String name) {
        for (Home home : this.userHomes) {
            if (home.getName().equals(name))
                return home;
        }

        return new Home();
    }

    public List<Home> getHomes() {
        return this.userHomes;
    }

    public boolean hasHome(String name) {
        boolean bool = false;
        for (Home userHome : this.userHomes) {
            if (userHome.getName().equals(name))
                bool = true;
        }

        return bool;
    }

    public static boolean hasHome(UUID uuid, String name) {
        boolean bool = false;
        for (Home loadedHome : loadedHomes) {
            if (loadedHome.getName().equals(name) && loadedHome.getOwner().equals(uuid))
                bool = true;
        }

        return bool;
    }

    public void teleportToHome(String name) {
        teleportToHome(getHome(name));
    }

    public void teleportToHome(Home home) {
        if (this.serverUser.isOnline()) {
            ServerWorld world = this.serverUser.getCommandSource().getMinecraftServer().getWorld(Registry.DIMENSION.get(home.getDimension() + 1));
            this.serverUser.getPlayer().teleport(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
        }

    }

    public CompoundTag serialize1() {
        CompoundTag compoundTag = new CompoundTag();
        for (Home userHome : this.userHomes) {
            compoundTag.put(userHome.getName(), userHome.toTag());
        }

        return compoundTag;
    }

    public CompoundTag serialize(CompoundTag tag) {
        for (Home userHome : this.userHomes) {
            tag.put(userHome.getName(), userHome.toTag());
            System.out.println(userHome.getName());
        }

        return tag;
    }

    public void deserialize(CompoundTag compoundTag) {
        for (String key : compoundTag.getKeys()) {
            Home home = new Home(compoundTag);
            home.setName(key);
            home.setOwner(this.serverUser.uuid);
            this.userHomes.add(home);
            loadedHomes.add(home);
        }

    }

    public static List<Home> getHomesOf(UUID uuid) {
        List<Home> list = new ArrayList<>();
        for (Home home : loadedHomes) {
            if  (home.getOwner().equals(uuid)) list.add(home);
        }

        return list;
    }

    public static List<Home> getLoadedHomes() {
        return loadedHomes;
    }

    public static SuggestionProvider<ServerCommandSource> suggestUserHomes = ((context, builder) ->
            CommandSource.suggestMatching(getHomesOf(context.getSource().getPlayer().getUuid()).stream().map(Home::getName), builder)
    );

}
