package org.kilocraft.essentials.util;

import com.mojang.brigadier.StringReader;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;

public class SelectorUtils {
    public static EntitySelector parse(String input, boolean atAllowed) {
        EntitySelectorReader reader = new EntitySelectorReader(new StringReader(input), atAllowed);
        return reader.build();
    }

}
