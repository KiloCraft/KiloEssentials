package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.RtpSpecsConfigSection;
import org.kilocraft.essentials.mixin.accessor.SpreadPlayerCommandInvoker;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.SimpleProcess;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class RtpCommand extends EssentialCommand {
    private static final SimpleProcess<Void> PROCESS = new SimpleProcess<>("rtp_process");
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_SELF);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHERS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_IGNORE_LIMIT = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_BYPASS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER_DIMENSIONS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERDIMENSIONS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_MANAGE = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_MANAGE);
    private static final Preference<Integer> RTP_LEFT = Preferences.RANDOM_TELEPORTS_LEFT;
    private static final String ACTION_MSG = ModConstants.translation("command.rtp.round_try");

    public RtpCommand() {
        super("rtp", PERMISSION_CHECK_SELF, new String[]{"wilderness", "wild"});
    }

    static void teleport(ServerCommandSource src, ServerPlayerEntity target, Logger logger) throws CommandSyntaxException {
        OnlineUser targetUser = KiloServer.getServer().getOnlineUser(target.getUuid());
        RtpSpecsConfigSection cfg = KiloConfig.main().rtpSpecs();
        //UserUtils.Process.add(targetUser, PROCESS);
        if (targetUser.getPreference(RTP_LEFT) < 0) {
            targetUser.getPreferences().set(RTP_LEFT, 0);
        }

        //Check if the player has any rtps left or permission to ignore the limit
        if (CommandUtils.areTheSame(src, target) && targetUser.getPreference(RTP_LEFT) <= 0 && !PERMISSION_CHECK_IGNORE_LIMIT.test(src)) {
            targetUser.sendMessage(KiloConfig.messages().commands().rtp().empty);
            return;
        }

        //Check if the target is in the correct dimension or has permission to perform the command in other dimensions
        if (RegistryUtils.dimensionTypeToRegistryKey(target.getServerWorld().getDimension()) != World.OVERWORLD && !PERMISSION_CHECK_OTHER_DIMENSIONS.test(src)) {
            targetUser.sendMessage(KiloConfig.messages().commands().rtp().dimensionException);
            return;
        }
        if (!cfg.broadcastMessage.isEmpty()) {
            KiloChat.broadCast(String.format(cfg.broadcastMessage, targetUser.getFormattedDisplayName()));
        }
        ServerCommandSource source = new ServerCommandSource(new CommandOutput() {
            public void sendSystemMessage(Text text, UUID uUID) { }
            public boolean shouldReceiveFeedback() { return false; }
            public boolean shouldTrackOutput() { return false; }
            public boolean shouldBroadcastConsoleToOps() { return false; }
        }, src.getPosition(), src.getRotation(), src.getWorld(), 4, src.getName(), src.getDisplayName(), src.getMinecraftServer(), src.getEntity());
        SpreadPlayerCommandInvoker.execute(source, new Vec2f(0, 0), cfg.min, cfg.max, src.getWorld().getTopY(), false, Collections.singleton(target));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> addArgument = literal("add")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                                argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeAdd)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                                argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeSet)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                                argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeRemove)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> sendArgument = literal("send")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(this::executeOthers)
                );

        LiteralArgumentBuilder<ServerCommandSource> checkArgument = literal("check")
                .executes(this::executeLeft)
                .then(argument("target", EntityArgumentType.player())
                        .requires(PERMISSION_CHECK_OTHERS)
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(this::executeGet)
                );

        LiteralArgumentBuilder<ServerCommandSource> performArgument = literal("perform")
                .executes(this::executePerform);

        this.commandNode.addChild(addArgument.build());
        this.commandNode.addChild(setArgument.build());
        this.commandNode.addChild(removeArgument.build());
        this.commandNode.addChild(sendArgument.build());
        this.commandNode.addChild(sendArgument.build());
        this.commandNode.addChild(checkArgument.build());
        this.commandNode.addChild(performArgument.build());
        this.argumentBuilder.executes(this::executeSelf);
    }

    private int executeLeft(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(ctx.getSource().getPlayer());
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("command.rtp.get", user.getDisplayName(), user.getPreference(RTP_LEFT));

        return user.getPreference(RTP_LEFT);
    }

    private int executeAdd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        int amountToAdd = getInteger(ctx, "amount");
        user.getPreferences().set(RTP_LEFT, user.getPreference(RTP_LEFT) + amountToAdd);
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("template.#1", "RTPs left", user.getPreference(RTP_LEFT), user.getDisplayName());

        return user.getPreference(RTP_LEFT);
    }

    private int executeSet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        int amountToSet = getInteger(ctx, "amount");
        user.getPreferences().set(RTP_LEFT, amountToSet);
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("template.#1", "RTPs left", user.getPreference(RTP_LEFT), user.getDisplayName());

        return user.getPreference(RTP_LEFT);
    }

    private int executeGet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("command.rtp.get", user.getDisplayName(), user.getPreference(RTP_LEFT));

        return user.getPreference(RTP_LEFT);
    }

    private int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        int amountToRemove = getInteger(ctx, "amount");

        if ((user.getPreference(RTP_LEFT) - amountToRemove) < 0)
            throw KiloCommands.getArgException(ArgExceptionMessageNode.NO_NEGATIVE_VALUES).create();

        user.getPreferences().set(RTP_LEFT, user.getPreference(RTP_LEFT) - amountToRemove);
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("template.#1", "RTPs left", user.getPreference(RTP_LEFT), user.getDisplayName());

        return user.getPreference(RTP_LEFT);
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Text text = Texter.confirmationMessage(
                "command.rtp.confirm",
                Texter.getButton("&8[&aClick Here to perform&8]", "/rtp perform", Texter.newText("&dConfirm"))
        );
        this.getOnlineUser(ctx).sendMessage(text);
        return SUCCESS;
    }

    private int executePerform(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);

        if (UserUtils.Process.isIn(user, PROCESS.getId())) {
            user.sendLangError("command.rtp.in_process");
            return FAILED;
        }

        return execute(ctx.getSource(), ctx.getSource().getPlayer());
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser target = this.getOnlineUser(ctx);

        if (UserUtils.Process.isIn(target, PROCESS.getId())) {
            target.sendLangError("command.rtp.in_process");
            return FAILED;
        }

        return execute(ctx.getSource(), target.asPlayer());
    }

    private int execute(ServerCommandSource source, ServerPlayerEntity target) throws CommandSyntaxException {
        KiloServer.getServer().getOnlineUser(target).sendMessage(messages.commands().rtp().start);
        RtpCommand.teleport(source, target, logger);
        return SUCCESS;
    }
}