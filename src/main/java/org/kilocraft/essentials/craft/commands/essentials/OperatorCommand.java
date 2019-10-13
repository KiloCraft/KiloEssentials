package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextColor;
import org.kilocraft.essentials.api.command.PlayerSelectorArgument;

import java.util.Collection;
import java.util.Iterator;

public class OperatorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("operator")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.server.manage.operators", 2));
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> selectorArg = CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile());
        RequiredArgumentBuilder<ServerCommandSource, Boolean> boolArg = CommandManager.argument("set", BoolArgumentType.bool());
        RequiredArgumentBuilder<ServerCommandSource, Integer> levelArg = CommandManager.argument("level", IntegerArgumentType.integer(1, 4));

        boolArg.executes(
                c -> execute(
                        c.getSource(),
                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                        BoolArgumentType.getBool(c, "set"),
                        KiloServer.getServer().getVanillaServer().getOpPermissionLevel())
        );

        levelArg.executes(
                c -> execute(
                        c.getSource(),
                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                        BoolArgumentType.getBool(c, "set"),
                        IntegerArgumentType.getInteger(c, "level"))
        );

        selectorArg.suggests(PlayerSelectorArgument.getSuggestions());
        boolArg.then(levelArg);
        selectorArg.then(boolArg);
        builder.then(selectorArg);
        dispatcher.register(builder);
    }

    private static int execute(ServerCommandSource source, Collection<GameProfile> gameProfiles, boolean set, int level) {
        PlayerManager playerManager_1 = source.getMinecraftServer().getPlayerManager();
        int i = 0;
        Iterator v = gameProfiles.iterator();

        if (set)
            while(v.hasNext()) {
                GameProfile gameProfile = (GameProfile) v.next();
                ServerPlayerEntity p = source.getMinecraftServer().getPlayerManager().getPlayer(gameProfile.getId());
                source.getMinecraftServer().getPlayerManager().getOpList().add(
                        new OperatorEntry(gameProfile, level, source.getMinecraftServer().getPlayerManager().getOpList().isOp(gameProfile))
                );

                p.addChatMessage(LangText.getFormatter(true, "command.operator.announce", source.getName(), level), false);

                TextColor.sendToUniversalSource(
                        source,
                        LangText.getFormatter(true, "command.operator.success", gameProfile.getName(), level),
                        false
                );

            }

        else {


        }

        return level;
    }
}
