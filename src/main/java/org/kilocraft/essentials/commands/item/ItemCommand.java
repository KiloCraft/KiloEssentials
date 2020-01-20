package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class ItemCommand extends EssentialCommand {
    public ItemCommand() {
        super("item", CommandPermission.ITEM_NAME, 2);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ItemNameCommand.registerChild(argumentBuilder, dispatcher);
        ItemLoreCommand.registerChild(argumentBuilder, dispatcher);
    }
}
