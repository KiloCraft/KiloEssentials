package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class ModifyItemCommand extends EssentialCommand {
    public ModifyItemCommand() {
        super("modifyitem", src ->
            KiloCommands.hasPermission(src, CommandPermission.ITEM_NAME) ||
                    KiloCommands.hasPermission(src, CommandPermission.ITEM_LORE) ||
                    KiloCommands.hasPermission(src, CommandPermission.ITEM_COMMANDS) ||
                    KiloCommands.hasPermission(src, CommandPermission.ITEM_ENCHANT) ||
                    KiloCommands.hasPermission(src, CommandPermission.ITEM_MEND)
        );
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ItemNameCommand.registerChild(argumentBuilder, dispatcher);
        ItemLoreCommand.registerChild(argumentBuilder, dispatcher);
        PowerToolsCommand.registerChild(argumentBuilder, dispatcher);
        ItemMendCommand.registerChild(argumentBuilder, dispatcher);
    }
}
