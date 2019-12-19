package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;

import static net.minecraft.server.command.CommandManager.literal;

public class ItemCommand {
    private static LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("item").executes(KiloCommands::executeSmartUsage);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("item");
    	argumentBuilder.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("item"), 2));
    	
        ItemNameCommand.registerChild(argumentBuilder);
        ItemLoreCommand.registerChild(argumentBuilder);

        dispatcher.register(argumentBuilder);
    }
}
