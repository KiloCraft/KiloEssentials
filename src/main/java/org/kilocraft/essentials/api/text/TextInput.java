package org.kilocraft.essentials.api.text;

import com.google.common.collect.Lists;
import net.minecraft.text.MutableText;

import java.util.Arrays;
import java.util.List;

public class TextInput implements ContainedText {
    private final List<String> lines;
    private final List<MutableText> textLines;

    public TextInput(String... strings) {
        this();
        this.append(strings);
    }

    public TextInput(MutableText... texts) {
        this();
        this.append(texts);
    }

    public TextInput() {
        this.lines = Lists.newArrayList();
        this.textLines = Lists.newArrayList();
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
    public ContainedText append(String... strings) {
        this.lines.addAll(Arrays.asList(strings));
        return this;
    }

    @Override
    public ContainedText append(MutableText... texts) {
        this.textLines.addAll(Arrays.asList(texts));
        return this;
    }
}