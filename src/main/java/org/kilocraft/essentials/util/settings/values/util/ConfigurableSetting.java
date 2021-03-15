package org.kilocraft.essentials.util.settings.values.util;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ConfigurableSetting<K> extends AbstractSetting {

    protected static String commandArgumentValue = "value";
    private K value;
    private final List<Consumer<K>> onLoad = new ArrayList<>();

    public ConfigurableSetting(K value, String id) {
        super(id);
        this.value = value;
    }

    @Override
    public void toTag(CompoundTag tag) {
        CompoundTag setting = new CompoundTag();
        setValue(setting);
        for (AbstractSetting child : children) {
            child.toTag(setting);
        }
        tag.put(id, setting);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(id)) {
            CompoundTag setting = tag.getCompound(id);
            this.setValue(getValue(setting));
            for (AbstractSetting child : children) {
                child.fromTag(setting);
            }
        }
    }

    public abstract RequiredArgumentBuilder<ServerCommandSource, K> valueArgument();

    public abstract void setValueFromCommand(CommandContext<ServerCommandSource> ctx);

    public K getValue() {
        return value;
    }

    public void setValue(K value) {
        this.value = value;
        changed();
    }

    protected abstract void setValue(CompoundTag tag);

    protected abstract K getValue(CompoundTag tag);

    void changed() {
        for (Consumer<K> consumer : onLoad) {
            consumer.accept(this.getValue());
        }
    }

    public ConfigurableSetting<K> onChanged(Consumer<K> consumer) {
        this.onLoad.add(consumer);
        return this;
    }
}
