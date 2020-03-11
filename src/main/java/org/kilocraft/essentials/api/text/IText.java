package org.kilocraft.essentials.api.text;

import java.util.List;

public interface IText {
    String asString();

    List<String> getLines();

    IText append(String... strings);
}
