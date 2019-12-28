package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.executeUsageFor;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class InventoryCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> inventoryCommand = dispatcher.register(literal("inventory")
                .requires(src -> hasPermission(src, "inventory", 2))
                .executes(ctx -> executeUsageFor("command.inventory.usage", ctx.getSource()))
        );

    }
}
