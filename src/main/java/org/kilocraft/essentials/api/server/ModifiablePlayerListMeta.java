package org.kilocraft.essentials.api.server;

import net.minecraft.text.Text;

public interface ModifiablePlayerListMeta {
    void setHeader(Text text);

    void setFooter(Text text);
}
