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
import net.minecraft.command.CommandSource;
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
import org.kilocraft.essentials.api.feature.ConfigurableFeatures;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.commands.LiteralCommandModified;
import org.kilocraft.essentials.commands.help.HelpCommand;
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
import org.kilocraft.essentials.commands.moderation.*;
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
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.extensions.customcommands.CustomCommands;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.extensions.playtimecommands.PlaytimeCommands;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.ServerWarpManager;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;
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
    private final List<IEssentialCommand> commands;
    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private SimpleCommandManager simpleCommandManager;
    private static LiteralCommandNode<ServerCommandSource> rootNode;
    private static KiloCommands instance;


    public KiloCommands() {
        KiloCommands.instance = this;
        this.dispatcher = KiloEssentialsImpl.commandDispatcher;
        this.commands = new ArrayList<>();
        KiloCommands.rootNode = literal("essentials").executes(this::sendInfo).build();
        this.simpleCommandManager = new SimpleCommandManager();
        registerDefaults();
        registerFeatures();
    }

    public void registerFeatures() {
        ConfigurableFeatures features = new ConfigurableFeatures();
        features.register(new UserHomeHandler(), "playerHomes");
        features.register(new ServerWarpManager(), "serverWideWarps");
        features.register(new PlayerWarpsManager(), "playerWarps");
        features.register(new SeatManager(), "betterChairs");
        features.register(new CustomCommands(), "customCommands");
        features.register(new ParticleAnimationManager(), "magicalParticles");
        features.register(new DiscordCommand(), "discordCommand");
        features.register(new VoteCommand(), "voteCommand");
        features.register(new PlaytimeCommands(), "playtimeCommands");
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm) {
        return KiloEssentials.getInstance().getPermissionUtil().hasPermission(src, perm.getNode(), 2);
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm, final int minOpLevel) {
        return KiloEssentials.getInstance().getPermissionUtil().hasPermission(src, perm.getNode(), minOpLevel);
    }

    public static boolean hasPermission(final ServerCommandSource src, final String cmdPerm, final int minOpLevel) {
        return KiloEssentials.getInstance().getPermissionUtil().hasPermission(src, cmdPerm, minOpLevel);
    }

    private void registerDefaults() {
        this.register(new DebugEssentialsCommand());
        this.register(new LightningCommand());
        this.register(new NicknameCommand());
        this.register(new SayAsCommand());
        this.register(new SudoCommand());
        this.register(new ItemCommand());
        this.register(new WorkbenchCommand());
        this.register(new AnvilCommand());
        this.register(new SignEditCommand());
        this.register(new HatCommand());
        this.register(new VersionCommand());
        this.register(new ReloadCommand());
        this.register(new TextFormattingCommand());
        this.register(new CommandFormattingCommand());
        this.register(new GamemodeCommand());
        this.register(new RtpCommand());
        this.register(new BroadcastCommand());
        this.register(new UsageCommand());
        this.register(new HealCommand());
        this.register(new FeedCommand());
        this.register(new TimeCommand());
        this.register(new FlyCommand());
        this.register(new InvulnerableCommand());
        this.register(new FormatPreviewCommand());
        this.register(new PingCommand());
        this.register(new ClearChatCommand());
        this.register(new EnderchestCommand());
        this.register(new StatusCommand());
        this.register(new StaffMessageCommand());
        this.register(new BuilderMsgCommand());
        this.register(new SocialSpyCommand());
        this.register(new CommandSpyCommand());
        this.register(new BackCommand());
        this.register(new ModsCommand());
        this.register(new TpsCommand());
        this.register(new LocateCommand());
        this.register(new MessageCommand());
        this.register(new DoNotDisturbCommand());
        this.register(new IgnoreCommand());
        this.register(new IgnoreListCommand());
        this.register(new ReplyCommand());
        this.register(new RealNameCommand());
        this.register(new IpInfoCommand());
        this.register(new HelpCommand());
        this.register(new WhoIsCommand());
        this.register(new WhoWasCommand());
        this.register(new PlaytimeCommand());
        this.register(new HelpMeCommand());
        this.register(new PlaytimeTopCommand());
        this.register(new SilenceCommand());
        this.register(new TpaCommand());
        this.register(new TpaHereCommand());
        this.register(new TpAcceptCommand());
        this.register(new TpDenyCommand());
        this.register(new TpCancelCommand());
        this.register(new LastSeenCommand());
        this.register(new InventoryCommand());
        this.register(new CalculateCommand());
        this.register(new HugCommand());
        this.register(new GlowCommand());
        this.register(new BanCommand());
        this.register(new TempBanCommand());
        this.register(new BanIpCommand());
        this.register(new TempBanIpCommand());
        this.register(new MuteCommand());
        this.register(new TempMuteCommand());
        this.register(new UnBanCommand());
        this.register(new UnBanIpCommand());
        this.register(new UnMuteCommand());
        this.register(new ToggleChatCommand());

        this.dispatcher.getRoot().addChild(KiloCommands.rootNode);

        StopCommand.register(this.dispatcher);
        RestartCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);
        TeleportCommands.register(this.dispatcher);
    }

    public <C extends IEssentialCommand> void register(@NotNull final C c) {
        EssentialCommand command = (EssentialCommand) c;
        command.register(this.dispatcher);
        this.commands.add(command);

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

    @Deprecated
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

    @Deprecated
    public static void executeUsageFor(final String langKey, final ServerCommandSource source) {
        final String fromLang = ModConstants.getStrings().getProperty(langKey);
        if (fromLang != null)
            KiloServer.getServer().getCommandSourceUser(source).sendMessage("<gold>Command usage:\n" + fromLang);
        else
            KiloServer.getServer().getCommandSourceUser(source).sendMessage("general.usage.help");
    }

    @Deprecated
    public static int executeSmartUsage(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String command = ctx.getInput().replace("/", "");
        final ParseResults<ServerCommandSource> parseResults = KiloCommands.getDispatcher().parse(command, ctx.getSource());
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(ctx.getSource());
        if (parseResults.getContext().getNodes().isEmpty())
            throw KiloCommands.getException(ExceptionMessageNode.UNKNOWN_COMMAND_EXCEPTION).create();

        final Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = KiloCommands.getDispatcher().getSmartUsage(((ParsedCommandNode) Iterables.getLast(parseResults.getContext().getNodes())).getNode(), ctx.getSource());
        final Iterator<String> iterator = commandNodeStringMap.values().iterator();

        user.sendLangMessage( "command.usage.firstRow", command);
        user.sendLangMessage( "command.usage.commandRow", command, "");

        while (iterator.hasNext()) {
            if (iterator.next().equals("/" + command)) continue;
            user.sendLangMessage( "command.usage.commandRow", command, iterator.next());
        }

        return 1;
    }

    @Deprecated
    public static void executeSmartUsageFor(final String command, final ServerCommandSource source) throws CommandSyntaxException {
        final ParseResults<ServerCommandSource> parseResults = KiloCommands.getDispatcher().parse(command, source);
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(source);
        if (parseResults.getContext().getNodes().isEmpty())
            throw KiloCommands.getException(ExceptionMessageNode.UNKNOWN_COMMAND_EXCEPTION).create();

        final Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = KiloCommands.getDispatcher().getSmartUsage(((ParsedCommandNode) Iterables.getLast(parseResults.getContext().getNodes())).getNode(), source);
        final Iterator<String> iterator = commandNodeStringMap.values().iterator();

        int usages = 0;
        user.sendLangMessage("command.usage.firstRow", parseResults.getReader().getString());
        if (parseResults.getContext().getNodes().get(0).getNode().getCommand() != null) {
            user.sendLangMessage("command.usage.commandRow", parseResults.getReader().getString(), "");
            usages++;
        }

        while (iterator.hasNext()) {
            usages++;
            final String usage = iterator.next();
            user.sendLangMessage("command.usage.commandRow", parseResults.getReader().getString(), usage);
        }

        if (usages == 0)
            user.sendLangMessage("command.usage.commandRow", parseResults.getReader().getString(), "");

        commandNodeStringMap.size();
    }

    private int sendInfo(final CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(
                StringText.of(true, "command.info", ModConstants.getMinecraftVersion())
                        .formatted(Formatting.GRAY)
                        .append("\n")
                        .append(new LiteralText("GitHub: ").formatted(Formatting.GRAY))
                        .append(Texts.bracketed(new LiteralText("github.com/KiloCraft/KiloEssentials/")
                                .styled(style -> style.withFormatting(Formatting.GOLD)
                                        .withClickEvent(Texter.Events.onClickOpen("https://github.com/KiloCraft/KiloEssentials/"))
                                        .withHoverEvent(Texter.Events.onHover("&eClick to open"))
                                )
                        )), false);

        return 1;
    }

    @Deprecated
    public static LiteralText getPermissionError(final String hoverText) {
        final LiteralText literalText = StringText.of(true, "command.exception.permission");
        literalText.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hoverText).formatted(Formatting.YELLOW))));
        return literalText;
    }

    @Deprecated
    public static void sendPermissionError(final ServerCommandSource source) {
        KiloServer.getServer().getCommandSourceUser(source).sendMessage(KiloConfig.messages().commands().context().permissionException);
    }

    public static SimpleCommandExceptionType getException(final ExceptionMessageNode node, final Object... objects) {
        final String message = ModConstants.getMessageUtil().fromExceptionNode(node);
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

    public static void updateGlobalCommandTree() {
        for (ServerPlayerEntity player : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            KiloServer.getServer().getPlayerManager().sendCommandTree(player);
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
                        break;
                    }
                }
            }

        }

        return esscommand;
    }

    @Deprecated
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

        source.sendFeedback(ComponentText.toText(builder.toString()), false);
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
                        src.sendMessage(KiloConfig.messages().commands().context().executionException);
                    }

                } else {
                    src.sendError(e.getRawMessage().getString());

                    if (e.getInput() != null && e.getCursor() >= 0) {
                        final int cursor = Math.min(e.getInput().length(), e.getCursor());
                        final MutableText text = new LiteralText("").formatted(Formatting.GRAY)
                                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command).formatted(Formatting.YELLOW))));

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
            final StringBuilder builder = new StringBuilder(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            if (SharedConstants.isDevelopment) {
                getLogger().error("Command exception: {}", command, e);
                StackTraceElement[] stackTraceElements = e.getStackTrace();

                for (int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    builder.append(Texter.exceptionToString(e, true));
                }
            }

            executor.sendError(new TranslatableText("command.failed").styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentText.toText(builder.toString())))));

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

    public static KiloCommands getInstance() {
        if (KiloCommands.instance != null) {
            return KiloCommands.instance;
        }
        throw new RuntimeException("Its too early to get a static instance of KiloCommands!");
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
