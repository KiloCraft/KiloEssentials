package org.kilocraft.essentials.util.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;

public class WorkbenchCommand extends EssentialCommand {

    public WorkbenchCommand() {
        super("workbench", CommandPermission.WORKBENCH, new String[]{"craftingbench", "craft"});
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(WorkbenchCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        player.openMenu(new SimpleMenuProvider(WorkbenchCommand::createContainer, new TranslatableComponent("container.crafting")));
        player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        return SUCCESS;
    }

    public static CraftingMenu createContainer(int syncId, Inventory inventory, Player player) {
        return new CraftingMenu(syncId, inventory, ContainerLevelAccess.create(player.level, player.blockPosition()));
    }

}
