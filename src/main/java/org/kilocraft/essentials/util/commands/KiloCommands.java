package org.kilocraft.essentials.util.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.messaging.HelpMeCommand;
import org.kilocraft.essentials.util.commands.inventory.AnvilCommand;
import org.kilocraft.essentials.util.commands.inventory.EnderchestCommand;
import org.kilocraft.essentials.util.commands.inventory.InventoryCommand;
import org.kilocraft.essentials.util.commands.inventory.WorkbenchCommand;
import org.kilocraft.essentials.util.commands.item.ModifyItemCommand;
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
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.SettingCommand;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class KiloCommands {
    private static final List<IEssentialCommand> commands = new ArrayList<>();
    private static final LiteralCommandNode<ServerCommandSource> rootNode = literal("essentials").executes(KiloCommands::sendInfo).build();
    private static CommandDispatcher<ServerCommandSource> dispatcher;

    public static void registerCommands(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        dispatcher = commandDispatcher;
        registerDefaults();
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm) {
        return KiloEssentials.hasPermissionNode(src, perm);
    }

    public static boolean hasPermission(final ServerCommandSource src, final CommandPermission perm, final int minOpLevel) {
        return KiloEssentials.hasPermissionNode(src, perm, minOpLevel);
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
        register(new MessageCommand());
        register(new DoNotDisturbCommand());
        register(new VanishCommand());
        register(new ReplyCommand());
        register(new RealNameCommand());
        register(new IpInfoCommand());
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
                StringText.of("command.info", ModConstants.getMinecraftVersion())
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

    public static void onCommand(@NotNull final ServerCommandSource executor, final String command) {
        if (CommandUtils.isPlayer(executor)) {

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

    public static boolean isCommandDisabled(ServerCommandSource src, String command) {
        try {
            if (KiloEssentials.hasPermissionNode(src, EssentialPermission.COMMANDS_BYPASS_WORLD)) return false;
            final DimensionType dimensionType = src.getPlayer().getWorld().getDimension();
            final Identifier identifier = RegistryUtils.toIdentifier(dimensionType);
            final List<String> disabledCommands = KiloConfig.main().world().disabledCommands.get(identifier.toString());
            if (disabledCommands != null) {
                for (String disabledCommand : disabledCommands) {
                    if (command.startsWith(disabledCommand)) {
                        src.sendError(StringText.of("general.dimension_command_disabled", command, identifier.getPath()));
                        return true;
                    }
                }
            }
        } catch (CommandSyntaxException noPlayer) {
            return false;
        }
        return false;
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return dispatcher;
    }

    public static List<IEssentialCommand> getCommands() {
        return commands;
    }

}
