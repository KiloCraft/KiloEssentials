package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.util.RegexLib;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class DateArgument implements CommandedArgument {
    private String input;
    private boolean simple;
    private boolean isValid;
    private Date result;

    public static DateArgument complex(String input) {
        return new DateArgument(input, false);
    }

    public static DateArgument simple(String input) {
        return new DateArgument(input, true);
    }

    private DateArgument(String input, boolean isSimple) {
        this.input = input;
        this.simple = isSimple;
    }

    @Override
    public String[] getExamples() {
        return new String[]{"1h30m", "10d", "1m", "0.5y"};
    }

    @Override
    public DateArgument parse() throws CommandSyntaxException {
        String completeRegex = "^((\\d+s)?(\\d+m)?(\\d+h)?(\\d+d)?(\\d+M)?(\\d+y)?|(\\d+y)?(\\d+M)?(\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?)$";
        String simpleRegex = "^\\d+[smhdy]$";

        if (!this.input.matches((this.simple) ? simpleRegex : completeRegex)) {
            isValid = false;
            throw KiloCommands.getArgException(ArgExceptionMessageNode.TIME_ARGUMENT_INVALID, this.input).create();
        }

        isValid = true;
        this.result = thisDate();
        return this;
    }

    public Date getDate() {
        return this.result;
    }

    private Date thisDate() {
        if (!isValid)
            return null;

        Calendar calendar = Calendar.getInstance();

        char[] chars = this.input.toCharArray();
        String number = "";

        for (char value : chars) {
            try {
                System.out.println(value);
                Integer.parseInt(String.valueOf(value));
                number += value;
            } catch (NumberFormatException ignored) {
                System.out.println("V: " + value);
                addToDate(calendar, Integer.parseInt(number), value);
            }

        }

        return calendar.getTime();
    }

    private void addToDate(Calendar calendar, int amount, char dateType) {
        switch (dateType) {
            case 'y':
                calendar.add(Calendar.YEAR, amount);
            case 's':
                calendar.add(Calendar.SECOND, amount);
            case 'm':
                calendar.add(Calendar.MINUTE, amount);
            case 'h':
                calendar.add(Calendar.HOUR, amount);
            case 'd':
                calendar.add(Calendar.DAY_OF_MONTH, amount);
            case 'M':
                calendar.add(Calendar.MONTH, amount);
        }
    }

    public static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return TabCompletions.suggestAtCursor(
                Arrays.stream(new String[]{"s", "m", "h", "d", "M", "y"}).filter((it) ->
                        String.valueOf(context.getInput().charAt(TabCompletions.getPendingCursor(context))).matches(RegexLib.START_WITH_DIGITS.get())),
                context
        );
    }

}
