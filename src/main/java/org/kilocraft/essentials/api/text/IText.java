package org.kilocraft.essentials.api.text;

import java.util.List;
import java.util.Map;

public interface IText {

    IText add(String add);

    List<String> getLines();

    List<String> getChapters();

    Map<String, Integer> getBookmarks();

}
