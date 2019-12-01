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
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DateArgument implements CommandedArgument {
    private String input;
    private boolean simple;
    private Date result;
    private HashMap<Integer, String> values;

    public static DateArgument complex(String input) {
        return new DateArgument(input, false);
    }

    public static DateArgument simple(String input) {
        return new DateArgument(input, true);
    }

    private DateArgument(String input, boolean isSimple) {
        this.input = input;
        this.simple = isSimple;
        this.values = new HashMap<>();
    }

    @Override
    public String[] getExamples() {
        return new String[]{"1h30m", "10d", "1m", "0.5y"};
    }

    @Override
    public DateArgument parse() throws CommandSyntaxException {
        String simpleRegex = "^\\d+[smhdY]$";
        String completeRegex = "\\d+[smhdy]";

        if (!this.input.matches((this.simple) ? simpleRegex : completeRegex))
            throw KiloCommands.getArgException(ArgExceptionMessageNode.TIME_ARGUMENT_INVALID, this.input).create();

        String[] strings = this.input.split("\\w");
        System.out.println(Arrays.toString(strings));

        for (String value : strings) {
            System.out.println(value);

            this.values.put(
                    Integer.parseInt(value.replaceAll(RegexLib.ALL_EXCEPT_DIGITS.get(), "")),
                    value.replaceAll(RegexLib.DIGITS.get(), "")
            );
        }

        return this;
    }

    public Date getDate() {
        return this.result;
    }

    private Date thisDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        this.values.forEach((amount, type) -> {
            addToDate(calendar, amount, type);
        });


        return calendar.getTime();
    }


    private void addToDate(Calendar calendar, int amount, String dateType) {
        switch (dateType) {
            case "s":
                calendar.add(Calendar.SECOND, amount);
            case "m":
                calendar.add(Calendar.MINUTE, amount);
            case "h":
                calendar.add(Calendar.HOUR, amount);
            case "d":
                calendar.add(Calendar.DAY_OF_MONTH, amount);
            case "mo":
                calendar.add(Calendar.MONTH, amount);
            case "y":
                calendar.add(Calendar.YEAR, amount);
            default:
                try {
                    throw KiloCommands.getArgException(ArgExceptionMessageNode.TIME_ARGUMENT_ERROR).create();
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
        }
    }

    public static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ArgumentSuggestions.suggestAtCursor(
                Arrays.stream(new String[]{"s", "m", "h", "d", "mo", "y"}).filter((it) ->
                        String.valueOf(context.getInput().charAt(ArgumentSuggestions.getPendingCursor(context))).matches(RegexLib.START_WITH_DIGITS.get())),
                context
        );
    }

}
