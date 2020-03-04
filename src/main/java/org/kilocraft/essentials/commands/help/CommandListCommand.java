package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.TextUtils;

import java.util.Collection;

public class CommandListCommand {

    static Collection<CommandNode<ServerCommandSource>> commands;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Can't use essentialcommand as you can't make the literal execute
        LiteralArgumentBuilder<ServerCommandSource> commandList = CommandManager.literal("commandlist").requires(src -> KiloCommands.hasPermission(src, CommandPermission.COMMAND_LIST)).executes(CommandListCommand::list);
        dispatcher.register(commandList);
        commands = dispatcher.getRoot().getChildren();
    }

    private static int list (CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        TextUtils.ListStyle text = TextUtils.ListStyle.of(
                "Commands", Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY
        );

        for (CommandNode node : commands) {
            text.append(node.getName(), null, TextUtils.Events.onClickSuggest("/" + node.getName()));
        }

        KiloChat.sendMessageTo(player, text.setSize(commands.size()).build());
        return EssentialCommand.SINGLE_SUCCESS;
    }
}
