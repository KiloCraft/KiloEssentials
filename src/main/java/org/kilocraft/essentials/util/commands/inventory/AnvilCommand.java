package org.kilocraft.essentials.util.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;

public class AnvilCommand extends EssentialCommand {
    public AnvilCommand() {
        super("anvil", CommandPermission.ANVIL);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        player.openMenu(new SimpleMenuProvider(this::createMenu, new TranslatableComponent("container.repair")));
        return SUCCESS;
    }

    private ItemCombinerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new AnvilMenu(syncId, playerInventory, ContainerLevelAccess.create(player.level, player.blockPosition()));
    }
}
