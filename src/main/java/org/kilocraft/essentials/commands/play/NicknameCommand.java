package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NicknameCommand {
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (s) -> KiloCommands.hasPermission(s, "nick.self", 2);
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER = (s) -> KiloCommands.hasPermission(s, "nick.other", 2);
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_EITHER = (s) -> PERMISSION_CHECK_OTHER.test(s) || PERMISSION_CHECK_SELF.test(s);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RootCommandNode<ServerCommandSource> rootCommandNode = dispatcher.getRoot();

        LiteralCommandNode<ServerCommandSource> nickRootCommand = dispatcher.register(literal("nick").requires(PERMISSION_CHECK_EITHER));

        LiteralCommandNode<ServerCommandSource> setSelf = literal("set").requires(PERMISSION_CHECK_EITHER).build();
        LiteralCommandNode<ServerCommandSource> setOther = literal("set").requires(PERMISSION_CHECK_OTHER).build();
        ArgumentCommandNode<ServerCommandSource, EntitySelector> target = argument("target", player()).requires(PERMISSION_CHECK_OTHER).suggests(ArgumentSuggestions::allPlayers).build();

        ArgumentCommandNode<ServerCommandSource, String> nicknameSelf = argument("nickname", greedyString())
                .suggests(NicknameCommand::setSelfSuggestions).executes(NicknameCommand::setSelf).build();
        ArgumentCommandNode<ServerCommandSource, String> nicknameOther = argument("nickname", greedyString())
                .suggests(NicknameCommand::setOthersSuggestions).executes(NicknameCommand::setOther).build();

        LiteralCommandNode<ServerCommandSource> resetSelf = literal("reset").requires(PERMISSION_CHECK_SELF).executes(NicknameCommand::resetSelf).build();
        LiteralCommandNode<ServerCommandSource> resetOther = literal("reset").requires(PERMISSION_CHECK_OTHER).executes(NicknameCommand::resetOther).build();

        LiteralCommandNode<ServerCommandSource> other = literal("other").requires(PERMISSION_CHECK_OTHER).build();

        setOther.addChild(nicknameOther);

        target.addChild(setOther);
        target.addChild(resetOther);

        other.addChild(target);

        nickRootCommand.addChild(other);

        setSelf.addChild(nicknameSelf);

        nickRootCommand.addChild(setSelf);
        nickRootCommand.addChild(resetSelf);

        rootCommandNode.addChild(nickRootCommand);

        LiteralCommandNode<ServerCommandSource> redirect_Nickname = literal("nickname").requires(PERMISSION_CHECK_EITHER).redirect(nickRootCommand).build();
        rootCommandNode.addChild(redirect_Nickname);
    }

    private static int setSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity self = source.getPlayer();

        Object unchecked = KiloConfig.getProvider().getMain().getIntegerSafely("nickname-max-length", 16);

        if (unchecked == null) {
            throw new SimpleCommandExceptionType(new LiteralText("Please contact the admins as this has not been configured correctly")).create();
        }

        int maxLength = (int) unchecked;
        String nickname = getString(ctx, "nickname");

        if (nickname.length() > maxLength)
            throw KiloCommands.getException(ExceptionMessageNode.NICKNAME_NOT_ACCEPTABLE, maxLength).create();

        String formattedNickname = "";
        if (hasPermissionOrOp(ctx.getSource(), KiloCommands.getCommandPermission("nick.formatting"), 2)) {
        	formattedNickname = TextFormat.translateAlternateColorCodes('&', nickname);
        } else {
        	formattedNickname = TextFormat.removeAlternateColorCodes('&', nickname);
        }

        User user = KiloServer.getServer().getUserManager().getOnline(self);
        user.setNickname(nickname);
        self.setCustomName(new LiteralText(formattedNickname));

        KiloChat.sendLangMessageTo(source, "template.#1", "nickname", formattedNickname, source.getName());
        return 1;
    }

    private static int setOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = getPlayer(ctx, "target");
        String nickname = getString(ctx, "nickname");

        Object unchecked = KiloConfig.getProvider().getMain().getValue("nickname-max-length");

        if (unchecked == null) {
            throw new SimpleCommandExceptionType(new LiteralText("Please contact the admins as this has not been configured correctly")).create();
        }

        int maxLength = (int) unchecked;

        if (nickname.length() > maxLength)
            throw KiloCommands.getException(ExceptionMessageNode.NICKNAME_NOT_ACCEPTABLE, maxLength).create();

        String formattedNickname = TextFormat.translateAlternateColorCodes('&', nickname);

        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.setNickname(nickname);
        player.setCustomName(new LiteralText(formattedNickname));

        KiloChat.sendLangMessageTo(source, "template.#1", "nickname", formattedNickname, player.getName().asString());
        if (!CommandHelper.areTheSame(source, player))
            KiloChat.sendLangMessageTo(player, "template.#1.announce", source.getName(), "nickname", formattedNickname);

        return 1;
    }

    private static int resetSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.clearNickname();
        // This is an Optional.ofNullable, so the DataTracker will just reset the name without any other magic since TrackedData is always and automatically synchronized with the client.
        player.setCustomName(null);
        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "nickname", "&ddefault", ctx.getSource().getName());
        return 1;
    }

    private static int resetOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = getPlayer(ctx, "target");
        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.clearNickname();
        // This is an Optional.ofNullable, so the DataTracker will just reset the name without any other magic since TrackedData is always and automatically synchronized with the client.
        player.setCustomName(null);

        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "nickname", "&ddefault", ctx.getSource().getName());
        return 1;
    }

    private static CompletableFuture<Suggestions> setSelfSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        User user = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer());
        List<String> strings = new ArrayList<>();
        if (user.hasNickname())
            strings.add(user.getNickname().get());

        return CommandSource.suggestMatching(strings, builder);
    }

    private static CompletableFuture<Suggestions> setOthersSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        User user = KiloServer.getServer().getUserManager().getOnline(getPlayer(context, "target"));
        List<String> strings = new ArrayList<>();
        if (user.hasNickname())
            strings.add(user.getNickname().get());

        return CommandSource.suggestMatching(strings, builder);
    }

}
