package org.kilocraft.essentials;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.commands.LiteralCommandModified;
import org.kilocraft.essentials.commands.debug.DebugEssentialsCommand;
import org.kilocraft.essentials.commands.help.HelpMeCommand;
import org.kilocraft.essentials.commands.help.UsageCommand;
import org.kilocraft.essentials.commands.inventory.AnvilCommand;
import org.kilocraft.essentials.commands.inventory.EnderchestCommand;
import org.kilocraft.essentials.commands.inventory.InventoryCommand;
import org.kilocraft.essentials.commands.inventory.WorkbenchCommand;
import org.kilocraft.essentials.commands.item.ItemCommand;
import org.kilocraft.essentials.commands.locate.LocateCommand;
import org.kilocraft.essentials.commands.messaging.*;
import org.kilocraft.essentials.commands.misc.*;
import org.kilocraft.essentials.commands.moderation.BanCommand;
import org.kilocraft.essentials.commands.moderation.ClearChatCommand;
import org.kilocraft.essentials.commands.moderation.IpInfoCommand;
import org.kilocraft.essentials.commands.moderation.KickCommand;
import org.kilocraft.essentials.commands.play.*;
import org.kilocraft.essentials.commands.server.*;
import org.kilocraft.essentials.commands.teleport.BackCommand;
import org.kilocraft.essentials.commands.teleport.RtpCommand;
import org.kilocraft.essentials.commands.teleport.TeleportCommands;
import org.kilocraft.essentials.commands.teleport.tpr.*;
import org.kilocraft.essentials.commands.user.LastSeenCommand;
import org.kilocraft.essentials.commands.user.SilenceCommand;
import org.kilocraft.essentials.commands.user.WhoIsCommand;
import org.kilocraft.essentials.commands.user.WhoWasCommand;
import org.kilocraft.essentials.commands.world.TimeCommand;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.events.commands.OnCommandExecutionEventImpl;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;
import org.kilocraft.essentials.util.messages.nodes.CommandMessageNode;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.api.KiloEssentials.getLogger;
import static org.kilocraft.essentials.api.KiloEssentials.getServer;
import static org.kilocraft.essentials.commands.LiteralCommandModified.*;

public class KiloCommands {
    private static final List<String> initializedPerms = new ArrayList<>();
    private final List<IEssentialCommand> commands;
    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private final SimpleCommandManager simpleCommandManager;
    static final String PERMISSION_PREFIX = "kiloessentials.command.";
    private static LiteralCommandNode<ServerCommandSource> rootNode;

    public KiloCommands() {
        this.dispatcher = KiloEssentialsImpl.commandDispatcher;
        this.simpleCommandManager = new SimpleCommandManager(KiloServer.getServer(), this.dispatcher);
        this.commands = new ArrayList<>();
        KiloCommands.rootNode = literal("essentials").executes(this::sendInfo).build();
        this.register();
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm) {
        return KiloEssentials.getInstance().getPermissionUtil().hasPermission(src, perm.getNode(), 2);
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm, final int minOpLevel) {
        return KiloEssentials.getInstance().getPermissionUtil().hasPermission(src, perm.getNode(), minOpLevel);
    }

    public static boolean hasPermission(final ServerCommandSource src, final String cmdPerm, final int minOpLevel) {
        return KiloEssentials.getInstance().getPermissionUtil().hasPermission(src, cmdPerm, 2);
    }

    private void register() {
        final List<IEssentialCommand> commandsList = new ArrayList<IEssentialCommand>() {{
            this.add(new DebugEssentialsCommand());
            this.add(new LightningCommand());
            this.add(new NicknameCommand());
            this.add(new SayAsCommand());
            this.add(new SudoCommand());
            this.add(new ItemCommand());
            this.add(new WorkbenchCommand());
            this.add(new AnvilCommand());
            this.add(new SignEditCommand());
            this.add(new HatCommand());
            this.add(new VersionCommand());
            this.add(new ReloadCommand());
            this.add(new TextFormattingCommand());
            this.add(new CommandFormattingCommand());
            this.add(new GamemodeCommand());
            this.add(new RtpCommand());
            this.add(new BroadcastCommand());
            this.add(new UsageCommand());
            this.add(new HealCommand());
            this.add(new FeedCommand());
            this.add(new TimeCommand());
            this.add(new FlyCommand());
            this.add(new InvulnerableCommand());
            this.add(new FormatPreviewCommand());
            this.add(new PingCommand());
            this.add(new ClearChatCommand());
            this.add(new EnderchestCommand());
            this.add(new StatusCommand());
            this.add(new StaffMessageCommand());
            this.add(new BuilderMsgCommand());
            this.add(new SocialSpyCommand());
            this.add(new CommandSpyCommand());
            this.add(new BackCommand());
            this.add(new ModsCommand());
            this.add(new TpsCommand());
            this.add(new LocateCommand());
            this.add(new MessageCommand());
            this.add(new DoNotDisturbCommand());
            this.add(new IgnoreCommand());
            this.add(new IgnoreListCommand());
            this.add(new ReplyCommand());
            this.add(new RealNameCommand());
            this.add(new IpInfoCommand());
            this.add(new HelpCommand());
            this.add(new WhoIsCommand());
            this.add(new WhoWasCommand());
            this.add(new PlaytimeCommand());
            this.add(new MotdCommand());
            this.add(new HelpMeCommand());
            this.add(new PlaytimeTopCommand());
            this.add(new BanCommand());
            this.add(new KickCommand());
            this.add(new SilenceCommand());
            this.add(new TpaCommand());
            this.add(new TpaHereCommand());
            this.add(new TpAcceptCommand());
            this.add(new TpDenyCommand());
            this.add(new TpCancelCommand());
            this.add(new LastSeenCommand());
            this.add(new InventoryCommand());
            this.add(new CalculateCommand());
            this.add(new HugCommand());
            this.add(new GlowCommand());
        }};

        this.commands.addAll(commandsList);

        for (final IEssentialCommand command : this.commands) {
            this.registerCommand(command);
        }

        this.dispatcher.getRoot().addChild(KiloCommands.rootNode);

        StopCommand.register(this.dispatcher);
        RestartCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);
        TeleportCommands.register(this.dispatcher);
    }

    public <C extends IEssentialCommand> void register(final C c) {
        this.registerCommand(c);
    }

    private <C extends IEssentialCommand> void registerCommand(@NotNull final C c) {
        EssentialCommand command = (EssentialCommand) c;
        command.register(this.dispatcher);

        if (command.getForkType() == IEssentialCommand.ForkType.DEFAULT || command.getForkType() == IEssentialCommand.ForkType.SUB_ONLY) {
            KiloCommands.rootNode.addChild(command.getArgumentBuilder().build());
            KiloCommands.rootNode.addChild(command.getCommandNode());
        }

        if (command.getForkType().shouldRegisterOnMain()) {
            if (command.getAlias() != null) {
                for (String alias : command.getAlias()) {
                    LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal(alias)
                            .requires(command.getRootPermissionPredicate())
                            .executes(command.getArgumentBuilder().getCommand());

                    for (CommandNode<ServerCommandSource> child : command.getCommandNode().getChildren()) {
                        argumentBuilder.then(child);
                    }

                    this.dispatcher.register(argumentBuilder);
                }
            }

            this.dispatcher.getRoot().addChild(command.getCommandNode());
            this.dispatcher.register(command.getArgumentBuilder());
        }
    }

    public static CompletableFuture<Suggestions> toastSuggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        final List<String> suggestions = new ArrayList<>();

        for (final SimpleCommand command : KiloEssentials.getInstance().getCommandHandler().simpleCommandManager.getCommands()) {
            suggestions.add(command.getLabel());
        }

        KiloCommands.getDispatcher().getRoot().getChildren().stream().filter(child ->
                  child instanceof LiteralCommandNode && canSourceUse(child, context.getSource()) &&
                        !isVanillaCommand(child.getName()) && shouldUse(child.getName()))
                .map(CommandNode::getName).forEach(suggestions::add);


        return CommandSource.suggestMatching(suggestions, builder);
    }

    public static int executeUsageFor(final String langKey, final ServerCommandSource source) {
        final String fromLang = ModConstants.getLang().getProperty(langKey);
        if (fromLang != null)
            KiloChat.sendMessageToSource(source, new TextMessage("&6Command usage:\n" + fromLang, true));
        else
            KiloChat.sendLangMessageTo(source, "general.usage.help");
        return 1;
    }

    public static int executeSmartUsage(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String command = ctx.getInput().replace("/", "");
        final ParseResults<ServerCommandSource> parseResults = KiloCommands.getDispatcher().parse(command, ctx.getSource());
        if (parseResults.getContext().getNodes().isEmpty())
            throw KiloCommands.getException(ExceptionMessageNode.UNKNOWN_COMMAND_EXCEPTION).create();

        final Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = KiloCommands.getDispatcher().getSmartUsage(((ParsedCommandNode) Iterables.getLast(parseResults.getContext().getNodes())).getNode(), ctx.getSource());
        final Iterator<String> iterator = commandNodeStringMap.values().iterator();

        KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.firstRow", command);
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.commandRow", command, "");

        while (iterator.hasNext()) {
            if (iterator.next().equals("/" + command)) continue;
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.commandRow", command, iterator.next());
        }

        return 1;
    }

    public static int executeSmartUsageFor(final String command, final ServerCommandSource source) throws CommandSyntaxException {
        final ParseResults<ServerCommandSource> parseResults = KiloCommands.getDispatcher().parse(command, source);
        if (parseResults.getContext().getNodes().isEmpty())
            throw KiloCommands.getException(ExceptionMessageNode.UNKNOWN_COMMAND_EXCEPTION).create();

        final Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = KiloCommands.getDispatcher().getSmartUsage(((ParsedCommandNode)Iterables.getLast(parseResults.getContext().getNodes())).getNode(), source);
        final Iterator<String> iterator = commandNodeStringMap.values().iterator();

        int usages = 0;
        KiloChat.sendLangMessageTo(source, "command.usage.firstRow", parseResults.getReader().getString());
        if (parseResults.getContext().getNodes().get(0).getNode().getCommand() != null) {
            KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), "");
            usages++;
        }

        while (iterator.hasNext()) {
            usages++;
            final String usage = iterator.next();
            KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), usage);
        }

        if (usages == 0) KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), "");

        return commandNodeStringMap.size();
    }

    private int sendInfo(final CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(
                LangText.getFormatter(true, "command.info", ModConstants.getMinecraftVersion())
                        .formatted(Formatting.GRAY)
                        .append("\n")
                        .append(new LiteralText("GitHub: ").formatted(Formatting.GRAY))
                        .append(Texts.bracketed(new LiteralText("github.com/KiloCraft/KiloEssentials/")
                                .styled(style -> style.withFormatting(Formatting.GOLD).withClickEvent(Texter.Events.onClickOpen("https://github.com/KiloCraft/KiloEssentials/")).setHoverEvent(Texter.Events.onHover("&eClick to open"))))), false);

        return 1;
    }

    public static LiteralText getPermissionError(final String hoverText) {
        final LiteralText literalText = LangText.get(true, "command.exception.permission");
        literalText.styled(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hoverText).formatted(Formatting.YELLOW))));
        return literalText;
    }

    public static void sendPermissionError(final ServerCommandSource source) {
        KiloChat.sendMessageToSource(source, new TextMessage(
                KiloConfig.messages().commands().context().permissionException
                ,true));
    }

    public static SimpleCommandExceptionType getException(final ExceptionMessageNode node, final Object... objects) {
        final String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        return KiloCommands.commandException(
                new LiteralText(objects != null ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    public static SimpleCommandExceptionType getException(final CommandMessageNode node, final Object... objects) {
        final String message = ModConstants.getMessageUtil().fromCommandNode(node);
        return KiloCommands.commandException(
                new LiteralText(objects != null ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    public static SimpleCommandExceptionType commandException(final Text text) {
        return new SimpleCommandExceptionType(text);
    }

    public static SimpleCommandExceptionType getArgException(final ArgExceptionMessageNode node, final Object... objects) {
        final String message = ModConstants.getMessageUtil().fromArgumentExceptionNode(node);
        return KiloCommands.commandException(
                new LiteralText(objects != null ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    public static void updateCommandTreeForEveryone() {
        for (final ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            KiloServer.getServer().getPlayerManager().sendCommandTree(playerEntity);
        }
    }

    @Nullable
    public final IEssentialCommand getEssentialCommand(final String label) {
        IEssentialCommand esscommand = null;

        for (final IEssentialCommand command : this.commands) {

            if (command.getLabel().equalsIgnoreCase(label) || command.getLabel().equalsIgnoreCase("ke_" + label)) {
                esscommand = command;
            }

            if (esscommand == null && command.getAlias() != null) {
                for (final String alias : command.getAlias()) {
                    if (alias.equalsIgnoreCase(label)) {
                        esscommand = command;
                    }
                }
            }

        }

        return esscommand;
    }

    public final void sendUsage(final ServerCommandSource source, final EssentialCommand command) {
        if (!command.hasUsage()) {
            source.sendError(new LiteralText("No Usage!"));
            return;
        }

        StringBuilder builder = new StringBuilder(ModConstants.translation("command.usage", LiteralCommandModified.normalizeName(command.getLabel()))).append(' ');

        for (String arg : command.getUsageArguments()) {
            builder.append(ModConstants.translation("command.usage.arg", arg)).append(' ');
        }

        if (command.getDescriptionId() != null) {
            builder.append('\n').append(ModConstants.translation("command.usage.desc", ModConstants.translation(command.getDescriptionId())));
        }

        if (command.getAlias() != null && command.getAlias().length > 0) {
            builder.append('\n').append(ModConstants.translation("command.usage.aliases")).append(' ');

            for (int i = 0; i < command.getAlias().length; i++) {
                builder.append(ModConstants.translation("command.usage.alias", LiteralCommandModified.normalizeName(command.getAlias()[i])));

                if (i + 1 != command.getAlias().length) {
                    builder.append(ModConstants.translation("command.usage.separator")).append(' ');
                }
            }
        }

        source.sendFeedback(Texter.newText(builder.toString()), false);
    }

    public int execute(@NotNull final ServerCommandSource executor, @NotNull final String command) {
        CommandSourceUser src = KiloServer.getServer().getCommandSourceUser(executor);
        OnCommandExecutionEvent event = new OnCommandExecutionEventImpl(executor, command);
        String cmd = command;

        if (!command.endsWith("--push") && !src.hasPermission(EssentialPermission.IGNORE_COMMAND_EVENTS)) {
            try {
                KiloServer.getServer().triggerEvent(event);
            } catch (Exception e) {
                if (SharedConstants.isDevelopment) {
                    KiloDebugUtils.getLogger().fatal("Expected error while triggering an Event", e);
                }
            }
        } else {
            cmd = command.replace(" --push", "");
        }

        if (event.isCancelled()) {
            return 0;
        }

        if (this.simpleCommandManager.canExecute(cmd)) {
            return this.simpleCommandManager.execute(cmd, executor);
        }

        StringReader reader = new StringReader(cmd);

        if (reader.canRead() && reader.peek() == '/') {
            reader.skip();
        }

        getServer().getMinecraftServer().getProfiler().push(cmd);

        byte var = 0;
        try {
            try {
                return this.dispatcher.execute(reader, executor);
            } catch (final CommandException e) {
                executor.sendError(e.getTextMessage());
                var = (byte) 0;
                return var;
            } catch (final CommandSyntaxException e) {
                final EssentialCommand essentialcommand = (EssentialCommand) this.getEssentialCommand(cmd.replaceFirst("/", "").split(" ")[0]);

                if (e.getRawMessage().getString().startsWith("Unknown or incomplete")) {
                    String literalName = cmd.split(" ")[0].replace("/", "");
                    CommandPermission reqPerm = CommandPermission.getByNode(literalName);

                    if (essentialcommand != null && essentialcommand.hasUsage() && essentialcommand.getRootPermissionPredicate().test(executor)) {
                        this.sendUsage(executor, essentialcommand);
                        return var;
                    }

                    if (this.isCommand(literalName) && reqPerm != null && !KiloCommands.hasPermission(executor, reqPerm)) {
                        KiloCommands.sendPermissionError(executor);
                    } else {
                        KiloChat.sendMessageToSource(executor, new TextMessage(KiloConfig.messages().commands().context().executionException, true));
                    }

                } else {
                    executor.sendError(Texts.toText(e.getRawMessage()));

                    if (e.getInput() != null && e.getCursor() >= 0) {
                        final int cursor = Math.min(e.getInput().length(), e.getCursor());
                        final MutableText text = new LiteralText("").formatted(Formatting.GRAY)
                                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command).formatted(Formatting.YELLOW))));

                        if (cursor > 10) text.append("...");

                        text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                        if (cursor < e.getInput().length()) {
                            final Text errorAtPointMessage = new LiteralText(e.getInput().substring(cursor)).formatted(Formatting.RED, Formatting.UNDERLINE);
                            text.append(errorAtPointMessage);
                        }

                        text.append(new LiteralText("<--[HERE]").formatted(Formatting.RED, Formatting.ITALIC));
                        executor.sendError(text);
                    }
                }

            }
        } catch (final Exception e) {
            final MutableText text = new LiteralText(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            if (SharedConstants.isDevelopment) {
                getLogger().error("Command exception: {}", command, e);
                StackTraceElement[] stackTraceElements = e.getStackTrace();

                for (int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    text.append(Texter.exceptionToText(e, true));
                }
            }

            executor.sendError(new TranslatableText("command.failed").styled(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text))));

            if (SharedConstants.isDevelopment) {
                executor.sendError(new LiteralText(Util.getInnermostMessage(e)));
                getLogger().error("'" + command + "' threw an exception", e);
            }

            return (byte) 0;

        } finally {
            getServer().getMinecraftServer().getProfiler().pop();
        }

        return var;
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return KiloEssentialsImpl.commandDispatcher;
    }

    private boolean isCommand(final String literal) {
        return this.dispatcher.getRoot().getChild(literal) != null;
    }

    public List<IEssentialCommand> getCommands() {
        return this.commands;
    }

    @Deprecated
    public static int SUCCESS() {
        return 1;
    }

}
