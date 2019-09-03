package org.kilocraft.essentials.api.Entity;

import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import com.mojang.authlib.GameProfile;

import java.net.SocketAddress;

public interface Player extends OfflinePlayer {

    GameProfile getProfile();

    SocketAddress getAddress();

    void kickPlayer(String message);

    boolean performCommand(String command);

    boolean isSneaking();

    void setSneaking(boolean sneak);

    boolean isSprinting();

    void setSprinting(boolean sprinting);

    void giveExp(int amount);

    float getExp();

    void setExp(float exp);

    int getLevel();

    void setLevel(int level);

    int getTotalExperience();

    void setTotalExperience(int exp);

    float getSaturation();

    void setSaturation(float value);

    int getFoodLevel();

    void setFoodLevel(int value);

    boolean getAllowFlight();

    void setAllowFlight(boolean flight);

    void hidePlayer(Player player);

    void showPlayer(Player player);

    boolean canSee(Player player);

    boolean isOnGround();

    boolean isFlying();

    void setFlying(boolean value);

    void setGroup(String name);

    SpawnRestriction.Location getLocation();

    PlayerInventory getInventory();

    void sendMessage(String message);

    void sendMessage(LiteralText message);
}
