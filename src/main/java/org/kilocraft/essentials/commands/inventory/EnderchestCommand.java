package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.inventory.ServerUserInventory;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class EnderchestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> enderchestCommand = dispatcher.register(literal("ec")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.ENDERCHEST_SELF))
                .executes(ctx -> execute(ctx.getSource().getPlayer(), ctx.getSource().getPlayer()))
                .then(argument("user", EntityArgumentType.player())
                        .requires(src -> KiloCommands.hasPermission(src, CommandPermission.ENDERCHEST_OTHERS))
                        .suggests(TabCompletions::allPlayers)
                        .executes(ctx -> execute(ctx.getSource().getPlayer(), EntityArgumentType.getPlayer(ctx, "user")))));

        dispatcher.getRoot().addChild(enderchestCommand);
        dispatcher.register(literal("enderchest").redirect(enderchestCommand).requires(src -> hasPermission(src, "enderchest", 3))
                .executes(ctx -> execute(ctx.getSource().getPlayer(), ctx.getSource().getPlayer())));
    }

    private static int execute(ServerPlayerEntity sender, ServerPlayerEntity target) {
        ServerUserInventory.openEnderchest(sender, target);
        if (CommandHelper.areTheSame(sender, target))
            KiloChat.sendLangMessageTo(sender, "command.enderchest.open");
        else
            KiloChat.sendLangMessageTo(sender, "command.enderchest.open.others", target.getEntityName());

        return SUCCESS();
    }

}
