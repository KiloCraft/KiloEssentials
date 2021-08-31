package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;
import org.kilocraft.essentials.util.settings.values.util.RangeSetting;

import java.util.function.Consumer;

public class DoubleSetting extends ConfigurableSetting<Double> implements RangeSetting<Double> {

    private Double from = Double.MIN_VALUE;
    private Double to = Double.MAX_VALUE;

    public DoubleSetting(Double value, String id) {
        super(value, id);
    }

    @Override
    public String getFormattedValue() {
        return "<light_purple>" + getValue() + "d";
    }

    @Override
    public ArgumentType<Double> valueArgumentType() {
        return DoubleArgumentType.doubleArg(from, to);
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(Double.parseDouble(StringArgumentType.getString(ctx, commandArgumentValue)));
    }

    @Override
    protected void setValue(NbtCompound tag) {
        tag.putDouble("value", this.getValue());
    }

    @Override
    protected Double getValue(NbtCompound tag) {
        return tag.getDouble("value");
    }

    @Override
    public DoubleSetting onChanged(Consumer<Double> consumer) {
        return (DoubleSetting) super.onChanged(consumer);
    }

    @Override
    public DoubleSetting range(Double from, Double to) {
        this.from = from;
        this.to = to;
        return this;
    }
}
