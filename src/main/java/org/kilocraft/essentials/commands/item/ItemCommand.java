package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;

import static net.minecraft.server.command.CommandManager.literal;

public class ItemCommand {
    private static LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("item").executes(KiloCommands::executeSmartUsage);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    	argumentBuilder.requires(s -> KiloCommands.hasPermission(s, CommandPermission.ITEM_NAME) ||
                KiloCommands.hasPermission(s, CommandPermission.ITEM_LORE));
    	
        ItemNameCommand.registerChild(argumentBuilder);
        ItemLoreCommand.registerChild(argumentBuilder);

        dispatcher.register(argumentBuilder);
    }
}
