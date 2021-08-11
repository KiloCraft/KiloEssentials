package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;

import java.util.function.Consumer;

public class BooleanSetting extends ConfigurableSetting<Boolean> {

    public BooleanSetting(Boolean value, String id) {
        super(value, id);
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, Boolean> valueArgument() {
        return CommandManager.argument(commandArgumentValue, BoolArgumentType.bool());
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(BoolArgumentType.getBool(ctx, commandArgumentValue));
    }

    @Override
    protected void setValue(NbtCompound tag) {
        tag.putBoolean("value", this.getValue());
    }

    @Override
    protected Boolean getValue(NbtCompound tag) {
        return tag.getBoolean("value");
    }

    @Override
    public BooleanSetting onChanged(Consumer<Boolean> consumer) {
        return (BooleanSetting) super.onChanged(consumer);
    }
}
