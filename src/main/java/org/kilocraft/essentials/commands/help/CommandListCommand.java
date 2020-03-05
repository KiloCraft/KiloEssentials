package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.TextUtils;

public class CommandListCommand extends EssentialCommand {
    public CommandListCommand() {
        super("commandlist", new String[]{"commands"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(ctx -> execute(ctx, dispatcher));
    }

    private int execute(CommandContext<ServerCommandSource> ctx, CommandDispatcher<ServerCommandSource> dispatcher) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        TextUtils.ListStyle text = TextUtils.ListStyle.of(
                "Commands", Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY
        );

        for (CommandNode<ServerCommandSource> node : dispatcher.getRoot().getChildren()) {
            text.append(node.getName(), null, TextUtils.Events.onClickSuggest("/" + node.getName()));
        }

        KiloChat.sendMessageTo(player, text.setSize(dispatcher.getRoot().getChildren().size()).build());
        return EssentialCommand.SINGLE_SUCCESS;
    }
}
