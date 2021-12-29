package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;

import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;

public class BooleanSetting extends ConfigurableSetting<Boolean> {

    public BooleanSetting(Boolean value, String id) {
        super(value, id);
    }

    @Override
    public ArgumentType<Boolean> valueArgumentType() {
        return BoolArgumentType.bool();
    }

    @Override
    public void setValueFromCommand(CommandContext<CommandSourceStack> ctx) {
        this.setValue(Boolean.parseBoolean(StringArgumentType.getString(ctx, commandArgumentValue)));
    }

    @Override
    public String getFormattedValue() {
        return this.getValue() ? "<green>true" : "<red>false";
    }

    @Override
    protected void setValue(CompoundTag tag) {
        tag.putBoolean("value", this.getValue());
    }

    @Override
    protected Boolean getValue(CompoundTag tag) {
        return tag.getBoolean("value");
    }

    @Override
    public BooleanSetting onChanged(Consumer<Boolean> consumer) {
        return (BooleanSetting) super.onChanged(consumer);
    }
}
