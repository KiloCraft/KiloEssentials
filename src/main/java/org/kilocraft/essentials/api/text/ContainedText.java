package org.kilocraft.essentials.api.text;

import java.util.List;
import net.minecraft.network.chat.MutableComponent;

public interface ContainedText {
    String asString();

    List<String> getLines();

    List<MutableComponent> getTextLines();

    ContainedText append(final String... strings);

    ContainedText append(final MutableComponent... texts);
}