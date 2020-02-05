package org.kilocraft.essentials.extensions.warps;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WarpCommand {
    private static final SimpleCommandExceptionType WARP_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the warp specified!"));
    private static final SimpleCommandExceptionType NO_WARPS = new SimpleCommandExceptionType(new LiteralText("There are no Warps set!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("warp")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.WARP));
        RequiredArgumentBuilder<ServerCommandSource, String> warpArg = argument("warp", word());
        LiteralArgumentBuilder<ServerCommandSource> listLiteral = literal("warps");

        warpArg.executes(c -> executeTeleport(c.getSource(), getString(c, "warp")));
        listLiteral.executes(c -> executeList(c.getSource()));

        warpArg.suggests(WarpManager::suggestions);

        builder.then(warpArg);
        registerAdmin(builder, dispatcher);
        dispatcher.register(listLiteral);
        dispatcher.register(builder);

        registerAliases();
    }

    public static void registerAliases() {
        for (Warp warp : WarpManager.getWarps()) {
            if (warp.getAddCommand()) {
                SimpleCommandManager.register(
                        new SimpleCommand(
                                "warp." + warp.getName().toLowerCase(),
                                warp.getName().toLowerCase(),
                                (source, args, server) -> executeTeleport(source, warp.getName())));
            }
        }
    }

    private static void registerAdmin(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> aliasAdd = literal("setwarp");
        LiteralArgumentBuilder<ServerCommandSource> aliasRemove = literal("delwarp");
        RequiredArgumentBuilder<ServerCommandSource, String> removeArg = argument("warp", word());
        RequiredArgumentBuilder<ServerCommandSource, String> addArg = argument("name", word());

        aliasAdd.requires(s -> KiloCommands.hasPermission(s, CommandPermission.SETWARP));
        aliasRemove.requires(s -> KiloCommands.hasPermission(s, CommandPermission.DELWARP));

        removeArg.executes(c -> executeRemove(c.getSource(), getString(c, "warp")));
        addArg.then(argument("registerCommand", bool())
                .executes(c -> executeAdd(c.getSource(), getString(c, "name"), getBool(c, "registerCommand"))));

        removeArg.suggests(WarpManager::suggestions);

        aliasAdd.then(addArg);
        aliasRemove.then(removeArg);

        dispatcher.register(aliasAdd);
        dispatcher.register(aliasRemove);
    }

    private static int executeTeleport(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (!WarpManager.getWarpsByName().contains(name))
            throw WARP_NOT_FOUND_EXCEPTION.create();
            Warp warp = WarpManager.getWarp(name);

            KiloChat.sendMessageTo(source, new ChatMessage(
                    KiloConfig.messages().commands().warp().teleportTo
                            .replace("{WARP_NAME}", name),
                    true
            ));

            KiloServer.getServer().getOnlineUser(source.getPlayer()).saveLocation();
            WarpManager.teleport(source, warp);

        return 1;
    }

    private static int executeList(ServerCommandSource source) throws CommandSyntaxException {
        int warpsSize = WarpManager.getWarps().size();

        if (warpsSize == 0)
            throw NO_WARPS.create();

        Text text = new LiteralText("Warps").formatted(Formatting.GOLD)
                .append(new LiteralText(" [ ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(String.valueOf(warpsSize)).formatted(Formatting.LIGHT_PURPLE))
                .append(new LiteralText(" ]: ").formatted(Formatting.DARK_GRAY));

        int i = 0;
        boolean nextColor = false;
        for (Warp warp : WarpManager.getWarps()) {
            LiteralText thisHome = new LiteralText("");
            i++;

            Formatting thisFormat = nextColor ? Formatting.WHITE : Formatting.GRAY;

            thisHome.append(new LiteralText(warp.getName()).styled((style) -> {
                style.setColor(thisFormat);
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("[i] ").formatted(Formatting.YELLOW)
                                .append(new LiteralText("Click to teleport!").formatted(Formatting.GREEN))));
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/warp " + warp.getName()));
            }));

            if (warpsSize != i)
                thisHome.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

            nextColor = !nextColor;

            text.append(thisHome);
        }

        KiloChat.sendMessageToSource(source, text);
        return 1;
    }

    private static int executeAdd(ServerCommandSource source, String name, boolean addCommand) throws CommandSyntaxException {
        WarpManager.addWarp(new Warp(name, Vec3dLocation.of(source.getPlayer()).shortDecimals(), addCommand));

        KiloChat.sendLangMessageTo(source, "command.warp.set", name);
        registerAliases();
        KiloCommands.updateCommandTreeForEveryone();

        return 1;
    }

    private static int executeRemove(ServerCommandSource source, String warp) throws CommandSyntaxException {
        if (WarpManager.getWarpsByName().contains(warp)) {
            WarpManager.removeWarp(warp);
            KiloChat.sendLangMessageTo(source, "command.warp.remove", warp);
            registerAliases();
        }
        else
            throw WARP_NOT_FOUND_EXCEPTION.create();

        return 1;
    }

}
