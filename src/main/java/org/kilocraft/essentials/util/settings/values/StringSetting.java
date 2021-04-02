package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;

public class StringSetting extends ConfigurableSetting<String> {

    private boolean quotable = false;

    public StringSetting(String value, String id) {
        super(value, id);
    }

    public StringSetting quotable() {
        quotable = true;
        return this;
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, String> valueArgument() {
        return CommandManager.argument(commandArgumentValue, quotable ? StringArgumentType.string() : StringArgumentType.word());
    }

    @Override
    public void setValueFromCommand(CommandContext<ServerCommandSource> ctx) {
        this.setValue(StringArgumentType.getString(ctx, commandArgumentValue));
    }

    @Override
    protected void setValue(NbtCompound tag) {
        tag.putString("value", this.getValue());
    }

    @Override
    protected String getValue(NbtCompound tag) {
        return tag.getString("value");
    }

}
