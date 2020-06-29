package org.kilocraft.essentials.util;

import net.minecraft.util.Identifier;

public class ResourceLocation extends Identifier {
    protected ResourceLocation(String[] strings) {
        super("essentials", strings[0] + "." + strings[1]);
    }

    public ResourceLocation(String string) {
        super("essentials:" + string);
    }

    public ResourceLocation(String string, String string2) {
        super("essentials", string + "." + string2);
    }
}
