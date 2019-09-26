package org.kilocraft.essentials.craft.commands.essentials.ItemCommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ItemCommand {
    private static LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("item")
           /* .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.item", 3))*/;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
//        Thimble.permissionWriters.add(pair -> {
//            try {
//                Thimble.PERMISSIONS.getPermission("kiloessentials.command.item", CommandPermission.class); // Permission that updates command tree
//            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        });
        ItemNameCommand.registerChild(argumentBuilder);
        ItemLoreCommand.registerChild(argumentBuilder);

        dispatcher.register(argumentBuilder);
    }
}
