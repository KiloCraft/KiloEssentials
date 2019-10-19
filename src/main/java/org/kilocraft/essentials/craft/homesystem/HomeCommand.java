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
        LiteralArgumentBuilder<ServerCommandSource> homesLiteral = CommandManager.literal("homes");
        LiteralArgumentBuilder<ServerCommandSource> homeAlias = CommandManager.literal("h");
        RequiredArgumentBuilder<ServerCommandSource, String> argAdd, argRemove, argSet, argTeleport;

        argAdd = CommandManager.argument("name", StringArgumentType.string());
        argRemove = CommandManager.argument("home", StringArgumentType.string());
        argSet = CommandManager.argument("home", StringArgumentType.string());
        argTeleport = CommandManager.argument("home", StringArgumentType.string());

        sethomeLiteral.then(argSet);
        homeLiteral.then(argTeleport);
        addhomeLiteral.then(argAdd);
        sethomeLiteral.then(argSet);

        argAdd.executes(
                c -> executeAdd(
                        c.getSource(),
                        Collections.singleton(c.getSource().getPlayer().getGameProfile()),
                        StringArgumentType.getString(c, "name")
                )
        );

        argRemove.executes(
                c -> executeRemove(
                    c.getSource(),
                    Collections.singleton(c.getSource().getPlayer().getGameProfile()),
                    StringArgumentType.getString(c, "home")
                )
        );

        argTeleport.executes(
                c -> executeTeleport(
                        c.getSource(),
                        Collections.singleton(c.getSource().getPlayer().getGameProfile()),
                        StringArgumentType.getString(c, "home")
                )
        );
        homesLiteral.executes(
                c -> {return 1;}
        );


        argSet.suggests((context, builder) -> {
            return HomeManager.suggestHomesOLD.getSuggestions(context, builder);
        });
        argRemove.suggests((context, builder) -> {
            return HomeManager.suggestHomesOLD.getSuggestions(context, builder);
        });
        argAdd.suggests((context, builder) -> {
            return HomeManager.suggestHomes.getSuggestions(context, builder);
        });
        argTeleport.suggests((context, builder) -> {
            return HomeManager.suggestHomes.getSuggestions(context, builder);
        });



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
