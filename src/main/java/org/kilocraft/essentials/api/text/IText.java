package org.kilocraft.essentials.api.text;

import net.minecraft.text.Text;

import java.util.List;

public interface IText {
    String asString();

    List<String> getLines();

    List<Text> getTextLines();

    IText append(String... strings);

    IText append(Text... texts);
}
