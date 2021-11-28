package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class UnBanIpCommand extends EssentialCommand {
    public UnBanIpCommand() {
        super("unban-ip", CommandPermission.IPBAN);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> user = this.argument("target", StringArgumentType.word())
                .suggests(this::listSuggestions)
                .executes((ctx) -> this.execute(ctx, false));

        LiteralArgumentBuilder<CommandSourceStack> silent = this.literal("-silent")
                .executes((ctx) -> this.execute(ctx, true));

        user.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, boolean silent) {
        CommandSourceUser src = this.getCommandSource(ctx);
        String input = this.getUserArgumentInput(ctx, "target");

        Matcher matcher = BanIpCommand.PATTERN.matcher(input);
        if (matcher.matches()) {
            return this.unBan(src, null, input, silent);
        } else {
            this.getUserManager().getUserThenAcceptAsync(src, StringArgumentType.getString(ctx, "target"), (user) -> {
                if (user.getLastIp() == null) {
                    src.sendLangError("exception.no_value_set_user", "lastSocketAddress");
                    return;
                }

                if (!KiloEssentials.getMinecraftServer().getPlayerList().getIpBans().isBanned(user.getLastIp())) {
                    src.sendLangError("command.unban.not_banned", user.getName());
                    return;
                }

                this.unBan(src, user, user.getLastIp(), silent);
            });
        }

        return AWAIT;
    }

    private int unBan(final CommandSourceUser src, @Nullable final EntityIdentifiable victim, @NotNull final String ip, boolean silent) {
        KiloEssentials.getMinecraftServer().getPlayerList().getIpBans().remove(ip);
        this.getUserManager().onPunishmentRevoked(src, victim == null ? new Punishment(src, ip) : new Punishment(src, victim, ip, null, null), Punishment.Type.BAN_IP, null, silent);
        return SUCCESS;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(KiloEssentials.getMinecraftServer().getPlayerList().getIpBans().getUserList(), builder);
    }
}
