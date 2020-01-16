package org.kilocraft.essentials.extensions.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VanishEditCommand {
    private static SimpleCommandExceptionType INVALID_SETTING = new SimpleCommandExceptionType(new LiteralMessage("Invalid vanish setting key!"));
    private static Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = src -> KiloCommands.hasPermission(src, CommandPermission.VANISH_SELF);
    private static Predicate<ServerCommandSource> PERMISSION_CHECK_OTHERS = src -> KiloCommands.hasPermission(src, CommandPermission.VANISH_OTHERS);
    private static String COMMAND_FORMAT = "/vanishedit %s \"%s %s show\"";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("vanishedit")
                .requires(PERMISSION_CHECK_SELF)
                .executes(ctx -> showList(ctx, ctx.getSource().getPlayer()))
                .build();

        RequiredArgumentBuilder<ServerCommandSource, String> arg = argument("arg", string())
                .executes(ctx -> set(ctx, ctx.getSource().getPlayer()));

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArg = argument("target", player())
                .suggests(TabCompletions::allPlayers)
                .requires(PERMISSION_CHECK_OTHERS)
                .executes(ctx -> showList(ctx, getPlayer(ctx, "target")));

        selectorArg.then(arg);
        rootCommand.addChild(selectorArg.build());
        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int set(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        String input = getString(ctx, "arg");
        String[] strings = input.split(" ");

        if (strings.length < 2 || !VanishSettings.isValidKey(strings[0]))
            throw INVALID_SETTING.create();

        boolean set = strings[1].equals("true");
        OnlineUser user = KiloServer.getServer().getOnlineUser(target);
        VanishSettings settings = user.getVanishHandler().getSettings();

        switch (strings[0]) {
            case "enableEventMessages":
                settings.setEnableEventMessages(set);
            case "nightVision":
                settings.setNightVision(set);
            case "showBossbar":
                settings.setShowBossbar(set);
            case "disablePrivateMessage":
                settings.setDisablePrivateMessages(set);
            case "pickupItems":
                settings.setPickupItems(set);
            case "invulnerable":
                settings.setInvulnerable(set);
            case "ignoreEvents":
                settings.setIgnoreEvents(set);
            case "canDamageOthers":
                settings.setCanDamageOthers(set);
        }


        if (strings.length >= 3 && strings[2].equals("show"))
            return showList(ctx, target);
        else {
            Text feedback = new LiteralText("Set ").append(new LiteralText(strings[0]))
                    .append(new LiteralText(" to ")).append(new LiteralText(String.valueOf(set)))
                    .formatted(set ? Formatting.GREEN : Formatting.RED);

            ctx.getSource().getPlayer().addChatMessage(feedback, true);
        }

        return 1;
    }

    private static int showList(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(target);

        Text text = new LiteralText("");
        Text header = new LiteralText("=====================").formatted(Formatting.YELLOW)
                .append(new LiteralText(" VanishEdit ").formatted(Formatting.GOLD))
                .append(new LiteralText("=====================").formatted(Formatting.YELLOW));

        Text footer = new LiteralText("====================================================").formatted(Formatting.YELLOW);

        text.append(Texts.bracketed(header));
        text.append(new LiteralText(" " + user.getFormattedDisplayname()).append(new LiteralText(": ")).formatted(Formatting.YELLOW));

        VanishSettings settings = user.getVanishHandler().getSettings();

        targetName = user.getUsername();
        Text buttons = new LiteralText("");
        buttons.append(getButton("Event Messages", "enableEventMessages", settings.isEnableEventMessages()));
        buttons.append(new LiteralText(", ").formatted(Formatting.GRAY));
        buttons.append(getButton("Night vision", "nightVision", settings.isNightVision()));
        buttons.append(new LiteralText(", ").formatted(Formatting.GRAY));
        buttons.append(getButton("Show Bossbar", "showBossbar", settings.isShowBossbar()));
        buttons.append(new LiteralText(",\n ").formatted(Formatting.GRAY));
        buttons.append(getButton("Disable PrivateMessages", "enableEventMessages", settings.isDisablePrivateMessages()));
        buttons.append(new LiteralText(", ").formatted(Formatting.GRAY));
        buttons.append(getButton("Can Pickup Items", "pickupItems", settings.isPickupItems()));
        buttons.append(new LiteralText(", ").formatted(Formatting.GRAY));
        buttons.append(getButton("Invulnerable", "invulnerable", settings.isInvulnerable()));
        buttons.append(new LiteralText(",\n ").formatted(Formatting.GRAY));
        buttons.append(getButton("Ignore Events", "ignoreEvents", settings.isIgnoreEvents()));
        buttons.append(new LiteralText(", ").formatted(Formatting.GRAY));
        buttons.append(getButton("Can Damage Others", "canDamageOthers", settings.isCanDamageOthers()));

        text.append(buttons.formatted(Formatting.RESET));
        text.append("\n");
        text.append(Texts.bracketed(footer));
        KiloChat.sendMessageTo(ctx.getSource(), text);
        return 1;
    }

    private static String targetName = "";

    private static Text getButton(String name, String key, boolean enabled) {
        Text text = new LiteralText("")
                .append(new LiteralText(name).formatted(enabled ? Formatting.GREEN : Formatting.RED));

        text.styled((style) -> {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to Toggle").formatted(Formatting.AQUA)));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(COMMAND_FORMAT, targetName, key, !enabled)));
        });

        return text.append(new LiteralText("").formatted(Formatting.RESET));
    }

}
