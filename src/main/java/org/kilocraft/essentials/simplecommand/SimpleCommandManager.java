package org.kilocraft.essentials.simplecommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.List;

public class SimpleCommandManager {
    private static SimpleCommandManager INSTANCE;
    private final List<SimpleCommand> commands;
    private final List<String> byId;

    public SimpleCommandManager() {
        INSTANCE = this;
        this.commands = new ArrayList<>();
        this.byId = new ArrayList<>();
    }

    public static void register(SimpleCommand command) {
        if (INSTANCE != null) {
            INSTANCE.commands.add(command);
            INSTANCE.byId.add(command.id);

            LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(command.getLabel())
                    .requires(src -> canUse(src, command));

            if (command.hasArgs) {
                builder.then(CommandManager.argument("args", StringArgumentType.greedyString())
                        .suggests(ArgumentSuggestions::noSuggestions));
            }

            KiloCommands.getDispatcher().register(builder);
        }
    }

    private static boolean canUse(ServerCommandSource src, SimpleCommand command) {
        boolean canUse = true;
        if (command.opReq != 0) {
            canUse = src.hasPermissionLevel(command.opReq);
        }

        if (command.permReq != null && !command.permReq.isEmpty()) {
            canUse = canUse || KiloCommands.hasPermission(src, command.permReq, command.opReq == 0 ? 2 : command.opReq);
        }

        return canUse && getCommand(command.getId()) != null;
    }

    public static void unregister(String id) {
        if (INSTANCE != null && getCommand(id) != null) {
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
        } catch (final ArrayIndexOutOfBoundsException ignored) {
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
                    KiloCommands.sendPermissionError(source);
                    return 0;
                }

                var = command.executable.execute(source, args, KiloEssentials.getServer());
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
                    MutableText text = (new LiteralText("")).formatted(Formatting.GRAY)
                            .styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(input).formatted(Formatting.YELLOW))));

                    if (cursor > 10) text.append("...");

                    text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                    if (cursor < e.getInput().length()) {
                        Text errorAtPointMessage = (new LiteralText(e.getInput().substring(cursor))).formatted(Formatting.RED, Formatting.UNDERLINE);
                        text.append(errorAtPointMessage);
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
