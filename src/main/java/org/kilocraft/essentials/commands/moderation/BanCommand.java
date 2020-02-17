package org.kilocraft.essentials.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.user.punishment.BanEntryType;
import org.kilocraft.essentials.user.punishment.PunishmentManager;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.messages.nodes.CommandMessageNode;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.command.arguments.GameProfileArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.*;
import static org.kilocraft.essentials.user.punishment.BanEntryType.IP;
import static org.kilocraft.essentials.user.punishment.BanEntryType.PROFILE;

public class BanCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> rootCommand = literal("ke_ban")
                .requires(src -> hasPermission(src, "ban", 3))
                .executes(KiloCommands::executeSmartUsage);

        LiteralArgumentBuilder<ServerCommandSource> setArg = literal("set");
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgument> profileArg = argument("profile", gameProfile())
                    .suggests(TabCompletions::allPlayers);
            RequiredArgumentBuilder<ServerCommandSource, String> typeArg = argument("type", string())
                    .suggests(BanCommand::ENTRY_TYPE_SUGGESTIONS);

            LiteralArgumentBuilder<ServerCommandSource> permanentArg = literal("permanent")
                    .then(argument("reason", greedyString())
                            .executes(ctx -> executeSet(ctx, false)));
            LiteralArgumentBuilder<ServerCommandSource> temporaryArg = literal("temporary")
                    .then(argument("time", string()).suggests(TimeDifferenceUtil::listSuggestions)
                            .then(argument("reason", greedyString())
                                    .executes(ctx -> executeSet(ctx, true))));

            typeArg.then(permanentArg);
            typeArg.then(temporaryArg);
            profileArg.then(typeArg);
            setArg.then(profileArg);
        }

        LiteralArgumentBuilder<ServerCommandSource> clearArg = literal("clear");
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgument> profileArg = argument("profile", gameProfile())
                    .suggests(TabCompletions::allPlayers);
            RequiredArgumentBuilder<ServerCommandSource, String> typeArg = argument("type", string())
                    .suggests(BanCommand::ENTRY_TYPE_SUGGESTIONS)
                    .executes(BanCommand::executeClear);

            clearArg.then(profileArg);
        }

        LiteralArgumentBuilder<ServerCommandSource> checkArg = literal("check");
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgument> profileArg = argument("profile", gameProfile())
                    .suggests(TabCompletions::allPlayers);

            checkArg.then(profileArg);
        }

        LiteralArgumentBuilder<ServerCommandSource> listArg = literal("list");


        rootCommand.then(setArg);
        rootCommand.then(clearArg);
        rootCommand.then(checkArg);
        rootCommand.then(listArg);
        dispatcher.register(rootCommand);
    }

    private static PunishmentManager punishmentManager = KiloServer.getServer().getUserManager().getPunishmentManager();

    private static int executeSet(CommandContext<ServerCommandSource> ctx, boolean isTemporary) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        String reason = getString(ctx, "reason");

        if (isTemporary) {
//            DateArgument dateArgument = DateArgument.complex(getString(ctx, "time")).parse();
//            src.sendFeedback(new LiteralText("TIME: " + dateArgument.getDate()), false);
        }

        return SUCCESS();
    }

    private static int executeClear(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        Collection<GameProfile> gameProfiles = getProfileArgument(ctx, "profile");

        if (gameProfiles.size() > 1)
            throw getException(ExceptionMessageNode.TOO_MANY_SELECTIONS).create();

        GameProfile targetProfile = gameProfiles.iterator().next();

        BanEntryType entryType = getEntryType(ctx);

        if (entryType.equals(PROFILE) && !punishmentManager.isProfileBanned(targetProfile))
            throw getException(CommandMessageNode.BAN_NOT_BANNED, targetProfile.getName(), getEntryType(ctx).name().toLowerCase()).create();

        punishmentManager.pardon(targetProfile, getEntryType(ctx));

        KiloChat.sendLangMessageTo(src, "command.ban.remove", targetProfile.getName(), getEntryType(ctx).name().toLowerCase());
        return SUCCESS();
    }


    private static BanEntryType getEntryType(CommandContext<ServerCommandSource> ctx) {
        String input = getString(ctx, "type");
        return input.equals("profile") ? PROFILE : IP;
    }

    private static CompletableFuture<Suggestions> ENTRY_TYPE_SUGGESTIONS(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"ip", "profile"}, builder);
    }

}
