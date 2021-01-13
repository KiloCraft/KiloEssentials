package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;

public class BooleanSetting extends AbstractSetting<Boolean> {

    public BooleanSetting(Boolean value, String id) {
        super(value, id);
        this.shouldGenerateCommands = true;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean(id, this.getValue());
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        if (tag.contains(id)) this.setValue(tag.getBoolean(id));
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
