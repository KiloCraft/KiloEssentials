package org.kilocraft.essentials.simplecommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config_old.ConfigCache;
import org.kilocraft.essentials.config_old.KiloConfigOLD;
import org.kilocraft.essentials.util.messages.nodes.CommandMessageNode;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.*;
import static org.kilocraft.essentials.api.ModConstants.getMessageUtil;

public class SimpleCommandManager {
    private static SimpleCommandManager INSTANCE;
    private List<SimpleCommand> commands;
    private Server server;

    public SimpleCommandManager(Server server, CommandDispatcher<ServerCommandSource> dispatcher) {
        INSTANCE = this;
        this.server = server;
        this.commands = new ArrayList<>();
    }

    public static void register(SimpleCommand command) {
        if (INSTANCE != null && INSTANCE.commands != null) {
            INSTANCE.commands.add(command);

            getDispatcher().register(literal(command.getLabel())
                    .then(argument("args", greedyString()).suggests(TabCompletions::noSuggestions)));
        }
    }

    public static void unregister(SimpleCommand command) {
        if (INSTANCE != null && INSTANCE.commands != null)
            INSTANCE.commands.remove(command);
    }

    public static SimpleCommand getCommandByLabel(String label) {
        if (INSTANCE != null && INSTANCE.commands != null)
            for (SimpleCommand command : INSTANCE.commands) {
                if (command.label.equals(label))
                    return command;
            }

        return null;
    }

    public static SimpleCommand getCommand(String id) {
        if (INSTANCE != null && INSTANCE.commands != null)
            for (SimpleCommand command : INSTANCE.commands) {
                if (command.id.equals(id))
                    return command;
            }

        return null;
    }

    public List<SimpleCommand> getCommands() {
        return this.commands;
    }

    public boolean canExecute(String input) {
        for (SimpleCommand command : commands) {
            if (command.label.equals(input.split(" ")[0].replaceFirst("/", "")))
                return true;
        }

        return false;
    }

    public int execute(String input, ServerCommandSource source) {
        int var = 0;
        String label = input.split(" ")[0].replaceFirst("/", "");
        SimpleCommand command = getCommandByLabel(label);
        String str = input.replaceFirst("/", "").replaceFirst(label + " ", "");
        String[] args = str.replaceFirst(label, "").split(" ");

        try {
            if (command != null) {
                if (command.opReq >= 1 && !source.hasPermissionLevel(command.opReq)) {
                    sendPermissionError(source);
                    return 0;
                }

                var = command.executable.execute(source, args, this.server);
            }
        } catch (CommandSyntaxException e) {
            if (e.getRawMessage().getString().equals("Unknown command")) {
                CommandPermission reqPerm = CommandPermission.getByNode(label);

                if (isCommand(label) && (reqPerm != null && !hasPermission(source, reqPerm)))
                    sendPermissionError(source);
                else
                    KiloChat.sendMessageToSource(source, new ChatMessage(
                            KiloConfigOLD.getProvider().getMessages().getMessage(ConfigCache.COMMANDS_CONTEXT_EXECUTION_EXCEPTION)
                            , true));

            } else {
                source.sendError(Texts.toText(e.getRawMessage()));

                if (e.getRawMessage().getString().equals("Incorrect argument for command"))
                    KiloChat.sendMessageToSource(source,
                            new ChatMessage(getMessageUtil().fromCommandNode(CommandMessageNode.EXECUTION_EXCEPTION_HELP), true));

                if (e.getInput() != null && e.getCursor() >= 0) {
                    int cursor = Math.min(e.getInput().length(), e.getCursor());
                    Text text = (new LiteralText("")).formatted(Formatting.GRAY).styled((style) -> {
                        style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input));
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(input).formatted(Formatting.YELLOW)));
                    });

                    if (cursor > 10) text.append("...");

                    text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                    if (cursor < e.getInput().length()) {
                        Text errorAtPointMesssage = (new LiteralText(e.getInput().substring(cursor))).formatted(Formatting.RED, Formatting.UNDERLINE);
                        text.append(errorAtPointMesssage);
                    }

                    text.append(new LiteralText("<--[HERE]").formatted(Formatting.RED, Formatting.ITALIC));
                    source.sendError(text);
                }
            }
        }

        return var;
    }

    private boolean isCommand(String label) {
        return getCommandByLabel(label) != null;
    }

}
