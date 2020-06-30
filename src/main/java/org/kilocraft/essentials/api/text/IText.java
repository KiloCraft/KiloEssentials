package org.kilocraft.essentials.api.text;

import net.minecraft.text.MutableText;

import java.util.List;

public interface IText {
    String asString();

    List<String> getLines();

    List<MutableText> getTextLines();

    IText append(String... strings);

    IText append(MutableText... texts);
}
