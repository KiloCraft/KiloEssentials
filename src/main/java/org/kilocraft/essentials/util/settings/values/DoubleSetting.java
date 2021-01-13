package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.RangeSetting;

public class DoubleSetting extends AbstractSetting<Double> implements RangeSetting<Double> {

    private Double from = Double.MIN_VALUE;
    private Double to = Double.MAX_VALUE;

    public DoubleSetting(Double value, String id) {
        super(value, id);
        this.shouldGenerateCommands = true;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble(id, this.getValue());
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        if (tag.contains(id)) this.setValue(tag.getDouble(id));
        super.fromTag(tag);
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, Double> valueArgument() {
        return CommandManager.argument(commandArgumentValue, DoubleArgumentType.doubleArg(from, to));
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(DoubleArgumentType.getDouble(ctx, commandArgumentValue));
    }

    @Override
    public DoubleSetting range(Double from, Double to) {
        this.from = from;
        this.to = to;
        return this;
    }
}
