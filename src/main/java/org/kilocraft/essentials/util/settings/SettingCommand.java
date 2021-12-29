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
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.settings.values.CategorySetting;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;
import org.kilocraft.essentials.util.settings.values.util.ConfigurableSetting;
import org.kilocraft.essentials.util.settings.values.util.Setting;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SettingCommand extends EssentialCommand {

    final int MAX_ENTRIES = 5;
    SuggestionProvider<CommandSourceStack> SETTINGS = (context, builder) -> {
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

        return SharedSuggestionProvider.suggest(list, builder);
    };

    SuggestionProvider<CommandSourceStack> VALUES = (context, builder) -> {
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
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> settingArgument = this.argument("setting", StringArgumentType.word());
        settingArgument.suggests(this.SETTINGS);
        settingArgument.executes(this::getValue);
        RequiredArgumentBuilder<CommandSourceStack, String> valueArgument = this.argument(ConfigurableSetting.commandArgumentValue, StringArgumentType.word());
        valueArgument.suggests(this.VALUES);
        valueArgument.executes(this::setValue);
        settingArgument.then(valueArgument);
        this.commandNode.addChild(settingArgument.build());
    }

    public int setValue(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "setting");
        Setting setting = ServerSettings.root.getSetting(id);
        if (!(setting instanceof ConfigurableSetting<?> configurableSetting))
            throw new SimpleCommandExceptionType(new TextComponent("Invalid setting id: " + id)).create();
        configurableSetting.setValueFromCommand(ctx);
        Object value = configurableSetting.getValue();
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.displayClientMessage(StringText.of("command.setting.set", setting.getFullId(), value), false);
        return SUCCESS;
    }

    public int getValue(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "setting");
        Setting setting = ServerSettings.root.getSetting(id);
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (setting instanceof AbstractSetting abstractSetting) {
            String value = "";
            if (setting instanceof ConfigurableSetting) value = ((ConfigurableSetting<?>) setting).getFormattedValue();
            player.displayClientMessage(StringText.of("command.setting.title", setting.getFullId().toUpperCase(), value), false);
            this.printRecursive(player, abstractSetting, 0);
        } else {
            throw new SimpleCommandExceptionType(new TextComponent("Invalid setting id: " + id)).create();
        }
        return 1;
    }

    private void printRecursive(ServerPlayer player, AbstractSetting setting, int depth) {
        int children = 0;
        for (AbstractSetting child : setting.getChildren()) {
            String preString = "  ".repeat(Math.max(0, depth)) + "- ";
            if (depth != 0 && children >= this.MAX_ENTRIES && setting.shouldLimitChildren()) {
                player.displayClientMessage(StringText.of("command.setting.more", preString, (setting.getChildren().size() - this.MAX_ENTRIES)), false);
                return;
            }
            TextComponent text = null;
            if (child instanceof ConfigurableSetting<?> configurableSetting) {
                text = StringText.of("command.setting.info", preString + child.getId(), configurableSetting.getFormattedValue());
            } else if (child instanceof CategorySetting) {
                text = StringText.of("command.setting.info", preString + child.getId(), "");
            }
            if (text != null) {
                text.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setting " + child.getFullId())));
                player.displayClientMessage(text, false);
            }
            this.printRecursive(player, child, depth + 1);
            children++;
        }
    }

}
