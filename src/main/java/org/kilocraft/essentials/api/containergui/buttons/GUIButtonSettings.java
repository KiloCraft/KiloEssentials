package org.kilocraft.essentials.api.containergui.buttons;

import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GUIButtonSettings {
    public static GUIButtonSettings dummy() {
        GUIButtonSettings settings = new GUIButtonSettings();
        settings.dummy = true;
        settings.icon = new ItemStack(Items.AIR);
        return settings;
    }

    public boolean dummy;
    public ItemStack icon;
    public Component name;
    public Map<GUIButton.ClickAction, Runnable> actions;
}
