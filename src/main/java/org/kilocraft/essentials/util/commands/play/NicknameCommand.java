package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.Format;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.player.PlayerDataModifier;

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
        LiteralCommandNode<ServerCommandSource> setSelf = this.literal("set").requires(PERMISSION_CHECK_EITHER).build();
        LiteralCommandNode<ServerCommandSource> setOther = this.literal("set").requires(PERMISSION_CHECK_OTHER).build();
        ArgumentCommandNode<ServerCommandSource, String> target = this.getUserArgument("user")
                .requires(PERMISSION_CHECK_OTHER).suggests(ArgumentSuggestions::allPlayers).build();

        ArgumentCommandNode<ServerCommandSource, String> nicknameSelf = this.argument("nickname", greedyString())
                .suggests(NicknameCommand::setSelfSuggestions).executes(ctx -> this.setNickname(ctx, (OnlineServerUser) this.getOnlineUser(ctx.getSource().getPlayer()))).build();
        ArgumentCommandNode<ServerCommandSource, String> nicknameOther = this.argument("nickname", greedyString())
                .suggests(NicknameCommand::setOthersSuggestions).executes(ctx -> this.setNickname(ctx, (OnlineServerUser) this.getOnlineUser(ctx, "user"))).build();

        LiteralCommandNode<ServerCommandSource> resetSelf = this.literal("reset").requires(PERMISSION_CHECK_SELF).executes(this::resetSelf).build();
        LiteralCommandNode<ServerCommandSource> resetOther = this.literal("reset").requires(PERMISSION_CHECK_OTHER).executes(this::resetOther).build();

        LiteralCommandNode<ServerCommandSource> other = this.literal("other").requires(PERMISSION_CHECK_OTHER).build();

        setOther.addChild(nicknameOther);

        target.addChild(setOther);
        target.addChild(resetOther);

        other.addChild(target);


        this.commandNode.addChild(other);
        setSelf.addChild(nicknameSelf);

        this.commandNode.addChild(setSelf);
        this.commandNode.addChild(resetSelf);
    }

    private int setNickname(CommandContext<ServerCommandSource> ctx, OnlineServerUser target) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = target.asPlayer();
        int maxLength = KiloConfig.main().nicknameMaxLength;
        String nickname = getString(ctx, "nickname");
        String unformatted = ComponentText.clearFormatting(nickname);

        if (unformatted.length() > maxLength || unformatted.length() < 3) {
            throw KiloCommands.getException("exception.nickname_not_acceptable", maxLength).create();
        }

        nickname = Format.validatePermission(target, nickname, CommandPermission.PERMISSION_PREFIX + "nickname.formatting");

        if (this.getUserManager().shouldNotUseNickname(target, nickname)) {
            target.sendLangMessage("command.nickname.already_taken");
            return -1;
        }

        CommandSourceServerUser.of(source).sendMessage((target.equals(this.getCommandSource(ctx)) ? this.messages.commands().nickname().setSelf : this.messages.commands().nickname().setOthers)
                .replace("{NICK}", target.getNickname().isPresent() ? target.getNickname().get() : target.getDisplayName())
                .replace("{NICK_NEW}", nickname));

        target.setNickname(nickname);
        player.setCustomName(ComponentText.toText(nickname));
        return AWAIT;
    }

    private int resetSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        User user = KiloEssentials.getUserManager().getOnline(player);
        user.clearNickname();

        player.setCustomName(null);

        this.getCommandSource(ctx).sendMessage(this.messages.commands().nickname().resetSelf);
        return SUCCESS;
    }

    private int resetOther(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = this.getCommandSource(ctx);

        this.getUserManager().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (user) -> {
            user.clearNickname();

            if (user.isOnline())
                ((OnlineUser) user).asPlayer().setCustomName(new LiteralText(""));
            else {
                PlayerDataModifier dataModifier = new PlayerDataModifier(user.getUuid());
                if (!dataModifier.load())
                    return;
                dataModifier.setCustomName(null);
                dataModifier.save();
            }
            src.sendMessage(this.messages.commands().nickname().resetOthers
                    .replace("{TARGET_TAG}", user.getNameTag()));
        });

        return SUCCESS;
    }

    private static CompletableFuture<Suggestions> setSelfSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        User user = KiloEssentials.getUserManager().getOnline(context.getSource().getPlayer());
        List<String> strings = new ArrayList<>();
        if (user.getPreference(Preferences.NICK).isPresent())
            strings.add(user.getPreference(Preferences.NICK).get());

        return CommandSource.suggestMatching(strings, builder);
    }

    private static CompletableFuture<Suggestions> setOthersSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        User user = KiloEssentials.getUserManager().getOnline(getString(context, "user"));
        List<String> strings = new ArrayList<>();
        if (user != null && user.getPreference(Preferences.NICK).isPresent())
            strings.add(user.getPreference(Preferences.NICK).get());

        return CommandSource.suggestMatching(strings, builder);
    }

}
