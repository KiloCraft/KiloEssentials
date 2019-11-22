package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.greedycommand.GreedyParser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.*;

public class NicknameCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> commandNode = dispatcher.register(literal("nickname")
                .requires(src -> hasPermissionOrOp(src, getCommandPermission("nickname"), 2))
                .executes(ctx -> executeUsageFor("command.nick.usage", ctx.getSource()))
                .then(argument("args", StringArgumentType.greedyString())
                        .suggests(NicknameCommand::suggestions)
                        .executes(NicknameCommand::execute)
                )
        );

        dispatcher.register(literal("nick").requires(src -> hasPermissionOrOp(src, getCommandPermission("nickname"), 2)).redirect(commandNode));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        GreedyParser gc = new GreedyParser(ctx, "args", false).parse();
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        String nick = TextFormat.translateAlternateColorCodes('&', gc.getString(0));

        if (gc.getRawArgument(1).isEmpty()) {
            KiloServer.getServer().getUserManager().getOnline(player).setNickname(nick);
            KiloChat.sendLangMessageTo(player, "command.nick.success", nick);
        }
        else
            if (hasPermission(ctx.getSource(), "nick.others")) {
                KiloServer.getServer().getUserManager().getOnline(gc.getPlayer(1));
                User target = KiloServer.getServer().getUserManager().getOnline(gc.getString(1));
                if(target == null) {
                    throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
                }

                target.setNickname(nick);
                KiloChat.sendLangMessageTo(player, "command.nick.success.others", nick, target.getUsername());
            } else
                throw new SimpleCommandExceptionType(getPermissionError("nick.others")).create();

        return SUCCESS();
    }


    private static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> suggestions = new ArrayList<String>(){{
            Optional<String> nickname = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer()).getNickname();
            if(nickname.isPresent())
                add(KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer()).getNickname().get());
        }};

        if (hasPermissionOrOp(context.getSource(), getCommandPermission("nick.others"), 2)) {
            KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> suggestions.add(player.getEntityName()));
        }

        return CommandSource.suggestMatching(suggestions, builder);
    }
}
