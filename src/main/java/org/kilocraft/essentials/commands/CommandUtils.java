package org.kilocraft.essentials.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommandUtils {
    private static final Server server = KiloServer.getServer();

    public static boolean isConsole(ServerCommandSource source) {
        try {
            source.getEntityOrThrow();
            return false;
        } catch (CommandSyntaxException e) {
            return true;
        }
    }

    public static boolean isPlayer(ServerCommandSource source) {
        try {
            source.getPlayer();
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    public static void failIfConsole(ServerCommandSource source) throws CommandSyntaxException {
        source.getEntityOrThrow();
    }

    public static boolean isOnline(ServerCommandSource source) {
        try {
            source.getPlayer();
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Deprecated
    public static boolean isOnline(ServerPlayerEntity playerEntity) {
        AtomicBoolean bool = new AtomicBoolean(false);
        try {
            KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> {
                if (player == playerEntity) bool.set(true);
            });
        } catch (Exception e) {
            bool.set(false);
        }

        return bool.get();
    }

    public static boolean areTheSame(ServerPlayerEntity playerEntity1, ServerPlayerEntity playerEntity2) {
        return playerEntity1.getUuid().equals(playerEntity2.getUuid());
    }

    public static boolean areTheSame(ServerCommandSource source, ServerPlayerEntity playerEntity) {
        return source.getName().equals(playerEntity.getName().asString());
    }

    public static boolean areTheSame(ServerCommandSource source, OnlineUser user) {
        try {
            return source.getPlayer().getUuid().equals(user.getUuid());
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }

    public static boolean areTheSame(OnlineUser user1, OnlineUser user2) {
        return user1.getUsername().equals(user2.getUsername());
    }

    public static boolean areTheSame(User user1, User user2) {
        return user1.getUuid().equals(user2.getUuid());
    }

    public static boolean areTheSame(ServerCommandSource source, User user) {
        try {
            return source.getPlayer().getUuid().equals(user.getUuid());
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }

    public static String getDisplayName(ServerCommandSource source) throws CommandSyntaxException {
        return isConsole(source) ? source.getName() : source.getPlayer().getDisplayName().asString();
    }

    public static int run(ServerCommandSource src, String cmd) {
        if (cmd.startsWith("!")) {
            return server.execute(operatorSource(src), cmd.replace("!", ""));
        } else if (cmd.startsWith("?")) {
            return server.execute(cmd.replaceFirst("\\?", ""));
        } else {
            return server.execute(src, cmd);
        }
    }

    private static ServerCommandSource operatorSource(ServerCommandSource src) {
        return new ServerCommandSource(commandOutput(src), src.getPosition(), src.getRotation(),
                src.getWorld(), 4, src.getName(), src.getDisplayName(), src.getMinecraftServer(), src.getEntity());
    }

    private static CommandOutput commandOutput(ServerCommandSource src) {
        return new CommandOutput() {
            @Override
            public void sendMessage(Text text) {
                src.sendFeedback(text, false);
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return true;
            }

            @Override
            public boolean shouldTrackOutput() {
                return false;
            }

            @Override
            public boolean shouldBroadcastConsoleToOps() {
                return false;
            }
        };
    }

}
