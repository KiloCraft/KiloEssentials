package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.Container;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.literal;

public class WorkbenchCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.WORKBENCH);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("craftingbench")
                .requires(PERMISSION_CHECK)
                .executes(WorkbenchCommand::execute).build();

        dispatcher.getRoot().addChild(rootCommand);
        dispatcher.register(literal("workbench").requires(PERMISSION_CHECK).executes(WorkbenchCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        KiloChat.sendLangMessageTo(context.getSource(), "general.open_container", "CraftingTable");

        player.openContainer(new ClientDummyContainerProvider(WorkbenchCommand::createMenu, new TranslatableText("container.crafting")));
        return 1;
    }

    private static Container createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new CraftingTableContainer(syncId, playerInventory);
    }
}
