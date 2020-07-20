package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.mixin.ServerPlayerEntityMixin;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Date;
import java.util.List;

public class TempBanIpCommand extends EssentialCommand {
    public TempBanIpCommand() {
        super("tempban_ip", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> victim = this.getUserArgument("user");

        RequiredArgumentBuilder<ServerCommandSource, String> time = argument("time", StringArgumentType.word())
                .suggests(TimeDifferenceUtil::listSuggestions)
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason")));

        time.then(reason);
        victim.then(time);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @NotNull final String time, @Nullable final String reason) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Date date = new Date();
        Date expiry = new Date(TimeDifferenceUtil.parse(time, true));

        this.getEssentials().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (victim) -> {
            if (victim.getLastSocketAddress() == null) {
                src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                return;
            }

            BannedIpEntry entry = new BannedIpEntry(victim.getLastSocketAddress(), date, src.getName(), expiry, reason);
            super.getServer().getMinecraftServer().getPlayerManager().getIpBanList().add(entry);

            MutableText text = new TextMessage(
                    ServerUserManager.replaceVariables(super.config.moderation().disconnectReasons().tempIpBan, entry, false)
            ).toText();

            List<ServerPlayerEntity> players = super.getServer().getPlayerManager().getPlayersByIp(victim.getLastSocketAddress());
            for (ServerPlayerEntity player : players) {
                player.networkHandler.disconnect(text);
            }

            this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, victim, reason), Punishment.Type.BAN_IP, null);
        });

        return AWAIT;
    }


}
