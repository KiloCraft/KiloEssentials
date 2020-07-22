package org.kilocraft.essentials.api.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GUIButtonSettings {
    public boolean dummy;
    public int slot;
    public ItemStack icon;
    public Text name;
    Runnable onPickup = () -> {};
    Runnable onQuickMove = () -> {};
    Runnable onSwap = () -> {};
    Runnable onClone = () -> {};
    Runnable onThrow = () -> {};
    Runnable onPickUpAll = () -> {};
}
