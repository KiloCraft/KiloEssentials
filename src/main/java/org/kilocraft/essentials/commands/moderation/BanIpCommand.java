package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Date;

public class BanIpCommand extends EssentialCommand {
    public BanIpCommand() {
        super("ban_ip", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> victim = this.getUserArgument("user")
                .executes((ctx) -> this.execute(ctx, null));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason")));

        victim.then(reason);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @Nullable final String reason) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Date date = new Date();

        this.getEssentials().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (victim) -> {
            if (victim.getLastSocketAddress() == null) {
                src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                return;
            }

            BannedIpEntry entry = new BannedIpEntry(victim.getLastSocketAddress(), date, src.getName(), null, reason);
            super.getServer().getMinecraftServer().getPlayerManager().getIpBanList().add(entry);

            if (super.isOnline(victim.getId())) {
                super.getOnlineUser(victim.getId()).asPlayer().networkHandler.disconnect(
                        new TextMessage(
                                super.config.moderation().disconnectReasons().permIpBan
                                        .replace("{BAN_SOURCE}", entry.getSource())
                                        .replace("{BAN_REASON}", entry.getReason())
                        ).toText()
                );
            }

            this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, victim, reason), Punishment.Type.BAN_IP, null);
        });

        return AWAIT;
    }


}
