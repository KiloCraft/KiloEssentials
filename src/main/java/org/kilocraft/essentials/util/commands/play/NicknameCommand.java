package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class NicknameCommand extends EssentialCommand {
    public static final Predicate<CommandSourceStack> PERMISSION_CHECK_SELF = (s) -> KiloCommands.hasPermission(s, CommandPermission.NICKNAME_SELF);
    public static final Predicate<CommandSourceStack> PERMISSION_CHECK_OTHER = (s) -> KiloCommands.hasPermission(s, CommandPermission.NICKNAME_OTHERS);
    public static final Predicate<CommandSourceStack> PERMISSION_CHECK_EITHER = (s) -> PERMISSION_CHECK_OTHER.test(s) || PERMISSION_CHECK_SELF.test(s);

    public NicknameCommand() {
        super("nickname", CommandPermission.NICKNAME_SELF, new String[]{"nick"});
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> setSelf = this.literal("set").requires(PERMISSION_CHECK_EITHER).build();
        LiteralCommandNode<CommandSourceStack> setOther = this.literal("set").requires(PERMISSION_CHECK_OTHER).build();
        ArgumentCommandNode<CommandSourceStack, String> target = this.getUserArgument("user")
                .requires(PERMISSION_CHECK_OTHER).suggests(ArgumentSuggestions::allPlayers).build();

        ArgumentCommandNode<CommandSourceStack, String> nicknameSelf = this.argument("nickname", greedyString())
                .suggests(NicknameCommand::setSelfSuggestions).executes(ctx -> this.setNickname(ctx, (OnlineServerUser) this.getOnlineUser(ctx.getSource().getPlayerOrException()))).build();
        ArgumentCommandNode<CommandSourceStack, String> nicknameOther = this.argument("nickname", greedyString())
                .suggests(NicknameCommand::setOthersSuggestions).executes(ctx -> this.setNickname(ctx, (OnlineServerUser) this.getOnlineUser(ctx, "user"))).build();

        LiteralCommandNode<CommandSourceStack> resetSelf = this.literal("reset").requires(PERMISSION_CHECK_SELF).executes(this::resetSelf).build();
        LiteralCommandNode<CommandSourceStack> resetOther = this.literal("reset").requires(PERMISSION_CHECK_OTHER).executes(this::resetOther).build();

        LiteralCommandNode<CommandSourceStack> other = this.literal("other").requires(PERMISSION_CHECK_OTHER).build();

        setOther.addChild(nicknameOther);

        target.addChild(setOther);
        target.addChild(resetOther);

        other.addChild(target);


        this.commandNode.addChild(other);
        setSelf.addChild(nicknameSelf);

        this.commandNode.addChild(setSelf);
        this.commandNode.addChild(resetSelf);
    }

    private int setNickname(CommandContext<CommandSourceStack> ctx, OnlineServerUser target) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = target.asPlayer();
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

        final CommandSourceServerUser src = CommandSourceServerUser.of(source);
        String oldNickName = target.getNickname().isPresent() ? target.getNickname().get() : target.getDisplayName();
        if ((target.equals(this.getCommandSource(ctx)))) {
            src.sendLangMessage("command.nickname.set_self", nickname, oldNickName);
        } else {
            src.sendLangMessage("command.nickname.set_other", nickname, oldNickName, target.getNameTag());
        }

        target.setNickname(nickname);
        player.setCustomName(ComponentText.toText(nickname));
        return AWAIT;
    }

    private int resetSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        user.clearNickname();

        player.setCustomName(null);
        user.sendLangMessage("command.nickname.reset_self");
        return SUCCESS;
    }

    private int resetOther(CommandContext<CommandSourceStack> ctx) {
        CommandSourceUser src = this.getCommandSource(ctx);

        this.getUserManager().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (user) -> {
            user.clearNickname();

            if (user.isOnline())
                ((OnlineUser) user).asPlayer().setCustomName(new TextComponent(""));
            else {
                PlayerDataModifier dataModifier = new PlayerDataModifier(user.getUuid());
                if (!dataModifier.load())
                    return;
                dataModifier.setCustomName(null);
                dataModifier.save();
            }
            src.sendLangMessage("command.nickname.reset_other", user.getNameTag());
        });

        return SUCCESS;
    }

    private static CompletableFuture<Suggestions> setSelfSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        User user = KiloEssentials.getUserManager().getOnline(context.getSource().getPlayerOrException());
        List<String> strings = new ArrayList<>();
        if (user.getPreference(Preferences.NICK).isPresent())
            strings.add(user.getPreference(Preferences.NICK).get());

        return SharedSuggestionProvider.suggest(strings, builder);
    }

    private static CompletableFuture<Suggestions> setOthersSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        User user = KiloEssentials.getUserManager().getOnline(getString(context, "user"));
        List<String> strings = new ArrayList<>();
        if (user != null && user.getPreference(Preferences.NICK).isPresent())
            strings.add(user.getPreference(Preferences.NICK).get());

        return SharedSuggestionProvider.suggest(strings, builder);
    }

}
