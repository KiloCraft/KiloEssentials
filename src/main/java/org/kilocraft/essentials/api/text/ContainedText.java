package org.kilocraft.essentials.api.text;

import net.minecraft.text.MutableText;

import java.util.List;

public interface ContainedText {
    String asString();

    List<String> getLines();

    List<MutableText> getTextLines();

    ContainedText append(String... strings);

    ContainedText append(MutableText... texts);
}
