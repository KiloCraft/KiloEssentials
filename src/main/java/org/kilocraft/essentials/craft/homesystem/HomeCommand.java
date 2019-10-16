package org.kilocraft.essentials.craft.homesystem;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.Collections;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> homeLiteral = CommandManager.literal("home");
        LiteralArgumentBuilder<ServerCommandSource> sethomeLiteral = CommandManager.literal("sethome");
        LiteralArgumentBuilder<ServerCommandSource> delhomeLiteral = CommandManager.literal("delhome");
        LiteralArgumentBuilder<ServerCommandSource> addhomeLiteral = CommandManager.literal("addhome");
        LiteralArgumentBuilder<ServerCommandSource> homeAlias = CommandManager.literal("h");
        LiteralArgumentBuilder<ServerCommandSource> argumentList, argumentRemove, argumentAdd, argumentSet, argumentTeleport;
        RequiredArgumentBuilder<ServerCommandSource, String> secondArgumentAdd, secondArgumentRemove, secondArgumentSet, secondArgumentTeleport;

        argumentList = CommandManager.literal("list");
        argumentAdd = CommandManager.literal("add");
        argumentSet = CommandManager.literal("set");
        argumentRemove = CommandManager.literal("remove");
        argumentTeleport = CommandManager.literal("tp");

        secondArgumentTeleport = CommandManager.argument("home", StringArgumentType.string());
        secondArgumentAdd = CommandManager.argument("name", StringArgumentType.string());
        secondArgumentRemove = CommandManager.argument("home", StringArgumentType.string());
        secondArgumentSet = CommandManager.argument("home", StringArgumentType.string());

        secondArgumentAdd.executes(
                c -> executeAdd(
                        c.getSource(),
                        Collections.singleton(c.getSource().getPlayer().getGameProfile()),
                        StringArgumentType.getString(c, "name")
                )
        );

        secondArgumentRemove.executes(
                c -> executeRemove(
                    c.getSource(),
                    Collections.singleton(c.getSource().getPlayer().getGameProfile()),
                    StringArgumentType.getString(c, "home")
                )
        );

        secondArgumentTeleport.executes(
                c -> executeTeleport(
                        c.getSource(),
                        Collections.singleton(c.getSource().getPlayer().getGameProfile()),
                        StringArgumentType.getString(c, "home")
                )
        );


        secondArgumentSet.suggests((context, builder) -> {
            return HomeManager.suggestHomesOLD.getSuggestions(context, builder);
        });
        secondArgumentRemove.suggests((context, builder) -> {
            return HomeManager.suggestHomes.getSuggestions(context, builder);
        });
        secondArgumentTeleport.suggests((context, builder) -> {
            return HomeManager.suggestHomes.getSuggestions(context, builder);
        });

        argumentTeleport.then(secondArgumentTeleport);
        argumentAdd.then(secondArgumentAdd);
        argumentRemove.then(secondArgumentRemove);
        argumentSet.then(secondArgumentSet);

        homeLiteral.then(argumentRemove);
        homeLiteral.then(argumentAdd);
        homeLiteral.then(argumentSet);
        homeLiteral.then(argumentList);
        homeLiteral.then(argumentTeleport);
        homeAlias.then(secondArgumentTeleport);
        sethomeLiteral.then(secondArgumentSet);
        delhomeLiteral.then(secondArgumentRemove);
        addhomeLiteral.then(secondArgumentAdd);

        dispatcher.register(homeLiteral);
        dispatcher.register(sethomeLiteral);
        dispatcher.register(delhomeLiteral);
        dispatcher.register(homeAlias);
    }

    private static int executeAdd(ServerCommandSource source, Collection<GameProfile> gameProfiles, String name) throws CommandSyntaxException {
        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();

            HomeManager.addHome(
                    new Home(
                            gameProfile.getId(),
                            name,
                            source.getPosition().x,
                            source.getPosition().y,
                            source.getPosition().z,
                            source.getWorld().getDimension().getType().getRawId(),
                            source.getPlayer().yaw,
                            source.getPlayer().pitch
                    )
            );

            source.sendFeedback(new LiteralText("Added the home " + name), false);

        } else
            source.sendError(new LiteralText("Only one player is allowed but the provided selectors includes more!"));


        return 1;
    }

    private static int executeRemove(ServerCommandSource source, Collection<GameProfile> gameProfiles, String name) throws CommandSyntaxException {
        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            HomeManager.removeHome(HomeManager.getHome(gameProfile.getId().toString(), name));

            source.sendFeedback(new LiteralText("Removed the home " + name), false);
        } else
            source.sendError(new LiteralText("Only one player is allowed but the provided selectors includes more!"));


        return 1;
    }

    private static int executeTeleport(ServerCommandSource source, Collection<GameProfile> gameProfiles, String name) throws CommandSyntaxException {
        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            HomeManager.teleportToHome(source, HomeManager.getHome(gameProfile.getId().toString(), name));

            source.sendFeedback(new LiteralText("Teleporting to home " + name), false);
        } else
            source.sendError(new LiteralText("Only one player is allowed but the provided selectors includes more!"));

        return 1;
    }

}
