package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.config.provided.localVariables.PlayerConfigVariables;

public class DiscordCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("discord").executes(DiscordCommand::execute));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        KiloChat.sendMessageTo(context.getSource().getPlayer(),
                new ChatMessage(
                        KiloConifg.getProvider().getMessages().getLocal(
                                true,
                                "general.discord",
                                new PlayerConfigVariables(context.getSource().getPlayer())
                        ),
                        true
                ));
        return 1;
    }
}
