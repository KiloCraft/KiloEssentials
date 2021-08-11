package org.kilocraft.essentials.util.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {

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
            return execute(operatorSource(src), s.replace("!", ""));
        } else if (s.startsWith("?")) {
            return execute(s.replaceFirst("\\?", ""));
        } else {
            return execute(src, s);
        }
    }

    private static int execute(ServerCommandSource source, String command) {
        return KiloEssentials.getMinecraftServer().getCommandManager().execute(source, command);
    }

  private static int execute(String command) {
        return KiloEssentials.getMinecraftServer().getCommandManager().execute(KiloEssentials.getMinecraftServer().getCommandSource(), command);
    }



    private static ServerCommandSource operatorSource(ServerCommandSource src) {
        return src.withLevel(4);
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
                if (!string.contains(formatting)) {
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
