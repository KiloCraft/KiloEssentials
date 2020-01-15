package org.kilocraft.essentials;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.help.UsageCommand;
import org.kilocraft.essentials.commands.inventory.AnvilCommand;
import org.kilocraft.essentials.commands.inventory.EnderchestCommand;
import org.kilocraft.essentials.commands.item.ItemCommand;
import org.kilocraft.essentials.commands.locate.WorldLocateCommand;
import org.kilocraft.essentials.commands.messaging.*;
import org.kilocraft.essentials.commands.misc.ColorsCommand;
import org.kilocraft.essentials.commands.misc.HelpCommand;
import org.kilocraft.essentials.commands.misc.PingCommand;
import org.kilocraft.essentials.commands.misc.PreviewCommand;
import org.kilocraft.essentials.commands.moderation.ClearchatCommand;
import org.kilocraft.essentials.commands.play.*;
import org.kilocraft.essentials.commands.server.*;
import org.kilocraft.essentials.commands.teleport.BackCommand;
import org.kilocraft.essentials.commands.teleport.RtpCommand;
import org.kilocraft.essentials.commands.teleport.TeleportCommands;
import org.kilocraft.essentials.commands.teleport.TpaCommand;
import org.kilocraft.essentials.commands.world.TimeCommand;
import org.kilocraft.essentials.config.ConfigCache;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.events.commands.OnCommandExecutionEventImpl;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.util.messages.MessageUtil;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;
import org.kilocraft.essentials.util.messages.nodes.CommandMessageNode;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static io.github.indicode.fabric.permissions.Thimble.permissionWriters;
import static org.kilocraft.essentials.api.KiloEssentials.getLogger;
import static org.kilocraft.essentials.api.KiloEssentials.getServer;
import static org.kilocraft.essentials.commands.LiteralCommandModified.*;

public class KiloCommands {
    private static List<String> initializedPerms = new ArrayList<>();
    private CommandDispatcher<ServerCommandSource> dispatcher;
    private SimpleCommandManager simpleCommandManager;
    private static MessageUtil messageUtil = ModConstants.getMessageUtil();
    public static String PERMISSION_PREFIX = "kiloessentials.command.";

    public KiloCommands() {
        this.dispatcher = KiloEssentialsImpl.commandDispatcher;
        this.simpleCommandManager = new SimpleCommandManager(KiloServer.getServer(), this.dispatcher);
        register(true);
    }

    public static boolean hasPermission(ServerCommandSource src, CommandPermission perm) {
        return hasPermissionOrOp(src, perm.getNode(), 2);
    }

    public static boolean hasPermission(ServerCommandSource src, CommandPermission perm, int minOpLevel) {
        return hasPermissionOrOp(src, perm.getNode(), minOpLevel);
    }

    @Deprecated
    public static boolean hasPermission(ServerCommandSource src, String cmdPerm, int minOpLevel) {
        return hasPermissionOrOp(src, cmdPerm, minOpLevel);
    }

    private void register(boolean devEnv) {
        if (devEnv) {
            KiloEssentials.getLogger().warn("[!] Alert: Server is running in development mode!");
            SharedConstants.isDevelopment = true;
        }

        permissionWriters.add((map, server) -> {
            for (CommandPermission perm : CommandPermission.values()) {
                map.registerPermission(perm.getNode(), PermChangeBehavior.UPDATE_COMMAND_TREE);
            }
        });

        //TODO: Fix the Toast suggestions
        registerToast();

        VersionCommand.register(this.dispatcher);
        HelpCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        ColorsCommand.register(this.dispatcher);
        GamemodeCommand.register(this.dispatcher);
        TpaCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        RtpCommand.register(this.dispatcher);
        MessageCommand.register(this.dispatcher);
        SudoCommand.register(this.dispatcher);
        BroadcastCommand.register(this.dispatcher);
        UsageCommand.register(this.dispatcher);
        PlayerParticlesCommand.register(this.dispatcher);
        AnvilCommand.register(this.dispatcher);
        ItemCommand.register(this.dispatcher);
        ColorsCommand.register(this.dispatcher);
        WorldLocateCommand.register(this.dispatcher);
        BackCommand.register(this.dispatcher);
        HealCommand.register(this.dispatcher);
        FeedCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        FlyCommand.register(this.dispatcher);
        StopCommand.register(this.dispatcher);
        RestartCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);
        InvulnerablemodeCommand.register(this.dispatcher);
        PreviewCommand.register(this.dispatcher);
        TeleportCommands.register(this.dispatcher);
        NicknameCommand.register(this.dispatcher);
        PingCommand.register(this.dispatcher);
        ClearchatCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);
        SaveCommand.register(this.dispatcher);
        StaffmsgCommand.register(this.dispatcher);
        BuildermsgCommand.register(this.dispatcher);
        SocialspyCommand.register(this.dispatcher);
        CommandspyCommand.register(this.dispatcher);
        StatusCommand.register(this.dispatcher);
        //InventoryCommand.register(this.dispatcher);
        SayasCommand.register(this.dispatcher);
    }

    private void registerToast() {
        ArgumentCommandNode<ServerCommandSource, String> toast = CommandManager.argument("label", StringArgumentType.string())
                .then(CommandManager.argument("args", StringArgumentType.greedyString())
                        .suggests(TabCompletions::noSuggestions))
                .build();

        getDispatcher().getRoot().addChild(toast);
    }

    public static CompletableFuture<Suggestions> toastSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();

        for (SimpleCommand command : KiloEssentials.getInstance().getCommandHandler().simpleCommandManager.getCommands()) {
            suggestions.add(command.getLabel());
        }

        getDispatcher().getRoot().getChildren().stream().filter((child) ->
                  child instanceof LiteralCommandNode && canSourceUse(child, context.getSource()) &&
                        !isVanillaCommand(child.getName()) && shouldUse(child.getName()))
                .map(CommandNode::getName).forEach(suggestions::add);


        return CommandSource.suggestMatching(suggestions, builder);
    }


    public static int executeUsageFor(String langKey, ServerCommandSource source) {
        String fromLang = ModConstants.getLang().getProperty(langKey);
        if (fromLang != null)
            KiloChat.sendMessageToSource(source, new ChatMessage("&6Command usage:\n" + fromLang, true));
        else
            KiloChat.sendLangMessageTo(source, "general.usage.help");
        return 1;
    }

    public static int executeSmartUsage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String command = ctx.getInput().replace("/", "");
        ParseResults<ServerCommandSource> parseResults = getDispatcher().parse(command, ctx.getSource());
        if (parseResults.getContext().getNodes().isEmpty()) {
            throw getException(ExceptionMessageNode.UNKNOWN_COMMAND_EXCEPTION).create();
        } else {
            Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = getDispatcher().getSmartUsage(((ParsedCommandNode)Iterables.getLast(parseResults.getContext().getNodes())).getNode(), ctx.getSource());
            Iterator<String> iterator = commandNodeStringMap.values().iterator();

            KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.firstRow", command);
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.commandRow", command, "");

            while (iterator.hasNext()) {
                if (iterator.next().equals("/" + command)) continue;
                KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.commandRow", command, iterator.next());
            }

            return 1;
        }
    }

    public static int executeSmartUsageFor(String command, ServerCommandSource source) throws CommandSyntaxException {
        ParseResults<ServerCommandSource> parseResults = getDispatcher().parse(command, source);
        if (parseResults.getContext().getNodes().isEmpty()) {
            throw getException(ExceptionMessageNode.UNKNOWN_COMMAND_EXCEPTION).create();
        } else {
            Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = getDispatcher().getSmartUsage(((ParsedCommandNode)Iterables.getLast(parseResults.getContext().getNodes())).getNode(), source);
            Iterator<String> iterator = commandNodeStringMap.values().iterator();

            KiloChat.sendLangMessageTo(source, "command.usage.firstRow", parseResults.getReader().getString());
            if (parseResults.getContext().getNodes().get(0).getNode().getCommand() != null)
                KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), "");

            int usages = 0;
            while (iterator.hasNext()) {
                usages++;
                String usage = iterator.next();
                KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), usage);
            }

            if (usages == 0) KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), "");

            return commandNodeStringMap.size();
        }
    }

    public static LiteralText getPermissionError(String hoverText) {
        LiteralText literalText = LangText.get(true, "command.exception.permission");
        literalText.styled((style) -> {
           style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hoverText).formatted(Formatting.YELLOW)));
        });
        return literalText;
    }

    public static void sendPermissionError(ServerCommandSource source) {
        KiloChat.sendMessageToSource(source, new ChatMessage(
                KiloConfig.getProvider().getMessages().getMessage(ConfigCache.COMMANDS_CONTEXT_PERMISSION_EXCEPTION)
                ,true));
    }

    public static SimpleCommandExceptionType getException(ExceptionMessageNode node, Object... objects) {
        String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        return commandException(
                new LiteralText((objects != null) ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    public static SimpleCommandExceptionType getException(CommandMessageNode node, Object... objects) {
        String message = ModConstants.getMessageUtil().fromCommandNode(node);
        return commandException(
                new LiteralText((objects != null) ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    public static SimpleCommandExceptionType commandException(String message) {
        return new SimpleCommandExceptionType(
                new LiteralText(TextFormat.translateAlternateColorCodes('&', message)));
    }

    public static SimpleCommandExceptionType commandException(Text text) {
        return new SimpleCommandExceptionType(text);
    }

    public static SimpleCommandExceptionType getArgException(ArgExceptionMessageNode node, Object... objects) {
        String message = ModConstants.getMessageUtil().fromArgumentExceptionNode(node);
        return commandException(
                new LiteralText((objects != null) ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    public static void updateCommandTreeForEveryone() {
        for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            KiloServer.getServer().getPlayerManager().sendCommandTree(playerEntity);
        }
    }

    public int execute(ServerCommandSource executor, String commandToExecute) {
        OnCommandExecutionEvent event = new OnCommandExecutionEventImpl(executor, commandToExecute);
        String cmd = commandToExecute;

        if (!commandToExecute.endsWith("--push") && !executor.hasPermissionLevel(4))
            KiloServer.getServer().triggerEvent(event);
        else
            cmd = commandToExecute.replace(" --push", "");

        if (event.isCancelled()) return 0;

        if (this.simpleCommandManager.canExecute(cmd))
            return this.simpleCommandManager.execute(cmd, executor);

        StringReader stringReader = new StringReader(cmd);

        if (stringReader.canRead() && stringReader.peek() == '/')
            stringReader.skip();

        getServer().getVanillaServer().getProfiler().push(cmd);

        byte var = 0;
        try {
            try {
                return this.dispatcher.execute(stringReader, executor);
            } catch (CommandException e) {
                executor.sendError(e.getTextMessage());
                var = 0;
                return var;
            } catch (CommandSyntaxException e) {
                if (e.getRawMessage().getString().equals("Unknown command")) {
                    String literalName = cmd.split(" ")[0].replace("/", "");
                    CommandPermission reqPerm = CommandPermission.getByNode(literalName);

                    if (isCommand(literalName) && (reqPerm != null && !hasPermission(executor, reqPerm)))
                        sendPermissionError(executor);
                    else
                        KiloChat.sendMessageToSource(executor, new ChatMessage(
                                KiloConfig.getProvider().getMessages().getMessage(ConfigCache.COMMANDS_CONTEXT_EXECUTION_EXCEPTION)
                                , true));

                } else {
                    executor.sendError(Texts.toText(e.getRawMessage()));

                    if (e.getRawMessage().getString().equals("Incorrect argument for command"))
                        KiloChat.sendMessageToSource(executor,
                                new ChatMessage(messageUtil.fromCommandNode(CommandMessageNode.EXECUTION_EXCEPTION_HELP), true));

                    if (e.getInput() != null && e.getCursor() >= 0) {
                        int cursor = Math.min(e.getInput().length(), e.getCursor());
                        Text text = (new LiteralText("")).formatted(Formatting.GRAY).styled((style) -> {
                            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandToExecute));
                            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(commandToExecute).formatted(Formatting.YELLOW)));
                        });

                        if (cursor > 10) text.append("...");

                        text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                        if (cursor < e.getInput().length()) {
                            Text errorAtPointMesssage = (new LiteralText(e.getInput().substring(cursor))).formatted(Formatting.RED, Formatting.UNDERLINE);
                            text.append(errorAtPointMesssage);
                        }

                        text.append(new LiteralText("<--[HERE]").formatted(Formatting.RED, Formatting.ITALIC));
                        executor.sendError(text);
                    }
                }

            }
        } catch (Exception e) {
            Text text = new LiteralText(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            if (SharedConstants.isDevelopment) {
                getLogger().error("Command exception: {}", commandToExecute, e);
                StackTraceElement[] stackTraceElements = e.getStackTrace();

                for(int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    text.append("\n\n").append(stackTraceElements[i].getMethodName()).append("\n ").append(stackTraceElements[i].getFileName()).append(":").append(String.valueOf(stackTraceElements[i].getLineNumber()));
                }
            }

            executor.sendError((new TranslatableText("command.failed")).styled((style) -> {
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text));
            }));

            if (SharedConstants.isDevelopment) {
                executor.sendError(new LiteralText(Util.getInnermostMessage(e)));
                getLogger().error("'" + commandToExecute + "' threw an exception", e);
            }

            return (byte) 0;

        } finally {
            getServer().getVanillaServer().getProfiler().pop();
        }

        return var;
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return KiloEssentialsImpl.commandDispatcher;
    }

    private boolean isCommand(String literal) {
        return dispatcher.getRoot().getChild(literal) != null;
    }

    public static int SUCCESS() {
        return 1;
    }

    public static int FAILED() {
        return -1;
    }

}
