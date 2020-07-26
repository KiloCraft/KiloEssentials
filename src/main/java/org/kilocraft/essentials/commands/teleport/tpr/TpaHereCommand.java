package org.kilocraft.essentials.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Texts;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.text.Texter;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public class TpaHereCommand extends EssentialCommand {
    public TpaHereCommand() {
        super("tpahere", TpaCommand.PERMISSION, new String[]{"tprhere"});
        this.withUsage("command.tpa.usage", "target");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument = this.getOnlineUserArgument("victim")
                .executes(this::request);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int request(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "victim");

        if (src.equals(target)) {
            throw KiloCommands.getException(ExceptionMessageNode.SOURCE_IS_TARGET).create();
        }

        if (target.ignored(src.getUuid()) || target.getPreference(Preferences.DON_NOT_DISTURB) || !target.hasPermission(TpaCommand.PERMISSION)) {
            throw KiloCommands.getException(ExceptionMessageNode.IGNORED, target.getFormattedDisplayName()).create();
        }

        if (UserUtils.TpaRequests.hasRequest(src, target)) {
            src.sendLangError("command.tpa.already_requested", target.getFormattedDisplayName());
            return FAILED;
        }

        UserUtils.TpaRequests.add(src, target, true);

        src.sendMessage(
                Texter.newText(ModConstants.translation("command.tpa.sent", target.getFormattedDisplayName()))
                        .append(" ")
                        .append(Texts.bracketed(Texter.getButton(" &c" + '\u00d7' + "&r ", "/tpcancel " + target.getUsername(), Texter.newText("&cCancel"))))
        );

        target.sendMessage(
                Texter.newText(ModConstants.translation("command.tpa.receive.here", src.getFormattedDisplayName()))
                        .append(" ")
                        .append(Texts.bracketed(Texter.getButton(" &a" + '\u2714' + "&r ", "/tpaccept " + src.getUsername(), Texter.newText("&aClick to accept"))))
                        .append(" ")
                        .append(Texts.bracketed(Texter.getButton(" &c" + '\u00d7' + "&r ", "/tpdeny " + src.getUsername(), Texter.newText("&cClick to deny"))))
        );

        if (target.getPreference(Preferences.SOUNDS)) {
            ServerChat.pingPlayer(target.asPlayer(), ServerChat.MentionTypes.PRIVATE);
        }
        return SUCCESS;
    }

}
