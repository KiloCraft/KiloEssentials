package org.kilocraft.essentials.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class UnBanCommand extends EssentialCommand {
    public UnBanCommand() {
        super("unban", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> user = argument("profile", GameProfileArgumentType.gameProfile())
                .suggests(this::listSuggestions)
                .executes((ctx) -> this.execute(ctx, false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent")
                .executes((ctx) -> this.execute(ctx, true));

        user.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getServerUser(ctx);
        Collection<GameProfile> gameProfiles = GameProfileArgumentType.getProfileArgument(ctx, "profile");
        if (gameProfiles.size() > 1) {
            throw KiloCommands.getException(ExceptionMessageNode.TOO_MANY_SELECTIONS).create();
        }
        GameProfile profile = gameProfiles.iterator().next();

        if (!super.getServer().getPlayerManager().getUserBanList().contains(profile)) {
            src.sendLangError("command.unban.not_banned", profile.getName());
            return FAILED;
        }

        super.getServer().getPlayerManager().getUserBanList().remove(profile);
        this.getServer().getUserManager().onPunishmentRevoked(src, new Punishment(src, EntityIdentifiable.fromGameProfile(profile)), Punishment.Type.BAN, null, silent);

        return SUCCESS;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(super.getServer().getPlayerManager().getUserBanList().getNames(), builder);
    }
}
