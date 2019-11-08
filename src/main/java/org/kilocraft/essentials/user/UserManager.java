package org.kilocraft.essentials.user;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.chat.KiloChat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author CODY_AI
 * An easy way to handle the User (Instance of player)
 *
 * @see User
 * @see UserHomeHandler
 */


public class UserManager {
    private static List<User> loadedUsers = new ArrayList<>();
    private List<User> offlineUsers = new ArrayList<>();
    private UserHandler handler = new UserHandler();
    
    public UserManager() {
    }

    public static List<User> getUsers() {
        return loadedUsers;
    }

    public User getUser(UUID uuid) {
        User user = null;
        for (User loadedUser : loadedUsers) {
            if (loadedUser.uuid.equals(uuid)) user = loadedUser;
        }

        return user;
    }

    User getUser(String name) {
        User user = null;
        for (User loadedUser : loadedUsers) {
            if (loadedUser.getName().equals(name))
                user = loadedUser;
        }

        return user;
    }

    private User getOfflineUser(UUID uuid) {
        User user = null;
        this.handler.

        return user;
    }
    
    Text getUserDisplayName(User user) {
    	if (user.getNickname().equals("")) {
    		return Objects.requireNonNull(KiloServer.getServer().getPlayerManager().getPlayer(user.getUuid())).getDisplayName();
    	} else {
    		return new LiteralText(user.getNickname());
    	}
    }

    User getUserByNickname(String nickName) {
        User requested = null;
        for (User user : loadedUsers) {
            if (user.getNickname().equals(nickName))
                requested = user;
        }

        return requested;
    }

    public void triggerSave() throws IOException {
        for (User user : loadedUsers) {
            this.handler.saveData(user);
        }
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        User user = User.of(player);
        user.name = player.getGameProfile().getName();
        loadedUsers.add(user);

        try {
            this.handler.handleUser(user);
        } catch (IOException ignored) { }

        KiloChat.broadcastUserJoinEventMessage(user);
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        User user = User.of(player);
        user.name = player.getGameProfile().getName();
        loadedUsers.remove(user);

        try {
            this.handler.saveData(user);
        } catch (IOException ignored) { }

        KiloChat.broadcastUserLeaveEventMessage(user);
    }

}
