package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;
import org.kilocraft.essentials.util.settings.values.util.RangeSetting;

public class IntegerSetting extends ConfigurableSetting<Integer> implements RangeSetting<Integer> {

    private Integer from = Integer.MIN_VALUE;
    private Integer to = Integer.MAX_VALUE;

    public IntegerSetting(Integer value, String id) {
        super(value, id);
        this.shouldGenerateCommands = true;
    }

    @Override
    public void toTag(CompoundTag tag) {
        CompoundTag setting = new CompoundTag();
        setting.putInt("value", this.getValue());
        for (AbstractSetting child : this.children) {
            child.toTag(setting);
        }
        tag.put(id, setting);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(id)) {
            CompoundTag setting = tag.getCompound(id);
            this.setValue(setting.getInt("value"));
            for (AbstractSetting child : children) {
                child.fromTag(setting);
            }
        }
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
