package org.kilocraft.essentials.util.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.text.Texter;

public class InventoryCommand extends EssentialCommand {
    public InventoryCommand() {
        super("inventory", CommandPermission.SEEK_INVENTORY, new String[]{"inv", "seekinv"});
    }

    private static void setSlotsInit(ServerPlayer target, AbstractContainerMenu handler) {
        for (int i = 0; i < 36; i++) {
            handler.setItem(i, handler.getStateId(), target.getInventory().items.get(i));
        }

        for (int i = 0; i < 4; i++) {
            handler.setItem(i + 36, handler.getStateId(), target.getInventory().armor.get(i));
        }

        handler.setItem(44, handler.getStateId(), target.getInventory().offhand.get(0));
    }

    private static void copySlotsFromInventory(ServerPlayer target, AbstractContainerMenu handler, int slotID) {
        if (slotID < 36) {
            target.getInventory().items.set(slotID, handler.getItems().get(slotID));
        } else if (slotID < 40) {
            target.getInventory().armor.set(slotID - 36, handler.getItems().get(slotID));
        } else if (slotID == 44) {
            target.getInventory().offhand.set(0, handler.getItems().get(slotID));
        }
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> userArgument = this.getOnlineUserArgument("target")
                .executes(this::execute);

        this.commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final OnlineUser sender = this.getOnlineUser(ctx);
        final OnlineUser target = this.getOnlineUser(ctx, "target");

        if (sender.equals(target)) {
            sender.sendLangError("command.inventory.error");
            return FAILED;
        }

        sender.asPlayer().openMenu(this.factory(sender, target));
        sender.sendLangMessage("general.seek_screen", target.getFormattedDisplayName(), "");
        return SUCCESS;
    }

    private MenuProvider factory(final OnlineUser src, final OnlineUser target) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                Component text;
                Component translatable = new TranslatableComponent("container.inventory");

                if (src.equals(target)) {
                    text = Texter.newText().append(translatable).append(" ").append(target.getFormattedDisplayName());
                } else {
                    text = translatable;
                }

                return text;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                AbstractContainerMenu handler = ChestMenu.fiveRows(syncId, src.asPlayer().getInventory());
                handler.addSlotListener(new ContainerListener() {

                    @Override
                    public void slotChanged(AbstractContainerMenu screenHandler, int i, ItemStack itemStack) {
                        copySlotsFromInventory(target.asPlayer(), handler, syncId);
                    }

                    @Override
                    public void dataChanged(AbstractContainerMenu screenHandler, int i, int j) {
                        ((ServerPlayer) player).connection.send(new ClientboundContainerSetDataPacket(screenHandler.containerId, i, j));
                    }
                });

                return handler;
            }
        };
    }

}
