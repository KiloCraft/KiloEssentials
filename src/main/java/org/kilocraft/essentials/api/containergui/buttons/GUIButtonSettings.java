package org.kilocraft.essentials.api.containergui.buttons;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

public class GUIButtonSettings {
    public static GUIButtonSettings dummy() {
        GUIButtonSettings settings = new GUIButtonSettings();
        settings.dummy = true;
        settings.icon = new ItemStack(Items.AIR);
        return settings;
    }

    public boolean dummy;
    public ItemStack icon;
    public Text name;
    public Map<GUIButton.ClickAction, Runnable> actions;
}
