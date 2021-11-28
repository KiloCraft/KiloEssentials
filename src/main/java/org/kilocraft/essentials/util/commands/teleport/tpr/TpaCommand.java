package org.kilocraft.essentials.util.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextColor;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.text.Texter;

public class TpaCommand extends EssentialCommand {
    public static final CommandPermission PERMISSION = CommandPermission.TELEPORTREQUEST;
    public static final TextColor GREEN_COLOR = TextColor.parseColor("EF2828");
    public static final TextColor RED_COLOR = TextColor.parseColor("38F71A");

    public TpaCommand() {
        super("tpa", PERMISSION, new String[]{"tpr"});
        this.withUsage("command.tpa.usage", "target");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> selectorArgument = this.getOnlineUserArgument("victim")
                .executes(this::request);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int request(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "victim");

        if (src.equals(target)) {
            throw KiloCommands.getException("exception.source_is_target").create();
        }

        if (target.getPreference(Preferences.DON_NOT_DISTURB) || !target.hasPermission(PERMISSION)) {
            throw KiloCommands.getException("exception.ignored", target.getFormattedDisplayName()).create();
        }

        if (UserUtils.TpaRequests.hasRequest(src, target)) {
            src.sendLangError("command.tpa.already_requested", target.getFormattedDisplayName());
            return FAILED;
        }

        UserUtils.TpaRequests.add(src, target, false);

        src.sendMessage(
                ComponentText.toText(this.tl("command.tpa.sent", target.getFormattedDisplayName()))
                        .append(" ")
                        .append(
                                ComponentUtils.wrapInSquareBrackets(
                                        Texter.getButton(" &c" + '\u00d7' + "&r ", "/tpcancel " + target.getUsername(), Texter.newText(ModConstants.translation("general.click_cancel")))
                                                .withStyle(style -> style.withColor(RED_COLOR))
                                )
                        )
        );

        target.sendMessage(
                ComponentText.toText(this.tl("command.tpa.receive", src.getFormattedDisplayName()))
                        .append(" ")
                        .append(
                                ComponentUtils.wrapInSquareBrackets(
                                        Texter.getButton(" &a" + '\u2714' + "&r ", "/tpaccept " + src.getUsername(), Texter.newText(ModConstants.translation("general.click_accept")))
                                                .withStyle(style -> style.withColor(GREEN_COLOR))
                                )
                        )
                        .append(" ")
                        .append(
                                ComponentUtils.wrapInSquareBrackets(
                                        Texter.getButton(" &c" + '\u00d7' + "&r ", "/tpdeny " + src.getUsername(), Texter.newText(ModConstants.translation("general.click_deny")))
                                                .withStyle(style -> style.withColor(RED_COLOR))
                                )
                        )
        );

        if (target.getPreference(Preferences.SOUNDS)) {
            ServerChat.pingUser(target, ServerChat.MentionTypes.PRIVATE);
        }

        return SUCCESS;
    }

}
