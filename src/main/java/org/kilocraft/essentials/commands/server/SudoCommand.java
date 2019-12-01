package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.ServerChat;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SudoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("sudo")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("sudo.others"), 3))
                .executes(c -> KiloCommands.executeUsageFor("command.sudo.usage", c.getSource()))
                .then(
                        argument("player", player())
                            .suggests(ArgumentSuggestions::allPlayers)
                            .executes(c -> KiloCommands.executeUsageFor("command.sudo.usage", c.getSource()))
                            .then(
                                    argument("args", greedyString())
                                            .suggests(ArgumentSuggestions::usableCommands)
                                            .executes(c -> execute(dispatcher, c.getSource(), getPlayer(c, "player"), getString(c, "args")))
                            )
                );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, ServerPlayerEntity player, String command) {
        KiloChat.sendLangMessageTo(source, "command.sudo.success", player.getName().asString());

        if (command.startsWith("c:")) {
            ServerChat.sendChatMessage(player, command.replaceFirst("c:", ""));
        } else if (!command.contains("sudo")) {
            try {
                dispatcher.execute(command.replace("/", ""), player.getCommandSource());
            } catch (CommandSyntaxException e) {
                source.sendError(LangText.getFormatter(true, "command.sudo.failed", player.getName().asString()));
                sendExceptionMessage(source, command, e);
            }
        } else
            KiloChat.sendLangMessageTo(source, "command.sudo.failed", player.getName().asString(), "\nYou can't loop this command!");

        return 1;
    }

    private static void sendExceptionMessage(ServerCommandSource source, String command, CommandSyntaxException e) {
        if (e.getInput() != null && e.getCursor() >= 0) {
            int int_1 = Math.min(e.getInput().length(), e.getCursor());
            Text text_1 = (new LiteralText("")).formatted(Formatting.GRAY).styled((style_1) -> {
                style_1.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            });
            if (int_1 > 10) {
                text_1.append("...");
            }

            text_1.append(e.getInput().substring(Math.max(0, int_1 - 10), int_1));
            if (int_1 < e.getInput().length()) {
                Text text_2 = (new LiteralText(e.getInput().substring(int_1))).formatted(new Formatting[]{Formatting.RED, Formatting.UNDERLINE});
                text_1.append(text_2);
            }

            text_1.append((new TranslatableText("command.context.here", new Object[0])).formatted(new Formatting[]{Formatting.RED, Formatting.ITALIC}));
            source.sendError(text_1);
        }
    }

}
