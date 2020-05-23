package org.kilocraft.essentials.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static int runCommandWithFormatting(@NotNull final ServerCommandSource src, @NotNull final String cmd) {
        String s = cmd;
        Matcher matcher = Formatting.PATTERN.matcher(cmd);
        if (matcher.find()) {
            s = Formatting.format(cmd, src);
        }

        if (s.startsWith("!")) {
            return server.execute(operatorSource(src), s.replace("!", ""));
        } else if (s.startsWith("?")) {
            return server.execute(s.replaceFirst("\\?", ""));
        } else {
            return server.execute(src, s);
        }
    }

    private static ServerCommandSource operatorSource(ServerCommandSource src) {
        return new ServerCommandSource(commandOutput(src), src.getPosition(), src.getRotation(),
                src.getWorld(), 4, src.getName(), src.getDisplayName(), src.getMinecraftServer(), src.getEntity());
    }

    private static CommandOutput commandOutput(ServerCommandSource src) {
        return new CommandOutput() {
            @Override
            public void sendSystemMessage(Text text, UUID uUID) {
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

    public enum Formatting {
        SRC_NAME("source.name", ServerCommandSource::getName),
        SRC_UUID("source.uuid", (src) -> Objects.requireNonNull(src.getEntity()).getUuid().toString());

        final String format;
        final FormatterFunction<String, ServerCommandSource> function;
        Formatting(String format, FormatterFunction<String, ServerCommandSource> function) {
            this.format = format;
            this.function = function;
        }

        public String getFormat() {
            return "${" + this.format + "}";
        }

        public static String format(@NotNull final String cmd, @NotNull final ServerCommandSource src) {
            String string = cmd;
            for (Formatting value : values()) {
                final String formatting = value.getFormat();
                if (!string.contains(value.getFormat())) {
                    continue;
                }

                String var;
                try {
                    var = value.function.accept(src);
                } catch (Exception e) {
                    var = String.valueOf(null);
                }

                string = string.replace(formatting, var);
            }

            return string;
        }

        private interface FormatterFunction<T, U> {
            T accept(U u);
        }

        public static final Pattern PATTERN = Pattern.compile("\\$\\{[a-zA-Z][a-zA-Z0-9-_.]{0,32}}");
    }

}
