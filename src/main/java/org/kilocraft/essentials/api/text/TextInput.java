package org.kilocraft.essentials.api.text;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;

public class TextInput implements ContainedText {
    private final List<String> lines;
    private final List<MutableComponent> textLines;

    public TextInput(String... strings) {
        this();
        this.append(strings);
    }

    public TextInput(MutableComponent... texts) {
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
    public List<MutableComponent> getTextLines() {
        return this.textLines;
    }

    @Override
    public ContainedText append(String... strings) {
        this.lines.addAll(Arrays.asList(strings));
        return this;
    }

    @Override
    public ContainedText append(MutableComponent... texts) {
        this.textLines.addAll(Arrays.asList(texts));
        return this;
    }
}