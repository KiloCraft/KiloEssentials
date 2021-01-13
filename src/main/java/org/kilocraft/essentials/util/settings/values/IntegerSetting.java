package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.RangeSetting;

public class IntegerSetting extends AbstractSetting<Integer> implements RangeSetting<Integer> {

    private Integer from = Integer.MIN_VALUE;
    private Integer to = Integer.MAX_VALUE;

    public IntegerSetting(Integer value, String id) {
        super(value, id);
        this.shouldGenerateCommands = true;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putDouble(id, this.getValue());
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        if (tag.contains(id)) this.setValue(tag.getInt(id));
        super.fromTag(tag);
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, Integer> valueArgument() {
        return CommandManager.argument(commandArgumentValue, IntegerArgumentType.integer(from, to));
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(IntegerArgumentType.getInteger(ctx, commandArgumentValue));
    }

    @Override
    public IntegerSetting range(Integer from, Integer to) {
        this.from = from;
        this.to = to;
        return this;
    }
}
