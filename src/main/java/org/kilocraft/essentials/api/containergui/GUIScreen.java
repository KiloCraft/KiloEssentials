/*
package org.kilocraft.essentials.api.containergui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.containergui.buttons.GUIButton;

import java.util.Map;

public class GUIScreen extends ScreenHandler {
    private final Inventory inv;
    private final Map<Integer, GUIButton> buttons;

    protected GUIScreen(@NotNull final ScreenHandlerType<?> screenHandlerType,
                        final int syncId,
                        @NotNull final PlayerInventory playerInv,
                        @NotNull final Inventory inv,
                        final int rows,
                        @NotNull Map<Integer, GUIButton> buttons) {
        super(screenHandlerType, syncId);
        checkSize(inv, rows * 9);
        this.inv = inv;
        this.buttons = buttons;
        inv.onOpen(playerInv.player);
        int i = (rows - 4) * 18;
        int n;
        int m;

        for (n = 0; n < rows; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(inv, m + n * 9, 8 + m * 18, 18 + n * 18));
            }
        }

        for (n = 0; n < 3; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInv, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
            }
        }

        for (n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInv, n, 8 + n * 18, 161 + i));
        }
    }

    @Override
    public boolean canUse(PlayerEntity playerEntity) {
        return true;
    }

    @Override
    public void onSlotClick(int slot, int clickData, SlotActionType slotActionType, PlayerEntity playerEntity) {
        if (this.buttons.containsKey(slot)) {
            this.buttons.get(slot).onClick(GUIButton.ClickAction.getBySlotActionType(slotActionType));
        }

        this.inv.markDirty();
        ((ServerPlayerEntity) playerEntity).networkHandler.sendPacket(
                new InventoryS2CPacket(playerEntity.currentScreenHandler.syncId, playerEntity.currentScreenHandler.getRevision(), playerEntity.currentScreenHandler.getStacks(), playerEntity.currentScreenHandler.getCursorStack())
        );

        this.sendContentUpdates();
    }

}
*/
