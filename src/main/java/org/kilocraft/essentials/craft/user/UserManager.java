package org.kilocraft.essentials.craft.user;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class UserManager {
    private static List<User> loadedUsers = new ArrayList<>();
    private File saveDir = new File(KiloConifg.getWorkingDirectory() + "/users/");
    private static UserHandler handler = new UserHandler();
    
    public UserManager() {
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
    }

    public List<User> getUsers() {
        return loadedUsers;
    }

    public static User getUser(UUID uuid) {
        User requested = new User(uuid);
        for (User user : loadedUsers) {
            if (user.getUuid().equals(uuid))
                requested = user;
        }

        return requested;
    }

    public static User getUser(String name) {
        return getUser(Objects.requireNonNull(KiloServer.getServer().getPlayerManager().getPlayer(name)).getUuid());
    }
    
    public static String getUserDisplayName (String name) {
    	User user = getUser(name);
    	
    	if (user.getNickName() == "") {
    		return KiloServer.getServer().getPlayerManager().getPlayer(name).getName().asString();
    	} else {
    		return user.getNickName();
    	}
    }

    public User getUserByNickname(String nickName) {
        User requested = null;
        for (User user : loadedUsers) {
            if (user.getNickName().equals(nickName))
                requested = user;
        }

        return requested;
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        User thisUser = new User(player.getUuid());
        handler.handleUser(thisUser);
        loadedUsers.add(thisUser);
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        User thisUser = new User(player.getUuid());
        loadedUsers.remove(thisUser);
        try {
            handler.saveData(thisUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

