package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;

public class UnBanCommand extends EssentialCommand {
    public UnBanCommand() {
        super("unban", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, GameProfileArgument.Result> user = this.argument("profile", GameProfileArgument.gameProfile())
                .suggests(this::listSuggestions)
                .executes((ctx) -> this.execute(ctx, false));

        LiteralArgumentBuilder<CommandSourceStack> silent = this.literal("-silent")
                .executes((ctx) -> this.execute(ctx, true));

        user.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);
        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(ctx, "profile");
        if (gameProfiles.size() > 1) {
            throw KiloCommands.getException("exception.too_many_selections").create();
        }
        GameProfile profile = gameProfiles.iterator().next();

        if (!KiloEssentials.getMinecraftServer().getPlayerList().getBans().isBanned(profile)) {
            src.sendLangError("command.unban.not_banned", profile.getName());
            return FAILED;
        }

        KiloEssentials.getMinecraftServer().getPlayerList().getBans().remove(profile);
        this.getUserManager().onPunishmentRevoked(src, new Punishment(src, EntityIdentifiable.fromGameProfile(profile)), Punishment.Type.BAN, null, silent);

        return SUCCESS;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(KiloEssentials.getMinecraftServer().getPlayerList().getBans().getUserList(), builder);
    }
}
