package org.kilocraft.essentials.config;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.util.TickManager;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.monitor.SystemMonitor;
import org.kilocraft.essentials.util.text.Texter;

public class ConfigVariableFactory {

    public static String replaceOnlineUserVariables(String str, @NotNull final OnlineUser user) {
        Validate.notNull(user, "User most not be null!");
        String string = replaceUserVariables(str, user);
        return new ConfigObjectReplacerUtil("user", string)
                .append("rankedName", user.getRankedDisplayNameAsString())
                .append("ranked_displayName", Texter.Legacy.toFormattedString(user.getRankedDisplayName()))
                .append("ping", user.asPlayer().pingMilliseconds)
                .append("formatted_ping", ComponentText.formatPing(user.asPlayer().pingMilliseconds))
                .toString();
    }

    public static String replaceTargetUserVariables(String str, @NotNull final User user) {
        Validate.notNull(user, "User most not be null!");
        return new ConfigObjectReplacerUtil("target", str)
                .append("rankedName", user.getRankedDisplayNameAsString())
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .toString();
    }

    public static String replaceUserVariables(String str, @NotNull final OnlineUser user) {
        Validate.notNull(user, "User most not be null!");
        return new ConfigObjectReplacerUtil("user", str)
                .append("rankedName", user.getRankedDisplayNameAsString())
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .append("player_count", KiloEssentials.getUserManager().getOnlineUsersAsList(user.hasPermission(CommandPermission.VANISH)).size())
                .toString();
    }

    public static String replaceServerVariables(String str) {
        Validate.notNull(str, "String must not be null!");
        final double memUsagePercent = SystemMonitor.getRamUsedPercentage();
        return new ConfigObjectReplacerUtil("server", str)
                .append("tps", TickManager.tps[0])
                .append("formatted_tps", ComponentText.formatTps(TickManager.tps[0]))
                .append("tps5", TickManager.tps[3])
                .append("formatted_tps5", ComponentText.formatTps(TickManager.tps[3]))
                .append("tps15", TickManager.tps[4])
                .append("formatted_tps15", ComponentText.formatTps(TickManager.tps[4]))
                .append("max_players", KiloEssentials.getMinecraftServer().getPlayerManager().getMaxPlayerCount())
                .append("name", KiloConfig.main().server().name)
                .append("memory_max", String.valueOf(SystemMonitor.getRamMaxMB()))
                .append("memory_usage_percentage", memUsagePercent)
                .append("formatted_memory_usage_percentage", ComponentText.formatPercentage(memUsagePercent))
                .toString();
    }

}

