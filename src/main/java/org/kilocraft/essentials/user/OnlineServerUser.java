package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.UUID;

public class OnlineServerUser extends ServerUser implements OnlineUser {

    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
    }

    public ServerCommandSource getCommandSource() {
        return this.getPlayer().getCommandSource();
    }

    @Override
    public void sendMessage(String message) {
        KiloChat.sendMessageTo(this.getPlayer(), new LiteralText(message));
    }

    @Override
    public void sendMessage(Text text) {
        KiloChat.sendMessageTo(this.getPlayer(), text);
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) {
        KiloChat.sendMessageTo(this.getPlayer(), chatMessage);
    }

    @Override
    public void sendLangMessage(String key, Object... objects) {
        KiloChat.sendLangMessageTo(this.getPlayer(), key, objects);
    }

    @Override
    public void sendConfigMessage(String key, Object... objects) {
        String message = KiloConfig.getProvider().getMessages().getMessage(key, objects);
        this.sendMessage(new ChatMessage(message, true));
    }

    public static OnlineServerUser of(UUID uuid) {
        return (OnlineServerUser) manager.getOnline(uuid);
    }

    public static OnlineServerUser of(String name) {
        return (OnlineServerUser) manager.getOnline(name);
    }

    public static OnlineServerUser of(GameProfile profile) {
        return of(profile.getId());
    }

    public static OnlineServerUser of(ServerPlayerEntity player) {
        return of(player.getUuid());
    }

    public OnlineServerUser(ServerPlayerEntity player) {
        super(player.getUuid());
        this.name = player.getEntityName();
    }

    @Override
    protected void deserialize(@NotNull CompoundTag tag) {
        // All the other serialization logic is handled.
        super.deserialize(tag);
    }

    @Override
    public void setFlight(boolean set) {
        super.setFlight(true);
        this.getPlayer().abilities.allowFlying = set;
        this.getPlayer().abilities.flying = set;
        this.getPlayer().sendAbilitiesUpdate();
    }

    public void onJoined() {
        if (this.canFly() && KiloCommands.hasPermission(this.getCommandSource(), CommandPermission.FLY_SELF))
            this.setFlight(true);

    }

}
