package org.kilocraft.essentials.craft.homesystem;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.commands.essentials.BackCommand;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

public class HomeCommand {

    private static final SimpleCommandExceptionType HOME_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the home specified!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> homeLiteral = CommandManager.literal("home");
        LiteralArgumentBuilder<ServerCommandSource> sethomeLiteral = CommandManager.literal("sethome");
        LiteralArgumentBuilder<ServerCommandSource> delhomeLiteral = CommandManager.literal("delhome");
        LiteralArgumentBuilder<ServerCommandSource> homesLiteral = CommandManager.literal("homes");
        RequiredArgumentBuilder<ServerCommandSource, String> argRemove, argSet, argTeleport;

        argRemove = CommandManager.argument("home", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.home", 2));
        argSet = CommandManager.argument("name", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.home", 2));
        argTeleport = CommandManager.argument("home", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.home", 2));

        argSet.executes(
                c -> executeSet(
                        c, Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        argRemove.executes(
                c -> executeRemove(
                    c, Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        argTeleport.executes(
                c -> executeTeleport(
                        c, Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        homesLiteral.executes(
                c -> {return 1;}
        );


        argSet.suggests((context, builder) -> HomeManager.suggestHomes.getSuggestions(context, builder));
        argRemove.suggests((context, builder) -> HomeManager.suggestHomes.getSuggestions(context, builder));
        argTeleport.suggests((context, builder) -> HomeManager.suggestHomes.getSuggestions(context, builder));

        argTeleport.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                    .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.home.manage", 2))
                    .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                    .executes(c -> executeTeleport(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

        delhomeLiteral.then(argRemove);
        homeLiteral.then(argTeleport);
        sethomeLiteral.then(argSet);

        dispatcher.register(homeLiteral);
        dispatcher.register(homesLiteral);
        dispatcher.register(sethomeLiteral);
        dispatcher.register(delhomeLiteral);
    }

    private static int executeSet(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "name");
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();

            HomeManager.addHome(
                    new Home(
                            gameProfile.getId(),
                            arg,
                            Double.parseDouble(decimalFormat.format(source.getPosition().x)),
                            Double.parseDouble(decimalFormat.format(source.getPosition().x)),
                            Double.parseDouble(decimalFormat.format(source.getPosition().x)),
                            source.getWorld().getDimension().getType().getRawId(),
                            Float.parseFloat(decimalFormat.format(source.getPlayer().yaw)),
                            Float.parseFloat(decimalFormat.format(source.getPlayer().pitch))
                    )
            );

            source.sendFeedback(new LiteralText("Added the home " + arg), false);

        } else
            source.sendError(new LiteralText("Only one player is allowed but the provided selectors includes more!"));


        return 1;
    }

    private static int executeRemove(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "home");


        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();

            System.out.println(HomeManager.getHomes(gameProfile.getId()));
            HomeManager.getHomes(gameProfile.getId()).forEach((home) -> {
                System.out.println(home.getOwner() + "  :  " + home.getName());
            });
            if (HomeManager.getHomes(gameProfile.getId()).contains(arg)) {
                HomeManager.removeHome(HomeManager.getHome(gameProfile.getId(), arg));
                source.sendFeedback(new LiteralText("Removed the home " + arg), false);
            } else
                throw HOME_NOT_FOUND_EXCEPTION.create();

        } else
            source.sendError(new LiteralText("Only one player is allowed but the provided selectors includes more!"));


        return 1;
    }

    private static int executeTeleport(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "home");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();

            if (HomeManager.getHomes(gameProfile.getId()).contains(arg)) {
                HomeManager.teleport(source, HomeManager.getHome(gameProfile.getId(), arg));
                BackCommand.setLocation(source.getPlayer(), new Vector3f(source.getPosition()), source.getPlayer().dimension);

                source.sendFeedback(new LiteralText("Teleporting to home " + arg), false);
            } else
                throw HOME_NOT_FOUND_EXCEPTION.create();

        } else
            source.sendError(new LiteralText("Only one player is allowed but the provided selectors includes more!"));

        return 1;
    }

}
