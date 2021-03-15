package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;
import org.kilocraft.essentials.util.settings.values.util.RangeSetting;

public class FloatSetting extends ConfigurableSetting<Float> implements RangeSetting<Float> {

    private Float from = Float.MIN_VALUE;
    private Float to = Float.MAX_VALUE;

    public FloatSetting(Float value, String id) {
        super(value, id);
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, Float> valueArgument() {
        return CommandManager.argument(commandArgumentValue, FloatArgumentType.floatArg(from, to));
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(FloatArgumentType.getFloat(ctx, commandArgumentValue));
    }

    @Override
    protected void setValue(CompoundTag tag) {
        tag.putFloat("value", this.getValue());
    }

    @Override
    protected Float getValue(CompoundTag tag) {
        return tag.getFloat("value");
    }

    @Override
    public FloatSetting range(Float from, Float to) {
        this.from = from;
        this.to = to;
        return this;
    }
}
