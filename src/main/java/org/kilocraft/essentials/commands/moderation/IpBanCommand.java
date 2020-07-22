package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Date;
import java.util.List;

public class IpBanCommand extends EssentialCommand {
    public IpBanCommand() {
        super("ip_ban", CommandPermission.BAN);
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
        Date date = new Date();

        this.getEssentials().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (victim) -> {
            if (victim.getLastSocketAddress() == null) {
                src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                return;
            }

            BannedIpEntry entry = new BannedIpEntry(victim.getLastSocketAddress(), date, src.getName(), null, reason);
            super.getServer().getMinecraftServer().getPlayerManager().getIpBanList().add(entry);

            MutableText text = new TextMessage(
                    ServerUserManager.replaceVariables(super.config.moderation().messages().permIpBan, entry, true)
            ).toText();

            List<ServerPlayerEntity> players = super.getServer().getPlayerManager().getPlayersByIp(victim.getLastSocketAddress());
            for (ServerPlayerEntity player : players) {
                player.networkHandler.disconnect(text);
            }

            this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, victim, reason), Punishment.Type.BAN_IP, null, silent);
        });

        return AWAIT;
    }


}
