package org.kilocraft.essentials.config;

import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.TPSTracker;

public class ConfigVariableFactory {
    private static Server server = KiloEssentials.getServer();

    public static String replaceUserVariables(String str, @NotNull final User user) {
        Validate.notNull(user, "Use most not be null!");
        return new replacer("user", str)
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .toString();
    }

    public static String replacePlayerVariables(String str, @NotNull final ServerPlayerEntity player) {
        Validate.notNull(player, "Use most not be null!");
        return new replacer("player", str)
                .append("ping", player.pingMilliseconds)
                .append("formatted_ping", TextFormat.getFormattedPing(player.pingMilliseconds))
                .toString();
    }

    public static String replaceServerVariables(String str) {
        return new replacer("server", str)
                .append("tps", TPSTracker.tps1.getShortAverage())
                .append("formatted_tps", TextFormat.getFormattedTPS(TPSTracker.tps1.getAverage()) + TPSTracker.tps1.getShortAverage())
                .append("playerCount", server.getPlayerManager().getCurrentPlayerCount())
                .append("maxPlayers", server.getPlayerManager().getMaxPlayerCount())
                .append("name", KiloConfig.main().server().name)
                .toString();
    }

}

class replacer {
    private String prefix;
    private String text;

    replacer(String prefix, String str) {
        this.prefix = prefix;
        this.text = str;
    }

    String toVar(String key) {
        return "%" + prefix.toUpperCase() + "_" + key.toUpperCase() + "%";
    }

    replacer append(String key, Object value) {
        if (this.text.contains(toVar(key)))
            this.text.replaceAll(toVar(key), String.valueOf(value));

        return this;
    }

    public String toString() {
        String s = this.text;
        this.text = null;
        this.prefix = null;
        return s;
    }
}