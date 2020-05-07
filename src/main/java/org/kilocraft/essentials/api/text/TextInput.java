package org.kilocraft.essentials.api.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextInput implements IText {
    private List<String> lines;
    private List<MutableText> textLines;

    public TextInput(String... strings) {
        this();
        this.append(strings);
    }

    public TextInput(MutableText... texts) {
        this();
        this.append(texts);
    }

    public TextInput() {
        this.lines = new ArrayList<>();
        this.textLines = new ArrayList<>();
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
    public List<MutableText> getTextLines() {
        return this.textLines;
    }

    @Override
    public IText append(String... strings) {
        lines.addAll(Arrays.asList(strings));
        return this;
    }

    @Override
    public IText append(MutableText... texts) {
        textLines.addAll(Arrays.asList(texts));
        return this;
    }
}
