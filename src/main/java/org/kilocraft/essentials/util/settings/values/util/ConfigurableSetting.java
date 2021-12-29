package org.kilocraft.essentials.util.settings.values.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;

public abstract class ConfigurableSetting<K> extends AbstractSetting {

    public static String commandArgumentValue = "value";
    private final List<Consumer<K>> onLoad = new ArrayList<>();
    private K value;

    public ConfigurableSetting(K value, String id) {
        super(id);
        this.value = value;
    }


    @Override
    public void toTag(CompoundTag tag) {
        CompoundTag setting = new CompoundTag();
        this.setValue(setting);
        for (AbstractSetting child : this.children) {
            child.toTag(setting);
        }
        tag.put(this.id, setting);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(this.id)) {
            CompoundTag setting = tag.getCompound(this.id);
            this.setValue(this.getValue(setting));
            for (AbstractSetting child : this.children) {
                child.fromTag(setting);
            }
        }
    }

    public abstract ArgumentType<K> valueArgumentType();

    public abstract void setValueFromCommand(CommandContext<CommandSourceStack> ctx);

    public K getValue() {
        return this.value;
    }

    public void setValue(K value) {
        this.value = value;
        this.changed();
    }

    protected abstract void setValue(CompoundTag tag);

    public abstract String getFormattedValue();

    protected abstract K getValue(CompoundTag tag);

    void changed() {
        for (Consumer<K> consumer : this.onLoad) {
            consumer.accept(this.getValue());
        }
    }

    public ConfigurableSetting<K> onChanged(Consumer<K> consumer) {
        this.onLoad.add(consumer);
        return this;
    }

}
