package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.events.PunishEvents;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class TempBanIpCommand extends EssentialCommand {
    public TempBanIpCommand() {
        super("tempban-ip", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> victim = this.getUserArgument("target");

        RequiredArgumentBuilder<CommandSourceStack, String> time = this.argument("time", StringArgumentType.word())
                .suggests(TimeDifferenceUtil::listSuggestions)
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, false));

        RequiredArgumentBuilder<CommandSourceStack, String> reason = this.argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<CommandSourceStack> silent = this.literal("-silent")
                .then(this.getUserArgument("target").then(
                        this.argument("time", StringArgumentType.word())
                                .suggests(TimeDifferenceUtil::listSuggestions)
                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, true))
                                .then(
                                        this.argument("reason", StringArgumentType.greedyString())
                                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), true))
                                )
                ));

        time.then(reason);
        victim.then(time);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, @NotNull final String time, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);
        String input = this.getUserArgumentInput(ctx, "target");
        Matcher matcher = BanIpCommand.PATTERN.matcher(input);
        if (matcher.matches()) {
            return this.tempBanIp(src, null, input, time, reason, silent);
        } else {
            this.getUserManager().getUserThenAcceptAsync(src, input, (victim) -> {
                if (victim.getLastIp() == null) {
                    src.sendLangError("exception.no_value_set_user", "lastSocketAddress");
                    return;
                }

                try {
                    this.tempBanIp(src, victim, victim.getLastIp(), time, reason, silent);
                } catch (CommandSyntaxException e) {
                    src.sendError(e.getMessage());
                }
            });

        }

        return AWAIT;
    }

    private int tempBanIp(final CommandSourceUser src, @Nullable final EntityIdentifiable victim, @NotNull String ip, @NotNull final String time, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        Date date = new Date();
        Date expiry = new Date(TimeDifferenceUtil.parse(time, true));
        IpBanList bannedIpList = KiloEssentials.getMinecraftServer().getPlayerList().getIpBans();
        List<ServerPlayer> players = KiloEssentials.getMinecraftServer().getPlayerList().getPlayersWithAddress(ip);

        IpBanListEntry entry = new IpBanListEntry(ip, date, src.getName(), expiry, reason);
        bannedIpList.add(entry);

        MutableComponent text = ComponentText.toText(
                ServerUserManager.replaceBanVariables(KiloConfig.main().moderation().messages().tempIpBan, entry, false)
        );

        for (ServerPlayer player : players) {
            player.connection.disconnect(text);
        }

        PunishEvents.BAN.invoker().onBan(src, victim, entry.getReason(), true, expiry.getTime(), silent);

        this.getUserManager().onPunishmentPerformed(src, victim == null ? new Punishment(src, null, ip, reason, expiry) : new Punishment(src, victim, ip, reason, expiry), Punishment.Type.BAN_IP, time, silent);
        return players.size();
    }


}
