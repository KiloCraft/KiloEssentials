package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.text.Texter;

public class InventoryCommand extends EssentialCommand {
    public InventoryCommand() {
        super("inventory", CommandPermission.SEEK_INVENTORY, new String[]{"inv", "seekinv"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getOnlineUserArgument("target")
                .executes(this::execute);

        this.commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineUser sender = this.getOnlineUser(ctx);
        final OnlineUser target = this.getOnlineUser(ctx, "target");

        if (sender.equals(target)) {
            sender.sendError(tl("command.inventory.error"));
            return FAILED;
        }

        sender.asPlayer().openHandledScreen(factory(sender, target));
        sender.sendLangMessage("general.seek_screen", target.getFormattedDisplayName(), "");
        return SUCCESS;
    }

    private NamedScreenHandlerFactory factory(final OnlineUser src, final OnlineUser target) {
        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                Text text;
                Text translatable =  new TranslatableText("container.inventory");

                if (src.equals(target)) {
                    text = Texter.newText().append(translatable).append(" ").append(target.getFormattedDisplayName());
                } else {
                    text = translatable;
                }

                return text;
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                ScreenHandler handler = GenericContainerScreenHandler.createGeneric9x5(syncId, src.asPlayer().method_31548());
                handler.addListener(new ScreenHandlerListener() {
                    @Override
                    public void onHandlerRegistered(ScreenHandler screenHandler, DefaultedList<ItemStack> defaultedList) {
                        setSlotsInit(target.asPlayer(), handler);
                    }

                    @Override
                    public void onSlotUpdate(ScreenHandler screenHandler, int i, ItemStack itemStack) {
                        copySlotsFromInventory(target.asPlayer(), handler, syncId);
                    }

                    @Override
                    public void onPropertyUpdate(ScreenHandler screenHandler, int i, int j) {
                        ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(screenHandler.syncId, i, j));
                    }
                });

                return handler;
            }
        };
    }

    private static void setSlotsInit(ServerPlayerEntity target, ScreenHandler handler){
        for (int i = 0; i < 36; i++){
            handler.setStackInSlot(i, target.method_31548().main.get(i));
        }

        for (int i = 0; i < 4; i++){
            handler.setStackInSlot(i + 36, target.method_31548().armor.get(i));
        }

        handler.setStackInSlot(44, target.method_31548().offHand.get(0));
    }

    private static void copySlotsFromInventory(ServerPlayerEntity target, ScreenHandler handler, int slotID){
        if (slotID < 36){
            target.method_31548().main.set(slotID, handler.getStacks().get(slotID));
        } else if (slotID < 40){
            target.method_31548().armor.set(slotID - 36, handler.getStacks().get(slotID));
        } else if (slotID == 44){
            target.method_31548().offHand.set(0, handler.getStacks().get(slotID));
        }
    }

}
