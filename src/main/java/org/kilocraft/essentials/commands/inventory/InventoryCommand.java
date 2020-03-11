package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class InventoryCommand extends EssentialCommand {

    public InventoryCommand() {
        super("inventory", CommandPermission.SEEK_INVENTORY, new String[]{"inv", "seekinv"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getUserArgument("user")
                .executes(this::execute);

        this.commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineUser sender = this.getOnlineUser(ctx);
        final String inputName = this.getUserArgumentInput(ctx, "user");

        if (sender.getUsername().equals(inputName)) {
            sender.sendError(tl("command.inventory.error"));
            return SINGLE_FAILED;
        }

        if (this.server.getOnlineUser(inputName) == null && !KiloConfig.main().cachedInventoriesSection().enabled) {
            sender.sendError(tl("command.inventory.not_enabled"));
            return SINGLE_FAILED;
        }

        final AtomicInteger integer = new AtomicInteger(super.AWAIT_RESPONSE);
        super.essentials.getUserThenAcceptAsync(sender, inputName, (user) -> {
            ServerPlayerEntity player = sender.getPlayer();

            if (user.getInventory() == null || user.getInventory().getMain() == null) {
                sender.sendError(tl("command.inventory.no_cache"));
                return;
            }

            player.openHandledScreen(
                    create(player, user.getInventory().getMain().get(), new LiteralText(tl("command.inventory.info", "")))
            );

            integer.set(this.SINGLE_SUCCESS);
        });

        return integer.get();
    }

    private SimpleNamedScreenHandlerFactory create(final ServerPlayerEntity src, final Inventory inv, Text text) {
//        return new SimpleNamedScreenHandlerFactory((syncId, playerInv, player) ->
//                new GenericContainerScreen(GenericContainerScreenHandler, player.inventory, text),
//                text);
        return null;
    }

}
