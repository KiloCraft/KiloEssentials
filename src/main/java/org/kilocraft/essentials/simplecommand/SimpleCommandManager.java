package org.kilocraft.essentials.simplecommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.List;

public class SimpleCommandManager {
    private static SimpleCommandManager INSTANCE;
    private List<SimpleCommand> commands;
    private List<String> byId;
    private Server server;

    public SimpleCommandManager(Server server, CommandDispatcher<ServerCommandSource> dispatcher) {
        INSTANCE = this;
        this.server = server;
        this.commands = new ArrayList<>();
        this.byId = new ArrayList<>();
    }

    public static void register(SimpleCommand command) {
        if (INSTANCE != null) {
            INSTANCE.commands.add(command);
            INSTANCE.byId.add(command.id);

            KiloCommands.getDispatcher().register(CommandManager.literal(command.getLabel())
                    .then(
                            CommandManager.argument("args", StringArgumentType.greedyString())
                                    .requires(src -> INSTANCE.byId.contains(command.id))
                                    .suggests(ArgumentCompletions::noSuggestions)
                    )
            );
        }
    }

    public static void unregister(String id) {
        if (INSTANCE != null &&  getCommand(id) != null) {
            unregister(getCommand(id));
        }
    }

    public static void unregister(SimpleCommand command) {
        if (INSTANCE != null && INSTANCE.commands != null) {
            INSTANCE.commands.remove(command);
            INSTANCE.byId.remove(command.id);
        }
    }

    public static SimpleCommand getCommandByLabel(String label) {
        if (INSTANCE != null && INSTANCE.commands != null)
            for (SimpleCommand command : INSTANCE.commands) {
                if (command.label.equals(label)) {
                    return command;
                }
            }

        return null;
    }

    @Nullable
    public static SimpleCommand getCommand(String id) {
        if (INSTANCE != null && INSTANCE.commands != null) {
            for (SimpleCommand command : INSTANCE.commands) {
                if (command.id.equals(id)) {
                    return command;
                }
            }
        }

        return null;
    }

    public List<SimpleCommand> getCommands() {
        return this.commands;
    }

    public boolean canExecute(String input) {
        try {
            for (SimpleCommand command : commands) {
                if (command.label.equals(input.split(" ")[0].replaceFirst("/", ""))) {
                    return true;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException ignored) {}

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
                    KiloCommands.sendPermissionError(source);
                    return 0;
                }

                var = command.executable.execute(source, args, this.server);
            }
        } catch (CommandSyntaxException e) {
            if (e.getRawMessage().getString().equals("Unknown command")) {
                CommandPermission reqPerm = CommandPermission.getByNode(label);

                if (isCommand(label) && (reqPerm != null && !KiloCommands.hasPermission(source, reqPerm)))
                    KiloCommands.sendPermissionError(source);
                else
                    KiloChat.sendMessageToSource(source, new TextMessage(
                            KiloConfig.messages().commands().context().executionException
                            , true));

            } else {
                source.sendError(Texts.toText(e.getRawMessage()));

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
