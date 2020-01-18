package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;

import java.util.function.Predicate;

public class ItemCommand extends EssentialCommand {
    private Predicate<ServerCommandSource> PERMISSION_CHECK = src ->
            KiloCommands.hasPermission(src, CommandPermission.ITEM_NAME) ||
                    KiloCommands.hasPermission(src, CommandPermission.ITEM_LORE) ||
                    KiloCommands.hasPermission(src, CommandPermission.ITEM_COMMANDS);

    public ItemCommand() {
        super("item");
        super.PERMISSION_CHECK_ROOT = PERMISSION_CHECK;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ItemNameCommand.registerChild(argumentBuilder, dispatcher);
        ItemLoreCommand.registerChild(argumentBuilder, dispatcher);
        ItemNbtCommand.registerChild(argumentBuilder, dispatcher);
    }
}
