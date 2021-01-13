package org.kilocraft.essentials.util.settings.values.util;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractSetting<K> {

    protected static String commandArgumentValue = "value";
    private static List<AbstractSetting<?>> valueList = new ArrayList<>();
    protected final String id;
    public boolean shouldGenerateCommands = false;
    private K value;
    private List<Consumer<K>> onLoad = new ArrayList<>();

    public AbstractSetting(K value, String id) {
        this.value = value;
        this.id = id;
        valueList.add(this);
    }

    public static List<AbstractSetting<?>> getValueList() {
        return valueList;
    }

    public K getValue() {
        return value;
    }

    public void setValue(K value) {
        this.value = value;
        changed();
    }

    public abstract CompoundTag toTag(CompoundTag tag);

    public void fromTag(CompoundTag tag) {
        changed();
    }

    void changed() {
        for (Consumer<K> consumer : onLoad) {
            consumer.accept(this.getValue());
        }
    }

    public String getId() {
        return id;
    }

    public AbstractSetting<K> generateCommand(boolean shouldGenerateCommands) {
        this.shouldGenerateCommands = shouldGenerateCommands;
        return this;
    }

    public AbstractSetting<K> onChanged(Consumer<K> consumer) {
        this.onLoad.add(consumer);
        return this;
    }

    public abstract RequiredArgumentBuilder<ServerCommandSource, K> valueArgument();

    public abstract void setValueFromCommand(CommandContext<ServerCommandSource> ctx);

}
