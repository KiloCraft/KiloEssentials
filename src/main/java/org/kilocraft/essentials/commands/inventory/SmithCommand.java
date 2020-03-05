package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.class_4862;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.SimpleNamedContainerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

public class SmithCommand extends EssentialCommand {

    public SmithCommand() {
        super("smith", CommandPermission.SMITH, new String[]{"smithingtable", "smithbench"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        KiloChat.sendLangMessageTo(context.getSource(), "general.open_container", "SmithingTable");

        player.openContainer(new SimpleNamedContainerFactory(this::createContainer, new TranslatableText("container.upgrade")));
        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        return 1;
    }

    public Container createContainer(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new class_4862(syncId, inventory, BlockContext.EMPTY);
    }

}
