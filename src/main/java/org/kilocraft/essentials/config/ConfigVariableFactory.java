package org.kilocraft.essentials.config;

import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.TPSTracker;
import org.kilocraft.essentials.util.text.Texter;

public class ConfigVariableFactory {
    private static Server server = KiloEssentials.getServer();

    public static String replaceOnlineUserVariables(String str, @NotNull final OnlineUser user) {
        Validate.notNull(user, "User most not be null!");
        String string = replaceUserVariables(str, user);
        return new ConfigObjectReplacerUtil("user", string)
                .append("ranked_displayName", Texter.Legacy.toFormattedString(user.getRankedDisplayName()))
                .toString();
    }

    public static String replaceTargetUserVariables(String str, @NotNull final User user) {
        Validate.notNull(user, "User most not be null!");
        return new ConfigObjectReplacerUtil("target", str)
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .toString();
    }

    public static String replaceUserVariables(String str, @NotNull final User user) {
        Validate.notNull(user, "User most not be null!");
        return new ConfigObjectReplacerUtil("user", str)
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .toString();
    }

    public static String replacePlayerVariables(String str, @NotNull final ServerPlayerEntity player) {
        Validate.notNull(player, "Player most not be null!");
        return new ConfigObjectReplacerUtil("player", str)
                .append("ping", player.pingMilliseconds)
                .append("formatted_ping", TextFormat.getFormattedPing(player.pingMilliseconds))
                .toString();
    }

    public static String replaceServerVariables(String str) {
        Validate.notNull(str, "String must not be null!");
        return new ConfigObjectReplacerUtil("server", str)
                .append("tps", TPSTracker.tps.getShortAverage())
                .append("formatted_tps", TextFormat.getFormattedTPS(TPSTracker.tps.getAverage()) + TPSTracker.tps.getShortAverage())
                .append("playerCount", server.getPlayerManager().getCurrentPlayerCount())
                .append("maxPlayers", server.getPlayerManager().getMaxPlayerCount())
                .append("name", KiloConfig.main().server().name)
                .toString();
    }

}

