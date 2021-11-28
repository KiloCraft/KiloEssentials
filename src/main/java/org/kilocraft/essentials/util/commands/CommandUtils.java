package org.kilocraft.essentials.util.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {

    public static boolean isConsole(CommandSourceStack source) {
        try {
            source.getEntityOrException();
            return false;
        } catch (CommandSyntaxException e) {
            return true;
        }
    }

    public static boolean isPlayer(CommandSourceStack source) {
        try {
            source.getPlayerOrException();
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    public static void failIfConsole(CommandSourceStack source) throws CommandSyntaxException {
        source.getEntityOrException();
    }

    public static boolean isOnline(CommandSourceStack source) {
        try {
            source.getPlayerOrException();
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    public static boolean areTheSame(ServerPlayer playerEntity1, ServerPlayer playerEntity2) {
        return playerEntity1.getUUID().equals(playerEntity2.getUUID());
    }

    public static boolean areTheSame(CommandSourceStack source, ServerPlayer playerEntity) {
        return source.getTextName().equals(playerEntity.getName().getContents());
    }

    public static boolean areTheSame(CommandSourceStack source, OnlineUser user) {
        try {
            return source.getPlayerOrException().getUUID().equals(user.getUuid());
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

    public static boolean areTheSame(CommandSourceStack source, User user) {
        try {
            return source.getPlayerOrException().getUUID().equals(user.getUuid());
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }

    public static String getDisplayName(CommandSourceStack source) throws CommandSyntaxException {
        return isConsole(source) ? source.getTextName() : source.getPlayerOrException().getDisplayName().getContents();
    }

    public static int runCommandWithFormatting(@NotNull final CommandSourceStack src, @NotNull final String cmd) {
        String s = cmd;
        Matcher matcher = Formatting.PATTERN.matcher(cmd);
        if (matcher.find()) {
            s = Formatting.format(cmd, src);
        }

        if (s.startsWith("!")) {
            return execute(operatorSource(src), s.substring(1));
        } else if (s.startsWith("?")) {
            return execute(src.getServer(), s.substring(1));
        } else {
            return execute(src, s);
        }
    }

    private static int execute(CommandSourceStack source, String command) {
        return source.getServer().getCommands().performCommand(source, command);
    }

    private static int execute(MinecraftServer server, String command) {
        return execute(server.createCommandSourceStack(), command);
    }


    private static CommandSourceStack operatorSource(CommandSourceStack src) {
        return src.withPermission(4);
    }

    public enum Formatting {
        SRC_NAME("source.name", CommandSourceStack::getTextName),
        SRC_UUID("source.uuid", (src) -> Objects.requireNonNull(src.getEntity()).getUUID().toString());

        final String format;
        final FormatterFunction<String, CommandSourceStack> function;

        Formatting(String format, FormatterFunction<String, CommandSourceStack> function) {
            this.format = format;
            this.function = function;
        }

        public String getFormat() {
            return "${" + this.format + "}";
        }

        public static String format(@NotNull final String cmd, @NotNull final CommandSourceStack src) {
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
                    var = "";
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
