package org.kilocraft.essentials.util.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class AnvilCommand extends EssentialCommand {
    public AnvilCommand() {
        super("anvil", CommandPermission.ANVIL);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(this::createMenu, new TranslatableText("container.repair")));
        return SUCCESS;
    }

    private ForgingScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new AnvilScreenHandler(syncId, playerInventory);
    }
}
