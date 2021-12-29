package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommand extends EssentialCommand {
    public static final Pattern PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public BanIpCommand() {
        super("ke_ban-ip", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> victim = this.getUserArgument("target")
                .executes((ctx) -> this.execute(ctx, null, false));

        RequiredArgumentBuilder<CommandSourceStack, String> reason = this.argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<CommandSourceStack> silent = this.literal("-silent").then(
                this.getUserArgument("target")
                        .executes((ctx) -> this.execute(ctx, null, true))
                        .then(this.argument("reason", StringArgumentType.greedyString())
                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), true)))
        );

        victim.then(reason);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, @Nullable final String reason, boolean silent) {
        CommandSourceUser src = this.getCommandSource(ctx);
        String input = this.getUserArgumentInput(ctx, "target");
        Matcher matcher = PATTERN.matcher(input);
        if (matcher.matches()) {
            return this.banIp(src, null, input, reason, silent);
        } else {
            this.getUserManager().getUserThenAcceptAsync(src, input, (victim) -> {
                if (victim.getLastIp() == null) {
                    src.sendLangError("exception.no_value_set_user", "lastSocketAddress");
                    return;
                }

                this.banIp(src, victim, victim.getLastIp(), reason, silent);
            });

        }

        return AWAIT;
    }

    private int banIp(final CommandSourceUser src, @Nullable EntityIdentifiable victim, @NotNull String ip, @Nullable final String reason, boolean silent) {
        IpBanList bannedIpList = KiloEssentials.getMinecraftServer().getPlayerList().getIpBans();
        List<ServerPlayer> players = KiloEssentials.getMinecraftServer().getPlayerList().getPlayersWithAddress(ip);
        Date date = new Date();

        IpBanListEntry entry = new IpBanListEntry(ip, date, src.getName(), null, reason);
        bannedIpList.add(entry);

        MutableComponent text = ComponentText.toText(
                ServerUserManager.replaceBanVariables(KiloConfig.main().moderation().messages().permIpBan, entry, true)
        );

        for (ServerPlayer player : players) {
            player.connection.disconnect(text);
        }

        PunishEvents.BAN.invoker().onBan(src, victim, entry.getReason(), true, -1L, silent);

        this.getUserManager().onPunishmentPerformed(src, victim == null ? new Punishment(src, ip, reason) : new Punishment(src, victim, ip, reason, null), Punishment.Type.BAN_IP, null, silent);
        return players.size();
    }


}
