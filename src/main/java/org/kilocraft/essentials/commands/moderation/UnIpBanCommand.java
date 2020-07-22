package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.concurrent.CompletableFuture;

public class UnIpBanCommand extends EssentialCommand {
    public UnIpBanCommand() {
        super("unip_ban", CommandPermission.IPBAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = argument("profile", StringArgumentType.string())
                .suggests(this::listSuggestions)
                .executes((ctx) -> this.execute(ctx, false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent")
                .executes((ctx) -> this.execute(ctx, true));

        user.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, boolean silent) {
        CommandSourceUser src = this.getServerUser(ctx);

        this.getEssentials().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (user) -> {
            if (user.getLastSocketAddress() == null) {
                src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                return;
            }

            if (!super.getServer().getPlayerManager().getIpBanList().isBanned(user.getLastSocketAddress())) {
                src.sendLangError("command.unban.not_banned", user.getName());
                return;
            }

            super.getServer().getPlayerManager().getIpBanList().remove(user.getLastSocketAddress());
            this.getServer().getUserManager().onPunishmentRevoked(src, new Punishment(src, user), Punishment.Type.BAN_IP, null, silent);
        });

        return AWAIT;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(super.getServer().getPlayerManager().getUserBanList().getNames(), builder);
    }
}
