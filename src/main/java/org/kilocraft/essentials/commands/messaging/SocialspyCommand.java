package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.chat.ServerChat;

public class SocialspyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("socialspy");
        command.requires(source -> {
            try {
                source.getPlayer();
            } catch (CommandSyntaxException e) {
                return false;
            }
            return KiloEssentials.hasPermissionNode(source, EssentialPermission.SPY_CHAT);
        });
        command.executes(context -> {
            if (ServerChat.isSocialSpy(context.getSource().getPlayer())) {
                ServerChat.removeSocialSpy(context.getSource().getPlayer());
                context.getSource().sendFeedback(new LiteralText("SocialSpy is now inactive").formatted(Formatting.YELLOW), false);
            } else {
                ServerChat.addSocialSpy(context.getSource().getPlayer());
                context.getSource().sendFeedback(new LiteralText("SocialSpy is now active").formatted(Formatting.YELLOW), false);
            }
            return 1;
        });
        dispatcher.register(command);
    }
}
