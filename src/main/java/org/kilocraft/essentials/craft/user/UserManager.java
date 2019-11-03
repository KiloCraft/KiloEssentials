package org.kilocraft.essentials.craft.user;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.chat.KiloChat;

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
        User user = User.of(uuid);
        for (User loadedUser : loadedUsers) {
            if (user.getUuid().equals(uuid))
                user = loadedUser;
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
    
    Text getUserDisplayName(User user) {
    	if (user.getNickname().equals("")) {
    		return Objects.requireNonNull(KiloServer.getServer().getPlayerManager().getPlayer(user.getUuid())).getName();
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
        for (User loadedUser : loadedUsers) {
            loadedUser.name = loadedUser.getPlayer().getGameProfile().getName();
            loadedUser.updatePos();
            this.handler.saveData(loadedUser, false);
        }
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        User thisUser = User.of(player);
        thisUser.name = player.getGameProfile().getName();
        handler.handleUser(thisUser);
        loadedUsers.add(thisUser);

        KiloChat.broadcastUserJoinEventMessage(thisUser);
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        User thisUser = User.of(player);
        thisUser.name = player.getGameProfile().getName();
        loadedUsers.remove(thisUser);
        try {
            handler.saveData(thisUser, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        KiloChat.broadcastUserLeaveEventMessage(thisUser);
    }
}

