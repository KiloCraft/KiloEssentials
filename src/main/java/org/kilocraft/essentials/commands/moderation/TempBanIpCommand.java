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
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

public class TempBanIpCommand extends EssentialCommand {
    public TempBanIpCommand() {
        super("tempban-ip", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> victim = this.getUserArgument("target");

        RequiredArgumentBuilder<ServerCommandSource, String> time = argument("time", StringArgumentType.word())
                .suggests(TimeDifferenceUtil::listSuggestions)
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, false));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent")
                .then(this.getUserArgument("target").then(
                        argument("time", StringArgumentType.word())
                                .suggests(TimeDifferenceUtil::listSuggestions)
                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, true))
                                .then(
                                        argument("reason", StringArgumentType.greedyString())
                                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), true))
                                )
                ));

        time.then(reason);
        victim.then(time);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @NotNull final String time, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String input = this.getUserArgumentInput(ctx, "target");
        Matcher matcher = BanIpCommand.PATTERN.matcher(input);
        if (matcher.matches()) {
            return tempBanIp(src, null, input, time, reason, silent);
        } else {
            this.getEssentials().getUserThenAcceptAsync(src, input, (victim) -> {
                if (victim.getLastIp() == null) {
                    src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                    return;
                }

                try {
                    tempBanIp(src, victim, victim.getLastIp(), time, reason, silent);
                } catch (CommandSyntaxException e) {
                    src.sendError(e.getMessage());
                }
            });

        }

        return AWAIT;
    }

    private int tempBanIp(final OnlineUser src, @Nullable final EntityIdentifiable victim, @NotNull String ip, @NotNull final String time, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        Date date = new Date();
        Date expiry = new Date(TimeDifferenceUtil.parse(time, true));
        BannedIpList bannedIpList = super.getServer().getMinecraftServer().getPlayerManager().getIpBanList();
        List<ServerPlayerEntity> players = super.getServer().getPlayerManager().getPlayersByIp(ip);

        BannedIpEntry entry = new BannedIpEntry(ip, date, src.getName(), expiry, reason);
        bannedIpList.add(entry);

        MutableText text = new TextMessage(
                ServerUserManager.replaceVariables(super.config.moderation().messages().tempIpBan, entry, false)
        ).toText();

        for (ServerPlayerEntity player : players) {
            player.networkHandler.disconnect(text);
        }

        this.getServer().getUserManager().onPunishmentPerformed(src, victim == null ? new Punishment(src, null, ip, reason, expiry) : new Punishment(src, victim, ip, reason, expiry), Punishment.Type.BAN_IP, time, silent);
        return players.size();
    }


}
