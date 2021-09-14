package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.concurrent.CompletableFuture;

public class UnMuteCommand extends EssentialCommand {
    public UnMuteCommand() {
        super("unmute", CommandPermission.MUTE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = this.getUserArgument("victim")
                .suggests(this::listSuggestions)
                .executes((ctx) -> this.execute(ctx, false));

        LiteralArgumentBuilder<ServerCommandSource> silent = this.literal("-silent")
                .executes((ctx) -> this.execute(ctx, true));

        user.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);

        super.resolveAndGetProfileAsync(ctx, "victim").thenAcceptAsync((victim) -> {
            this.getUserManager().getMutedPlayerList().remove(victim);
            this.getUserManager().onPunishmentRevoked(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim)), Punishment.Type.MUTE, null, silent);
        });

        return AWAIT;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.getUserManager().getMutedPlayerList().getNames(), builder);
    }
}
