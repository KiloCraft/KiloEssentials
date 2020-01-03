package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SudoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("sudo")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.SUDO_OTHERS, 4))
                .executes(c -> KiloCommands.executeUsageFor("command.sudo.usage", c.getSource()))
                .then(literal("-console").then(argument("args", greedyString())
                        .suggests(TabCompletions::usableCommands)
                        .executes(ctx -> exeucteConsole(dispatcher, ctx.getSource(), StringArgumentType.getString(ctx, "args"))
                        )))
                .then(argument("player", player())
                            .suggests(TabCompletions::allPlayers)
                            .executes(c -> KiloCommands.executeUsageFor("command.sudo.usage", c.getSource()))
                            .then(argument("args", greedyString())
                                            .suggests(TabCompletions::usableCommands)
                                            .executes(c -> execute(dispatcher, c.getSource(), getPlayer(c, "player"), getString(c, "args")))
                            ));

        dispatcher.register(argumentBuilder);
    }

    private static int exeucteConsole(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, String command) throws CommandSyntaxException {
        if (command.contains("sudo"))
            throw KiloCommands.getException(ExceptionMessageNode.ILLEGA_SUDO_LOOP).create();

        String consoleName = KiloServer.getServer().getVanillaServer().getCommandSource().getName();

        KiloServer.getServer().execute(command);
        KiloChat.sendLangCommandFeedback(source, "command.sudo.success", true, consoleName);

        return 1;
    }

    private static int execute(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, ServerPlayerEntity player, String command) throws CommandSyntaxException {
        if (command.contains("sudo"))
            throw KiloCommands.getException(ExceptionMessageNode.ILLEGA_SUDO_LOOP).create();

        if (command.startsWith("c:")) {
            KiloServer.getServer().getChatManager().getChannel(GlobalChat.getChannelId()).sendChatMessage(
                    KiloServer.getServer().getOnlineUser(player), command.replaceFirst("c:", ""));

            KiloChat.sendLangCommandFeedback(source, "command.sudo.chat_success", true, player.getName().asString());
            return 1;
        }

        try {
            dispatcher.execute(command.replace("/", ""), player.getCommandSource());
            KiloChat.sendLangCommandFeedback(source, "command.sudo.success", true, player.getName().asString());
        } catch (CommandSyntaxException e) {
            source.sendError(LangText.getFormatter(true, "command.sudo.failed", player.getName().asString()));
        }

        return 1;
    }

}
