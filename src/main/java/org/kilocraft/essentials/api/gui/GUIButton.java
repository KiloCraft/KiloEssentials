package org.kilocraft.essentials.api.gui;

import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;

public class GUIButton {
    protected final GUIButtonSettings settings;

    public GUIButton(@NotNull final GUIButtonSettings settings) {
        this.settings = settings;
    }

    public void onAction(final SlotActionType type) {
        switch (type) {
            case SWAP:
                this.settings.onSwap.run();
            case CLONE:
                this.settings.onClone.run();
            case THROW:
                this.settings.onThrow.run();
            case PICKUP:
                this.settings.onPickup.run();
            case PICKUP_ALL:
                this.settings.onPickUpAll.run();
            case QUICK_MOVE:
                this.settings.onQuickMove.run();
            default:
                break;
        }
    }
}
