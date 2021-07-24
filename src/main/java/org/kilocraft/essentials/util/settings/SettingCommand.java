package org.kilocraft.essentials.util.settings;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;

public class SettingCommand extends EssentialCommand {

    public SettingCommand() {
        super("setting", CommandPermission.SETTING);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (AbstractSetting setting : ServerSettings.root.getChildren()) {
            if (!(setting instanceof ConfigurableSetting<?> configurableSetting)) continue;
            RequiredArgumentBuilder<ServerCommandSource, ?> value = configurableSetting.valueArgument();
            value.executes(this::setValue);
            LiteralArgumentBuilder<ServerCommandSource> id = literal(setting.getID());
            id.executes(this::getValue);
            id.then(value);
            commandNode.addChild(id.build());
        }
    }

    public int setValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String input = ctx.getInput();
        String id = input.split(" ")[1];
        AbstractSetting setting = ServerSettings.root.getSetting(id);
        if (!(setting instanceof ConfigurableSetting<?> configurableSetting)) throw new SimpleCommandExceptionType(new LiteralText("Invalid setting id: " + id)).create();
        configurableSetting.setValueFromCommand(ctx);
        Object value = configurableSetting.getValue();
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.setting.set", setting.getID(), value), false);
        return SUCCESS;
    }

    public int getValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String input = ctx.getInput();
        String id = input.split(" ")[1];
        AbstractSetting setting = ServerSettings.root.getSetting(id);
        if (!(setting instanceof ConfigurableSetting)) throw new SimpleCommandExceptionType(new LiteralText("Invalid setting id: " + id)).create();
        ConfigurableSetting<?> configurableSetting = (ConfigurableSetting<?>) setting;
        Object value = configurableSetting.getValue();
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.setting.info", setting.getID(), value), false);
        return 1;
    }
}
