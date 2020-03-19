package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.*;
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
        KiloChat.sendLangMessageTo(context.getSource(), "general.open_screen", "SmithingTable");

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(this::createContainer, new TranslatableText("container.upgrade")));
        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        return 1;
    }

    public ForgingScreenHandler createContainer(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new SmithingScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

}
