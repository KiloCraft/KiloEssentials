package org.kilocraft.essentials.util.settings.values.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ConfigurableSetting<K> extends AbstractSetting {

    public static String commandArgumentValue = "value";
    private final List<Consumer<K>> onLoad = new ArrayList<>();
    private K value;

    public ConfigurableSetting(K value, String id) {
        super(id);
        this.value = value;
    }


    @Override
    public void toTag(NbtCompound tag) {
        NbtCompound setting = new NbtCompound();
        setValue(setting);
        for (AbstractSetting child : children) {
            child.toTag(setting);
        }
        tag.put(id, setting);
    }

    @Override
    public void fromTag(NbtCompound tag) {
        if (tag.contains(id)) {
            NbtCompound setting = tag.getCompound(id);
            this.setValue(getValue(setting));
            for (AbstractSetting child : children) {
                child.fromTag(setting);
            }
        }
    }

    public abstract ArgumentType<K> valueArgumentType();

    public abstract void setValueFromCommand(CommandContext<ServerCommandSource> ctx);

    public K getValue() {
        return value;
    }

    public void setValue(K value) {
        this.value = value;
        changed();
    }

    protected abstract void setValue(NbtCompound tag);

    public abstract String getFormattedValue();

    protected abstract K getValue(NbtCompound tag);

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
