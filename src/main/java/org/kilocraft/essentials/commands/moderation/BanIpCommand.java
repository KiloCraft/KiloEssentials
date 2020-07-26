package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanIpCommand extends EssentialCommand {
    public static final Pattern PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public BanIpCommand() {
        super("ke_ban-ip", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> victim = this.getUserArgument("user")
                .executes((ctx) -> this.execute(ctx, null, false));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent")
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), true));

        victim.then(reason);
        silent.then(victim);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String input = this.getUserArgumentInput(ctx, "user");
        Matcher matcher = PATTERN.matcher(input);
        if (matcher.matches()) {
            return banIp(src, null, input, reason, silent);
        } else {
            this.getEssentials().getUserThenAcceptAsync(src, input, (victim) -> {
                if (victim.getLastSocketAddress() == null) {
                    src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                    return;
                }

                banIp(src, victim, victim.getLastSocketAddress(), reason, silent);
            });

        }

        return AWAIT;
    }

    private int banIp(final OnlineUser src, @Nullable EntityIdentifiable victim, @NotNull String ip, @Nullable final String reason, boolean silent) {
        BannedIpList bannedIpList = super.getServer().getMinecraftServer().getPlayerManager().getIpBanList();
        List<ServerPlayerEntity> players = super.getServer().getPlayerManager().getPlayersByIp(ip);
        Date date = new Date();

        BannedIpEntry entry = new BannedIpEntry(ip, date, src.getName(), null, reason);
        bannedIpList.add(entry);

        MutableText text = new TextMessage(
                ServerUserManager.replaceVariables(super.config.moderation().messages().permIpBan, entry, true)
        ).toText();

        for (ServerPlayerEntity player : players) {
            player.networkHandler.disconnect(text);
        }

        this.getServer().getUserManager().onPunishmentPerformed(src, victim == null ? new Punishment(src, ip, reason) : new Punishment(src, victim, ip, reason, null), Punishment.Type.BAN_IP, null, silent);
        return players.size();
    }


}
