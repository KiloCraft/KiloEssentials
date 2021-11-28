package org.kilocraft.essentials.extensions.warps.serverwidewarps;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.schedule.SinglePlayerScheduler;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WarpCommand {
    private static final SimpleCommandExceptionType WARP_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new TextComponent("Can not find the warp specified!"));
    private static final SimpleCommandExceptionType NO_WARPS = new SimpleCommandExceptionType(new TextComponent("There are no Warps set!"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal("warp")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.WARP));
        RequiredArgumentBuilder<CommandSourceStack, String> warpArg = argument("warp", word());
        LiteralArgumentBuilder<CommandSourceStack> listLiteral = literal("warps");

        warpArg.executes(c -> executeTeleport(c.getSource(), getString(c, "warp")));
        listLiteral.executes(c -> executeList(c.getSource()));

        warpArg.suggests(ServerWarpManager::suggestions);

        builder.then(warpArg);
        registerAdmin(builder, dispatcher);
        dispatcher.register(listLiteral);
        dispatcher.register(builder);

        registerAliases();
    }

    public static void registerAliases() {
        for (ServerWarp warp : ServerWarpManager.getWarps()) {
            if (warp.addCommand()) {
                SimpleCommandManager.register(
                        new SimpleCommand(
                                warp.getName().toLowerCase(Locale.ROOT),
                                (source, args) -> executeTeleport(source, warp.getName())
                        ).withoutArgs()
                );
            }
        }
    }

    private static void registerAdmin(LiteralArgumentBuilder<CommandSourceStack> builder, CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> aliasAdd = literal("setwarp");
        LiteralArgumentBuilder<CommandSourceStack> aliasRemove = literal("delwarp");
        RequiredArgumentBuilder<CommandSourceStack, String> removeArg = argument("warp", word());
        RequiredArgumentBuilder<CommandSourceStack, String> addArg = argument("name", word());

        aliasAdd.requires(s -> KiloCommands.hasPermission(s, CommandPermission.SETWARP));
        aliasRemove.requires(s -> KiloCommands.hasPermission(s, CommandPermission.DELWARP));

        removeArg.executes(c -> executeRemove(c.getSource(), getString(c, "warp")));
        addArg.then(argument("registerCommand", bool())
                .executes(c -> executeAdd(c.getSource(), getString(c, "name"), getBool(c, "registerCommand"))));

        removeArg.suggests(ServerWarpManager::suggestions);

        aliasAdd.then(addArg);
        aliasRemove.then(removeArg);

        dispatcher.register(aliasAdd);
        dispatcher.register(aliasRemove);
    }

    private static int executeTeleport(CommandSourceStack source, String name) throws CommandSyntaxException {
        if (!ServerWarpManager.getWarpsByName().contains(name)) {
            throw WARP_NOT_FOUND_EXCEPTION.create();
        }
        ServerWarp warp = ServerWarpManager.getWarp(name);
        OnlineUser user = KiloEssentials.getUserManager().getOnline(source);
        if (LocationUtil.isDestinationToClose(user, warp.getLocation())) {
            return IEssentialCommand.FAILED;
        }
        // TODO: Set a home for people who warp and don't have a home yet
/*        if (UserHomeHandler.isEnabled() && user.getHomesHandler().getHomes().isEmpty()) {
            Home home = new Home();
            user.getHomesHandler().addHome();
        }*/
        // Add a custom ticket to gradually preload chunks
        warp.getLocation().getWorld().getChunkSource().addRegionTicket(TicketType.create("warp", Integer::compareTo, (KiloConfig.main().server().cooldown + 1) * 20), new ChunkPos(warp.getLocation().toPos()), 1, user.asPlayer().getId()); // Lag reduction
            new SinglePlayerScheduler(user, 1, KiloConfig.main().server().cooldown, () -> {
                user.sendLangMessage("command.warp.teleport", warp.getName());
                user.saveLocation();
                try {
                    ServerWarpManager.teleport(user.asPlayer(), warp);
                } catch (CommandSyntaxException ignored) {
                    // We already have a check, which checks if the executor is a player
                }
            });
        return 1;
    }

    private static int executeList(CommandSourceStack source) throws CommandSyntaxException {
        int warpsSize = ServerWarpManager.getWarps().size();

        if (warpsSize == 0)
            throw NO_WARPS.create();

        MutableComponent text = new TextComponent("Warps").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent(" [ ").withStyle(ChatFormatting.DARK_GRAY))
                .append(new TextComponent(String.valueOf(warpsSize)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(" ]: ").withStyle(ChatFormatting.DARK_GRAY));

        int i = 0;
        boolean nextColor = false;
        for (ServerWarp warp : ServerWarpManager.getWarps()) {
            TextComponent thisWarp = new TextComponent("");
            i++;

            ChatFormatting thisFormat = nextColor ? ChatFormatting.WHITE : ChatFormatting.GRAY;

            thisWarp.append(new TextComponent(warp.getName()).withStyle((style) -> style.applyFormat(thisFormat).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new TextComponent("[i] ").withStyle(ChatFormatting.YELLOW)
                            .append(new TextComponent("Click to teleport!").withStyle(ChatFormatting.GREEN)))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/warp " + warp.getName()))));

            if (warpsSize != i)
                thisWarp.append(new TextComponent(", ").withStyle(ChatFormatting.DARK_GRAY));

            nextColor = !nextColor;

            text.append(thisWarp);
        }
        CommandSourceServerUser.of(source).sendMessage(text);
        return 1;
    }

    private static int executeAdd(CommandSourceStack source, String name, boolean addCommand) throws CommandSyntaxException {
        ServerWarpManager.addWarp(new ServerWarp(name, Vec3dLocation.of(source.getPlayerOrException()).shortDecimals(), addCommand));
        CommandSourceUser user = CommandSourceServerUser.of(source);
        user.sendLangMessage("command.warp.set", name);
        registerAliases();
        KiloCommands.updateGlobalCommandTree();
        return 1;
    }

    private static int executeRemove(CommandSourceStack source, String warp) throws CommandSyntaxException {
        ServerWarp w = ServerWarpManager.getWarp(warp);
        CommandSourceUser user = CommandSourceServerUser.of(source);
        if (w != null) {
            ServerWarpManager.removeWarp(w);
            user.sendLangMessage("command.warp.remove", warp);
        } else
            throw WARP_NOT_FOUND_EXCEPTION.create();

        return 1;
    }

}
