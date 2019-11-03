package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.user.User;

public class DevEnvironmentCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("!")
                .requires(source -> source.hasPermissionLevel(4))
                .then(
                        CommandManager.literal("test").then(
                                CommandManager.literal("User").then(
                                            CommandManager.argument("player", EntityArgumentType.player())
                                                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                                .executes(context -> testUser(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                                ).then(
                                            CommandManager.literal("set").then(
                                                    CommandManager.literal("nickname").then(
                                                                    CommandManager.argument("name", StringArgumentType.greedyString())
                                                                        .executes(context -> {
                                                                            User.of(context.getSource().getPlayer()).setNickname(StringArgumentType.getString(context, "name"));
                                                                            return 1;
                                                                        }))
                                                    ).then(
                                                            CommandManager.literal("rtpLeft").then(
                                                                        CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                                                .executes(context -> {
                                                                                    User.of(context.getSource().getPlayer()).setRTPsLeft(IntegerArgumentType.getInteger(context, "value"));
                                                                                    return 1;
                                                                                }))
                                                    )
                                            )
                                )
                        );


        dispatcher.register(argumentBuilder);
    }

    private static int testUser(ServerCommandSource source, ServerPlayerEntity target) {
        User user = User.of(target);
        String string = "&7Name: &e%s&7 DisplayName:&r %s&r &7Nickname: &6%s\n&7RandomTeleportsLeft: &6 %s\n&7LastPrivateMessageGetterUUID:&6 %s\n&7LastPrivateMessageText: &6%s";

        KiloChat.sendMessageToSource(
                source,
                new ChatMessage(
                        String.format(
                                string,
                                user.getName(),
                                user.getDisplayNameAsString(),
                                user.getNickname(),
                                user.getRTPsLeft(),
                                user.getLastPrivateMessageGetter(),
                                user.getLastPrivateMessageText()
                        ), true)
        );

        return 1;
    }
}
