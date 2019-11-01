package org.kilocraft.essentials.craft.user;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserManager {
    private List<User> loadedUsers = new ArrayList<>();
    private UserHandler handler = new UserHandler();
    
    public UserManager() {
    }

    public List<User> getUsers() {
        return loadedUsers;
    }

    public User getUser(UUID uuid) {
        User requested = new User(uuid);
        for (User user : loadedUsers) {
            if (user.getUuid().equals(uuid))
                requested = user;
        }

        return requested;
    }

    public User getUser(String name) {
        return getUser(Objects.requireNonNull(KiloServer.getServer().getPlayerManager().getPlayer(name)).getUuid());
    }
    
    public String getUserDisplayName (String name) {
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

    public void onPlayerJoin(ServerPlayerEntity player) {
        User thisUser = new User(player.getUuid());
        handler.handleUser(thisUser);
        loadedUsers.add(thisUser);
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        User thisUser = new User(player.getUuid());
        loadedUsers.remove(thisUser);
        try {
            handler.saveData(thisUser);;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

