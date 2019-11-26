package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.greedycommand.GreedyParser;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.*;

public class NicknameCommand {
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (s) -> hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick"), 2);
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER = (s) -> hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.other"), 3);
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_EITHER = (s) -> PERMISSION_CHECK_OTHER.test(s) || PERMISSION_CHECK_SELF.test(s);
    private static final SimpleCommandExceptionType NICKNAME_TOO_LONG = new SimpleCommandExceptionType(new LiteralText("Nickname is too long"));


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RootCommandNode<ServerCommandSource> rootCommandNode = dispatcher.getRoot();

        LiteralCommandNode<ServerCommandSource> nickRootCommand = dispatcher.register(literal("nick").requires(PERMISSION_CHECK_EITHER));

        LiteralCommandNode<ServerCommandSource> setSelf = literal("set").requires(PERMISSION_CHECK_EITHER).build();
        LiteralCommandNode<ServerCommandSource> setOther = literal("set").requires(PERMISSION_CHECK_OTHER).build();
        ArgumentCommandNode<ServerCommandSource, EntitySelector> target = argument("target", player()).requires(PERMISSION_CHECK_OTHER).build();

        ArgumentCommandNode<ServerCommandSource, String> nicknameSelf = argument("nickname", string()).executes(NicknameCommand::setSelf).build();
        ArgumentCommandNode<ServerCommandSource, String> nicknameOther = argument("nickname", string()).executes(NicknameCommand::setOther).build();

        LiteralCommandNode<ServerCommandSource> resetSelf = literal("reset").requires(PERMISSION_CHECK_SELF).executes(NicknameCommand::resetSelf).build();
        LiteralCommandNode<ServerCommandSource> clearSelf = literal("clear").requires(PERMISSION_CHECK_SELF).executes(NicknameCommand::resetSelf).build();
        LiteralCommandNode<ServerCommandSource> resetOther = literal("reset").requires(PERMISSION_CHECK_OTHER).executes(NicknameCommand::resetOther).build();
        LiteralCommandNode<ServerCommandSource> clearOther = literal("clear").requires(PERMISSION_CHECK_OTHER).executes(NicknameCommand::resetOther).build();

        LiteralCommandNode<ServerCommandSource> other = literal("other").requires(PERMISSION_CHECK_OTHER).build();

        setOther.addChild(nicknameOther);

        target.addChild(setOther);
        target.addChild(resetOther);
        target.addChild(clearOther);

        other.addChild(target);

        nickRootCommand.addChild(other);

        setSelf.addChild(nicknameSelf);

        nickRootCommand.addChild(setSelf);
        nickRootCommand.addChild(resetSelf);
        nickRootCommand.addChild(clearSelf);

        rootCommandNode.addChild(nickRootCommand);

        LiteralCommandNode<ServerCommandSource> redirect_Nickname = literal("nickname").requires(PERMISSION_CHECK_EITHER).redirect(nickRootCommand).build();
        rootCommandNode.addChild(redirect_Nickname);
    }

    private static int setSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity self = source.getPlayer();

        Object unchecked = KiloConfig.getProvider().getMain().getValue("nickname-max-length");

        if (unchecked == null) {
            throw new SimpleCommandExceptionType(new LiteralText("Please contact the admins as this has not been configured correctly")).create();
        }

        int maxLength = (int) unchecked;
        String nickname = getString(ctx, "nickname");

        if(nickname.length() > maxLength) {
            throw NICKNAME_TOO_LONG.create();
        }

        String formattedNickname = "";
        if (hasPermissionOrOp(ctx.getSource(), KiloCommands.getCommandPermission("nick.formatting"), 2)) {
        	formattedNickname = TextFormat.translateAlternateColorCodes('&', nickname);
        } else {
        	formattedNickname = TextFormat.removeAlternateColorCodes('&', nickname);
        }

        User user = KiloServer.getServer().getUserManager().getOnline(self);
        user.setNickname(nickname);
        self.setCustomName(new LiteralText(formattedNickname));

        source.sendFeedback(LangText.getFormatter(true, "command.nick.set.self", formattedNickname), false);
        return 1;
    }

    private static int setOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = getPlayer(ctx, "target");
        String nickname = getString(ctx, "nickname");

        String formattedNickname = TextFormat.translateAlternateColorCodes('&', nickname);

        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.setNickname(nickname);
        player.setCustomName(new LiteralText(formattedNickname));

        source.sendFeedback(LangText.getFormatter(true, "command.nick.set.other", formattedNickname), false);
        player.sendMessage(LangText.getFormatter(true, "command.nick.set.byother", formattedNickname));
        return 1;
    }

    private static int resetSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.clearNickname();
        // This is an Optional.ofNullable, so the DataTracker will just reset the name without any other magic since TrackedData is always and automatically synchronized with the client.
        player.setCustomName(null);
        ctx.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.reset"),
                false);
        return 1;
    }

    private static int resetOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = getPlayer(ctx, "target");
        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.clearNickname();
        // This is an Optional.ofNullable, so the DataTracker will just reset the name without any other magic since TrackedData is always and automatically synchronized with the client.
        player.setCustomName(null);

        source.sendFeedback(LangText.getFormatter(true, "command.nick.reset"),
                false);
        return 1;
    }


    private static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> suggestions = new ArrayList<String>(){{
            Optional<String> nickname = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer()).getNickname();
            if(nickname.isPresent())
                add(KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer()).getNickname().get());
        }};

        if (hasPermissionOrOp(context.getSource(), getCommandPermission("nick.others"), 2)) {
            KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> suggestions.add(player.getEntityName()));
        }

        return CommandSource.suggestMatching(suggestions, builder);
    }
}
