package org.kilocraft.essentials.util.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.help.HelpCommand;
import org.kilocraft.essentials.util.commands.help.HelpMeCommand;
import org.kilocraft.essentials.util.commands.inventory.AnvilCommand;
import org.kilocraft.essentials.util.commands.inventory.EnderchestCommand;
import org.kilocraft.essentials.util.commands.inventory.InventoryCommand;
import org.kilocraft.essentials.util.commands.inventory.WorkbenchCommand;
import org.kilocraft.essentials.util.commands.item.ModifyItemCommand;
import org.kilocraft.essentials.util.commands.locate.LocateCommand;
import org.kilocraft.essentials.util.commands.messaging.*;
import org.kilocraft.essentials.util.commands.misc.*;
import org.kilocraft.essentials.util.commands.moderation.*;
import org.kilocraft.essentials.util.commands.play.*;
import org.kilocraft.essentials.util.commands.server.*;
import org.kilocraft.essentials.util.commands.teleport.BackCommand;
import org.kilocraft.essentials.util.commands.teleport.RtpCommand;
import org.kilocraft.essentials.util.commands.teleport.TeleportCommands;
import org.kilocraft.essentials.util.commands.teleport.tpr.*;
import org.kilocraft.essentials.util.commands.user.LastSeenCommand;
import org.kilocraft.essentials.util.commands.user.SilenceCommand;
import org.kilocraft.essentials.util.commands.user.WhoIsCommand;
import org.kilocraft.essentials.util.commands.user.WhoWasCommand;
import org.kilocraft.essentials.util.commands.world.TimeCommand;
import org.kilocraft.essentials.util.settings.SettingCommand;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class KiloCommands {
    private static final List<IEssentialCommand> commands = new ArrayList<>();
    private static final SimpleCommandManager simpleCommandManager = new SimpleCommandManager();
    private static final LiteralCommandNode<ServerCommandSource> rootNode = literal("essentials").executes(KiloCommands::sendInfo).build();
    private static CommandDispatcher<ServerCommandSource> dispatcher;

    public static void registerCommands(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        dispatcher = commandDispatcher;
        registerDefaults();
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm) {
        return Permissions.check(src, perm.getNode(), 2);
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm, final int minOpLevel) {
        return Permissions.check(src, perm.getNode(), minOpLevel);
    }

    public static boolean hasPermission(final ServerCommandSource src, final String cmdPerm, final int minOpLevel) {
        return Permissions.check(src, cmdPerm, minOpLevel);
    }

    private static void registerDefaults() {
        register(new DebugEssentialsCommand());
        register(new LightningCommand());
        register(new MobCapCommand());
        register(new PlayerMobCapCommand());
        register(new ViewDistanceCommand());
        register(new NicknameCommand());
        register(new SayAsCommand());
        register(new SudoCommand());
        register(new ModifyItemCommand());
        register(new WorkbenchCommand());
        register(new AnvilCommand());
        register(new SignEditCommand());
        register(new HatCommand());
        register(new VersionCommand());
        register(new ReloadCommand());
        register(new SlimeChunkCommand());
        register(new TextFormattingCommand());
        register(new CommandFormattingCommand());
        register(new GamemodeCommand());
        register(new RtpCommand());
        register(new BroadcastCommand());
        register(new HealCommand());
        register(new FeedCommand());
        register(new TimeCommand());
        register(new FlyCommand());
        register(new InvulnerableCommand());
        register(new FormatPreviewCommand());
        register(new PingCommand());
        register(new ClearChatCommand());
        register(new EnderchestCommand());
        register(new EntitiesCommand());
        register(new StatusCommand());
        register(new StaffMessageCommand());
        register(new BuilderMsgCommand());
        register(new SocialSpyCommand());
        register(new CommandSpyCommand());
        register(new BackCommand());
        register(new ModsCommand());
        register(new TpsCommand());
        register(new LocateCommand());
        register(new MessageCommand());
        register(new DoNotDisturbCommand());
        register(new VanishCommand());
        register(new IgnoreCommand());
        register(new IgnoreListCommand());
        register(new ReplyCommand());
        register(new RealNameCommand());
        register(new IpInfoCommand());
        register(new HelpCommand());
        register(new WhoIsCommand());
        register(new WhoWasCommand());
        register(new PlaytimeCommand());
        register(new HelpMeCommand());
        register(new PlaytimeTopCommand());
        register(new SilenceCommand());
        register(new TpaCommand());
        register(new TpaHereCommand());
        register(new TpAcceptCommand());
        register(new TpDenyCommand());
        register(new TpCancelCommand());
        register(new LastSeenCommand());
        register(new InventoryCommand());
        register(new CalculateCommand());
        register(new HugCommand());
        register(new GlowCommand());
        register(new BanCommand());
        register(new TempBanCommand());
        register(new BanIpCommand());
        register(new TempBanIpCommand());
        register(new MuteCommand());
        register(new TempMuteCommand());
        register(new UnBanCommand());
        register(new UnBanIpCommand());
        register(new UnMuteCommand());
        register(new ToggleChatCommand());
        register(new SettingCommand());

        dispatcher.getRoot().addChild(rootNode);

        StopCommand.register(dispatcher);
        RestartCommand.register(dispatcher);
        TeleportCommands.register(dispatcher);
    }

    public static <C extends IEssentialCommand> void register(@NotNull final C c) {
        EssentialCommand command = (EssentialCommand) c;
        command.register(dispatcher);
        commands.add(command);

        if (command.getForkType() == IEssentialCommand.ForkType.DEFAULT || command.getForkType() == IEssentialCommand.ForkType.SUB_ONLY) {
            rootNode.addChild(command.getArgumentBuilder().build());
            rootNode.addChild(command.getCommandNode());
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

                    dispatcher.register(argumentBuilder);
                }
            }

            dispatcher.getRoot().addChild(command.getCommandNode());
            dispatcher.register(command.getArgumentBuilder());
        }
    }

    private static int sendInfo(final CommandContext<ServerCommandSource> ctx) {
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

    public static SimpleCommandExceptionType getException(final String langErrorKey) {
        return getException(langErrorKey, new Object[0]);
    }

    public static SimpleCommandExceptionType getException(final String langErrorKey, final Object... objects) {
        final String message = ModConstants.translation(langErrorKey, objects);
        return KiloCommands.commandException(
                new LiteralText(objects != null ? String.format(message, objects) : message).formatted(Formatting.RED));
    }

    private static SimpleCommandExceptionType commandException(final Text text) {
        return new SimpleCommandExceptionType(text);
    }

    public static void updateGlobalCommandTree() {
        for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
            KiloEssentials.getMinecraftServer().getPlayerManager().sendCommandTree(player);
        }
    }

    @Nullable
    public static IEssentialCommand getEssentialCommand(final String label) {
        IEssentialCommand esscommand = null;

        for (final IEssentialCommand command : commands) {
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
    public static void sendUsage(final ServerCommandSource source, final EssentialCommand command) {
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

    public static int execute(@NotNull final ServerCommandSource executor, @NotNull final String command) {
        CommandSourceUser src = CommandSourceServerUser.of(executor);
        onCommand(executor, command);

        if (simpleCommandManager.canExecute(command)) {
            return simpleCommandManager.execute(command, executor);
        }

        StringReader reader = new StringReader(command);

        if (reader.canRead() && reader.peek() == '/') {
            reader.skip();
        }

        KiloEssentials.getMinecraftServer().getProfiler().push(command);

        byte var = 0;
        try {
            try {
                return dispatcher.execute(reader, executor);
            } catch (final CommandException e) {
                executor.sendError(e.getTextMessage());
                var = (byte) 0;
                return var;
            } catch (final CommandSyntaxException e) {
                final EssentialCommand essentialcommand = (EssentialCommand) getEssentialCommand(command.replaceFirst("/", "").split(" ")[0]);

                if (e.getRawMessage().getString().startsWith("Unknown or incomplete")) {
                    String literalName = command.split(" ")[0].replace("/", "");
                    CommandPermission reqPerm = CommandPermission.getByNode(literalName);

                    if (essentialcommand != null && essentialcommand.hasUsage() && essentialcommand.getRootPermissionPredicate().test(executor)) {
                        sendUsage(executor, essentialcommand);
                        return var;
                    }

                    if (isCommand(literalName) && reqPerm != null && !KiloCommands.hasPermission(executor, reqPerm)) {
                        CommandSourceServerUser.of(executor).sendMessage(KiloConfig.messages().commands().context().permissionException);
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
            KiloEssentials.getLogger().error("'" + command + "' threw an exception", e);

            String exception = ExceptionUtils.getStackTrace(e);
            executor.sendError(new TranslatableText("command.failed")
                    .styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentText.toText(exception)))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, exception))
                    )
            );

            return (byte) 0;

        } finally {
            KiloEssentials.getMinecraftServer().getProfiler().pop();
        }

        return var;
    }

    private static void onCommand(@NotNull final ServerCommandSource executor, @NotNull String command) {
        if (CommandUtils.isPlayer(executor)) {
            command = command.startsWith("/") ? command.substring(1) : command;

            boolean isIgnored = false;
            for (String cmd : KiloConfig.main().ignoredCommandsForLogging) {
                if (command.startsWith(cmd)) {
                    isIgnored = true;
                    break;
                }
            }

            if (!isIgnored) {
                ServerChat.sendCommandSpy(executor, command);

                if (KiloConfig.main().server().logCommands) {
                    KiloEssentials.getLogger().info("[" + executor.getName() + "]: " + command);
                }
            }
        }
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return dispatcher;
    }

    private static boolean isCommand(final String literal) {
        return dispatcher.getRoot().getChild(literal) != null;
    }

    public static List<IEssentialCommand> getCommands() {
        return commands;
    }

}
