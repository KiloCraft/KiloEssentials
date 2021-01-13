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
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;

import java.security.InvalidKeyException;

public class SettingCommand extends EssentialCommand {

    public SettingCommand() {
        super("setting", CommandPermission.SETTING);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            if (!setting.shouldGenerateCommands) continue;
            RequiredArgumentBuilder<ServerCommandSource, ?> value = setting.valueArgument();
            value.executes(this::setValue);
            LiteralArgumentBuilder<ServerCommandSource> id = literal(setting.getId());
            id.executes(this::getValue);
            id.then(value);
            commandNode.addChild(id.build());
        }
    }

    public int setValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String input = ctx.getInput();
        String id = input.split(" ")[1];
        try {
            AbstractSetting<?> setting = KiloEssentials.getInstance().getSettingManager().getSetting(id);
            setting.setValueFromCommand(ctx);
            Object value = setting.getValue();
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            player.sendMessage(StringText.of(true, "command.setting.set", setting.getId(), value), false);
        } catch (InvalidKeyException e) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid setting id: " + id)).create();
        }
        return SUCCESS;
    }

    public int getValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String input = ctx.getInput();
        String id = input.split(" ")[1];
        try {
            AbstractSetting<?> setting = KiloEssentials.getInstance().getSettingManager().getSetting(id);
            Object value = setting.getValue();
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            player.sendMessage(StringText.of(true, "command.setting.info", setting.getId(), value), false);
        } catch (InvalidKeyException e) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid setting id: " + id)).create();
        }
        return 1;
    }
}
