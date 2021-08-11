package org.kilocraft.essentials.util.settings;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.settings.values.CategorySetting;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;
import org.kilocraft.essentials.util.settings.values.util.Setting;

import java.util.List;

public class SettingCommand extends EssentialCommand {

    final int MAX_ENTRIES = 5;
    SuggestionProvider<ServerCommandSource> SETTINGS = (context, builder) -> {
        List<String> list = Lists.newArrayList();
        String remaining = builder.getRemaining();
        char[] chars = remaining.toCharArray();
        int dotIndex = 0;
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            if (c == '.') {
                dotIndex = i;
                break;
            }
        }
        String parentSetting = remaining.substring(0, dotIndex);
        Setting parent = ServerSettings.root.getSetting(parentSetting);
        if (parent != null) {
            for (AbstractSetting child : parent.getChildren()) {
                list.add(child.getFullId());
            }
        }

        return CommandSource.suggestMatching(list, builder);
    };

    SuggestionProvider<ServerCommandSource> VALUES = (context, builder) -> {
        String id = StringArgumentType.getString(context, "setting");
        Setting setting = ServerSettings.root.getSetting(id);
        if (setting instanceof ConfigurableSetting<?> configSetting) {
            return configSetting.valueArgumentType().listSuggestions(context, builder);
        }
        return Suggestions.empty();
    };

    public SettingCommand() {
        super("setting", CommandPermission.SETTING);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> settingArgument = argument("setting", StringArgumentType.word());
        settingArgument.suggests(SETTINGS);
        settingArgument.executes(this::getValue);
        RequiredArgumentBuilder<ServerCommandSource, String> valueArgument = argument(ConfigurableSetting.commandArgumentValue, StringArgumentType.word());
        valueArgument.suggests(VALUES);
        valueArgument.executes(this::setValue);
        settingArgument.then(valueArgument);
        commandNode.addChild(settingArgument.build());
    }

    public int setValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "setting");
        Setting setting = ServerSettings.root.getSetting(id);
        if (!(setting instanceof ConfigurableSetting<?> configurableSetting))
            throw new SimpleCommandExceptionType(new LiteralText("Invalid setting id: " + id)).create();
        configurableSetting.setValueFromCommand(ctx);
        Object value = configurableSetting.getValue();
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.setting.set", setting.getFullId(), value), false);
        return SUCCESS;
    }

    public int getValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "setting");
        Setting setting = ServerSettings.root.getSetting(id);
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (setting instanceof AbstractSetting abstractSetting) {
            String value = "";
            if (setting instanceof ConfigurableSetting) value = ((ConfigurableSetting<?>) setting).getFormattedValue();
            player.sendMessage(StringText.of(true, "command.setting.title", setting.getFullId().toUpperCase(), value), false);
            printRecursive(player, abstractSetting, 0);
        } else {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid setting id: " + id)).create();
        }
        return 1;
    }

    private void printRecursive(ServerPlayerEntity player, AbstractSetting setting, int depth) {
        int children = 0;
        for (AbstractSetting child : setting.getChildren()) {
            String preString = "  ".repeat(Math.max(0, depth)) + "- ";
            if (depth != 0 && children >= MAX_ENTRIES) {
                player.sendMessage(StringText.of(true, "command.setting.more", preString, (setting.getChildren().size() - MAX_ENTRIES)), false);
                return;
            }
            LiteralText text = null;
            if (child instanceof ConfigurableSetting<?> configurableSetting) {
                text = StringText.of(true, "command.setting.info", preString + child.getId(), configurableSetting.getFormattedValue());
            } else if (child instanceof CategorySetting) {
                text = StringText.of(true, "command.setting.info", preString + child.getId(), "");
            }
            if (text != null) {
                text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setting " + child.getFullId())));
                player.sendMessage(text, false);
            }
            printRecursive(player, child, depth + 1);
            children++;
        }
    }

}
