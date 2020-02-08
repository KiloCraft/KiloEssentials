package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.PlayerDataModifier;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class NicknameCommand extends EssentialCommand {
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (s) -> KiloCommands.hasPermission(s, CommandPermission.NICKNAME_SELF);
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER = (s) -> KiloCommands.hasPermission(s, CommandPermission.NICKNAME_OTHERS);
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_EITHER = (s) -> PERMISSION_CHECK_OTHER.test(s) || PERMISSION_CHECK_SELF.test(s);

    public NicknameCommand() {
        super("nickname", CommandPermission.NICKNAME_SELF, new String[]{"nick"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> setSelf = literal("set").requires(PERMISSION_CHECK_EITHER).build();
        LiteralCommandNode<ServerCommandSource> setOther = literal("set").requires(PERMISSION_CHECK_OTHER).build();
        ArgumentCommandNode<ServerCommandSource, String> target = getUserArgument("user")
                .requires(PERMISSION_CHECK_OTHER).suggests(TabCompletions::allPlayers).build();

        ArgumentCommandNode<ServerCommandSource, String> nicknameSelf = argument("nickname", greedyString())
                .suggests(NicknameCommand::setSelfSuggestions).executes(this::setSelf).build();
        ArgumentCommandNode<ServerCommandSource, String> nicknameOther = argument("nickname", greedyString())
                .suggests(NicknameCommand::setOthersSuggestions).executes(this::setOther).build();

        LiteralCommandNode<ServerCommandSource> resetSelf = literal("reset").requires(PERMISSION_CHECK_SELF).executes(this::resetSelf).build();
        LiteralCommandNode<ServerCommandSource> resetOther = literal("reset").requires(PERMISSION_CHECK_OTHER).executes(this::resetOther).build();

        LiteralCommandNode<ServerCommandSource> other = literal("other").requires(PERMISSION_CHECK_OTHER).build();

        setOther.addChild(nicknameOther);

        target.addChild(setOther);
        target.addChild(resetOther);

        other.addChild(target);


        commandNode.addChild(other);
        setSelf.addChild(nicknameSelf);

        commandNode.addChild(setSelf);
        commandNode.addChild(resetSelf);
    }

    private int setSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity self = source.getPlayer();
        int maxLength = KiloConfig.main().nicknameMaxLength;
        String nickname = getString(ctx, "nickname");

        if (nickname.length() > maxLength || nickname.length() < 3)
            throw KiloCommands.getException(ExceptionMessageNode.NICKNAME_NOT_ACCEPTABLE, maxLength).create();

        String formattedNickname = "";
        if (KiloCommands.hasPermission(source, CommandPermission.NICKNAME_FORMATTING)) {
        	formattedNickname = TextFormat.translateAlternateColorCodes('&', nickname);
        } else {
        	formattedNickname = TextFormat.removeAlternateColorCodes('&', nickname);
        }

        User user = KiloServer.getServer().getUserManager().getOnline(self);

        KiloServer.getServer().getCommandSourceUser(source).sendMessage(new ChatMessage(messages.commands().nickname().setSelf
                .replace("{NICK}", user.getNickname().isPresent() ? user.getNickname().get() : user.getDisplayName())
                .replace("{NICK_NEW}", nickname)
                , true));

        user.setNickname(nickname);
        self.setCustomName(new LiteralText(formattedNickname));

        return 1;
    }

    private int setOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        String nickname = getString(ctx, "nickname");
        int maxLength = KiloConfig.main().nicknameMaxLength;

        if (nickname.length() > maxLength || nickname.length() < 3)
            throw KiloCommands.getException(ExceptionMessageNode.NICKNAME_NOT_ACCEPTABLE, maxLength).create();

        essentials.getUserThenAcceptAsync(source, getUserArgumentInput(ctx, "user"), (user) -> {
            String formattedNickname = TextFormat.translateAlternateColorCodes('&', nickname);
            KiloServer.getServer().getCommandSourceUser(source).sendMessage(new ChatMessage(messages.commands().nickname().setOthers
                    .replace("{NICK}", user.getNickname().isPresent() ? user.getNickname().get() : user.getDisplayName())
                    .replace("{NICK_NEW}", nickname)
                    .replace("{TARGET_TAG}", user.getNameTag())
                    , true));

            if (user.isOnline())
                ((OnlineUser) user).getPlayer().setCustomName(new LiteralText(formattedNickname));
            else {
                PlayerDataModifier dataModifier = new PlayerDataModifier(user.getUuid());
                if (!dataModifier.load())
                    return;
                dataModifier.setCustomName(new LiteralText(formattedNickname));
                dataModifier.save();
            }

            user.setNickname(nickname);
        });

        return SINGLE_SUCCESS;
    }

    private int resetSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.clearNickname();   /* This is an Optional.ofNullable, so the DataTracker will
                                   just reset the name without any other magic since TrackedData
                                   is always and automatically synchronized with the client. */

        player.setCustomName(null);

        KiloServer.getServer().getCommandSourceUser(ctx.getSource()).sendMessage(messages.commands().nickname().resetSelf);
        return 1;
    }

    private int resetOther(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser source = getServerUser(ctx);

        essentials.getUserThenAcceptAsync(source, getUserArgumentInput(ctx, "user"), (user) -> {
            user.clearNickname();   /* This is an Optional.ofNullable, so the DataTracker will
                                   just reset the name without any other magic since TrackedData
                                   is always and automatically synchronized with the client. */

            if (user.isOnline())
                ((OnlineUser) user).getPlayer().setCustomName(new LiteralText(""));
            else {
                PlayerDataModifier dataModifier = new PlayerDataModifier(user.getUuid());
                if (!dataModifier.load())
                    return;
                dataModifier.setCustomName(null);
                dataModifier.save();
            }

            KiloServer.getServer().getCommandSourceUser(ctx.getSource()).sendMessage(new ChatMessage(messages.commands().nickname().resetOthers
                    .replace("{TARGET_TAG}", user.getNameTag())
                    , true));
        });

        return SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> setSelfSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        User user = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer());
        List<String> strings = new ArrayList<>();
        if (user.getNickname().isPresent())
            strings.add(user.getNickname().get());

        return CommandSource.suggestMatching(strings, builder);
    }

    private static CompletableFuture<Suggestions> setOthersSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        User user = KiloServer.getServer().getUserManager().getOnline(getString(context, "user"));
        List<String> strings = new ArrayList<>();
        if (user != null && user.getNickname() != null &&  user.getNickname().isPresent())
            strings.add(user.getNickname().get());

        return CommandSource.suggestMatching(strings, builder);
    }

}
