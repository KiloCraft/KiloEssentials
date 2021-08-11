package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;
import org.kilocraft.essentials.util.settings.values.util.RangeSetting;

import java.util.function.Consumer;

public class FloatSetting extends ConfigurableSetting<Float> implements RangeSetting<Float> {

    private Float from = Float.MIN_VALUE;
    private Float to = Float.MAX_VALUE;

    public FloatSetting(Float value, String id) {
        super(value, id);
    }

    @Override
    public FloatSetting onChanged(Consumer<Float> consumer) {
        return (FloatSetting) super.onChanged(consumer);
    }

    @Override
    public ArgumentType<Float> valueArgumentType() {
        return FloatArgumentType.floatArg(from, to);
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(Float.parseFloat(StringArgumentType.getString(ctx, commandArgumentValue)));
    }

    @Override
    public String getFormattedValue() {
        return "<aqua>" +  getValue() + "f";
    }

    @Override
    protected void setValue(NbtCompound tag) {
        tag.putFloat("value", this.getValue());
    }

    @Override
    protected Float getValue(NbtCompound tag) {
        return tag.getFloat("value");
    }

    @Override
    public FloatSetting range(Float from, Float to) {
        this.from = from;
        this.to = to;
        return this;
    }
}
