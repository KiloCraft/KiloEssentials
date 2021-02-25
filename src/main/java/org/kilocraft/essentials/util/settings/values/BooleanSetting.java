package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;

public class BooleanSetting extends ConfigurableSetting<Boolean> {

    public BooleanSetting(Boolean value, String id) {
        super(value, id);
        this.shouldGenerateCommands = true;
    }

    @Override
    public void toTag(CompoundTag tag) {
        CompoundTag setting = new CompoundTag();
        setting.putBoolean("value", this.getValue());
        for (AbstractSetting child : children) {
            child.toTag(setting);
        }
        tag.put(id, setting);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(id)) {
            CompoundTag setting = tag.getCompound(id);
            this.setValue(setting.getBoolean("value"));
            for (AbstractSetting child : children) {
                child.fromTag(setting);
            }
        }
        super.fromTag(tag);
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, Boolean> valueArgument() {
        return CommandManager.argument(commandArgumentValue, BoolArgumentType.bool());
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(BoolArgumentType.getBool(ctx, commandArgumentValue));
    }
}
