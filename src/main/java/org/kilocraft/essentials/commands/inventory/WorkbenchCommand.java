package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

public class WorkbenchCommand extends EssentialCommand {

    public WorkbenchCommand() {
        super("workbench", CommandPermission.WORKBENCH, new String[]{"craftingbench", "craft"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(WorkbenchCommand::execute);
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(WorkbenchCommand::createContainer, new TranslatableText("container.crafting")));
        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        return SUCCESS;
    }

    public static CraftingScreenHandler createContainer(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new CraftingScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

}
