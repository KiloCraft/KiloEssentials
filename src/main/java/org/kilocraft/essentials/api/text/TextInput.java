package org.kilocraft.essentials.api.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextInput implements IText {
    private List<String> lines;

    public TextInput(String... strings) {
        this();
        this.append(strings);
    }

    public TextInput() {
        this.lines = new ArrayList<>();
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        for (String line : this.lines) {
            builder.append(line).append("\n");
        }
        return builder.toString();
    }

    @Override
    public List<String> getLines() {
        return this.lines;
    }

    @Override
    public IText append(String... strings) {
        lines.addAll(Arrays.asList(strings));
        return this;
    }
}
