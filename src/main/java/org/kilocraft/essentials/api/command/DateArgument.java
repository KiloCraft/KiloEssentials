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

public class DateArgument {
    private String input;
    private String type;
    private boolean simple;
    private int amount;
    private Date result;

    public static DateArgument full(String input) {
        return new DateArgument(input, false);
    }

    public static DateArgument simple(String input) {
        return new DateArgument(input, true);
    }

    private DateArgument(String input, boolean isSimple) {
        this.input = input;
        this.simple = isSimple;
    }

    public DateArgument parse() throws CommandSyntaxException {
        String simpleRegex = "^\\d+[smhd]$";
        String fullRegex = "^\\d+[smhdoY]$";

        if (!input.matches((this.simple) ? simpleRegex : fullRegex))
            throw KiloCommands.getArgException(ArgExceptionMessageNode.TIME_ARGUMENT_INVALID, this.input).create();

        this.type = this.input.replace("\\d+", "");
        this.amount = Integer.parseInt(this.input.replaceAll(RegexLib.ALL_EXCEPT_DIGITS.get(), ""));
        this.result = thisDate();

        return this;
    }

    public void renew() {
        this.result = thisDate();
    }

    public Date getDate() {
        return this.result;
    }

    public int getTimeAmount() {
        return this.amount;
    }

    public String getTimeType() {
        return this.type;
    }

    public String getTypeName() {
        switch (this.type) {
            case "s":
                return "second";
            case "m":
                return "minute";
            case "h":
                return "hour";
            case "d":
                return "day";
            case "mo":
                return "month";
            case "y":
                return "year";
            default:
                return this.type;
        }
    }

    private Date thisDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        switch (this.type) {
            case "s":
                calendar.add(Calendar.SECOND, this.amount);
            case "m":
                calendar.add(Calendar.MINUTE, this.amount);
            case "h":
                calendar.add(Calendar.HOUR, this.amount);
            case "d":
                calendar.add(Calendar.DAY_OF_MONTH, this.amount);
            case "M":
                calendar.add(Calendar.MONTH, this.amount);
            case "y":
                calendar.add(Calendar.YEAR, this.amount);
            break;
        }

        return calendar.getTime();
    }

    public static CompletableFuture<Suggestions> getSimpleSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ArgumentSuggestions.suggestAtCursor(
                Arrays.stream(new String[]{"s", "m", "h", "d"}).filter((it) ->
                        String.valueOf(context.getInput().charAt(ArgumentSuggestions.getPendingCursor(context))).matches("^\\d+")),
                context
        );
    }

    public static CompletableFuture<Suggestions> getFullSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ArgumentSuggestions.suggestAtCursor(
                Arrays.stream(new String[]{"s", "m", "h", "d", "mo", "y"}).filter((it) ->
                        String.valueOf(context.getInput().charAt(ArgumentSuggestions.getPendingCursor(context))).matches(RegexLib.DIGITS_ONLY.get())),
                context
        );
    }

}
