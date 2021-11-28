package org.kilocraft.essentials.api.containergui.buttons;

import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GUIButton {
    public final GUIButtonSettings settings;
    public final int forcedSlot;

    public GUIButton(@NotNull final GUIButtonSettings settings) {
        this(-1, settings);
    }

    public GUIButton(final int forcedSlot, @NotNull final GUIButtonSettings settings) {
        this.forcedSlot = forcedSlot;
        this.settings = settings;
    }

    public void onClick(final ClickAction action) {
        if (this.settings.actions != null && this.settings.actions.containsKey(action)) {
            this.settings.actions.get(action).run();
        }
    }

    public enum ClickAction {
        CLICK(ClickType.PICKUP),
        PICKUP_ALL(ClickType.PICKUP_ALL),
        CLONE(ClickType.CLONE),
        DROP(ClickType.THROW),
        QUICK_MOVE(ClickType.QUICK_MOVE);

        private final ClickType type;

        ClickAction(final ClickType type) {
            this.type = type;
        }

        @Nullable
        public static ClickAction getBySlotActionType(@NotNull final ClickType type) {
            for (ClickAction value : values()) {
                if (value.type == type) {
                    return value;
                }
            }

            return null;
        }
    }
}
