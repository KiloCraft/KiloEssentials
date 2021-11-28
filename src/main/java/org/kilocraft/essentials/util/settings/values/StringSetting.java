package org.kilocraft.essentials.util.settings.values;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;

public class StringSetting extends ConfigurableSetting<String> {

    private boolean quotable = false;

    public StringSetting(String value, String id) {
        super(value, id);
    }

    public StringSetting quotable() {
        this.quotable = true;
        return this;
    }

    @Override
    public ArgumentType<String> valueArgumentType() {
        return this.quotable ? StringArgumentType.string() : StringArgumentType.word();
    }

    @Override
    public void setValueFromCommand(CommandContext<CommandSourceStack> ctx) {
        this.setValue(StringArgumentType.getString(ctx, commandArgumentValue));
    }

    @Override
    public String getFormattedValue() {
        return "<green>\"" + this.getValue() + "\"";
    }

    @Override
    protected void setValue(CompoundTag tag) {
        tag.putString("value", this.getValue());
    }

    @Override
    protected String getValue(CompoundTag tag) {
        return tag.getString("value");
    }

}
