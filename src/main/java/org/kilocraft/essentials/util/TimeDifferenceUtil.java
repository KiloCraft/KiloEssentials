package org.kilocraft.essentials.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.kilocraft.essentials.api.ModConstants.translation;

public class TimeDifferenceUtil {
    private static final Pattern TIME_PATTERN = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
    private static final int MAX_YEARS = 100000;
    private static final String[] VALID_UNITS = new String[]{"s", "m", "h", "d"};

    public static String convertSecondsToString(int seconds, char numFormat, char typeFormat) {
        int day = seconds / (24 * 3600);
        seconds = seconds % (24 * 3600);
        int hour = seconds / 3600;
        seconds %= 3600;
        int min = seconds / 60;
        seconds %= 60;

        return ((day != 0) ? "&" + numFormat + day + "&" + typeFormat + " Days " : "") +
                ((hour != 0) ? "&" + numFormat + hour + "&" + typeFormat + " Hours " : "") +
                ((min != 0) ? "&" + numFormat + min + "&" + typeFormat + " Minutes " : "") +
                ("&" + numFormat + seconds + "&" + typeFormat + " Sec");
    }

    public static String removeTimePattern(String input) {
        return TIME_PATTERN.matcher(input).replaceFirst("").trim();
    }

    public static long parse(String time, boolean future) throws CommandSyntaxException {
        Date date = new Date();
        Matcher matcher = TIME_PATTERN.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (matcher.find()) {
            if (matcher.group() == null || matcher.group().isEmpty())
                continue;

            for (int i = 0; i < matcher.groupCount(); i++) {
                if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw KiloCommands.getException("argument.time.invalid", time).create();
            }

            if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
                years = Integer.parseInt(matcher.group(1));
            }
            if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
                months = Integer.parseInt(matcher.group(2));
            }
            if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
                weeks = Integer.parseInt(matcher.group(3));
            }
            if (matcher.group(4) != null && !matcher.group(4).isEmpty()) {
                days = Integer.parseInt(matcher.group(4));
            }
            if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
                hours = Integer.parseInt(matcher.group(5));
            }
            if (matcher.group(6) != null && !matcher.group(6).isEmpty()) {
                minutes = Integer.parseInt(matcher.group(6));
            }
            if (matcher.group(7) != null && !matcher.group(7).isEmpty()) {
                seconds = Integer.parseInt(matcher.group(7));
            }
            break;
        }

        Calendar c = new GregorianCalendar();
        if (years > 0) {
            if (years > MAX_YEARS) {
                years = MAX_YEARS;
            }
            c.add(Calendar.YEAR, years * (future ? 1 : -1));
        }
        if (months > 0) {
            c.add(Calendar.MONTH, months * (future ? 1 : -1));
        }
        if (weeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
        }
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
        }
        Calendar max = new GregorianCalendar();
        max.add(Calendar.YEAR, 10);
        if (c.after(max)) {
            return max.getTimeInMillis();
        }

        if (date.getTime() == c.getTimeInMillis()) {
            throw KiloCommands.getException("argument.time.invalid", time).create();
        }

        return c.getTimeInMillis();
    }

    static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int year = Calendar.YEAR;

        int fromYear = fromDate.get(year);
        int toYear = toDate.get(year);
        if (Math.abs(fromYear - toYear) > MAX_YEARS) {
            toDate.set(year, fromYear + (future ? MAX_YEARS : -MAX_YEARS));
        }

        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        diff--;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    public static String formatDateDiff(Date from, Date to) {
        Calendar fromC = new GregorianCalendar();
        Calendar toC = new GregorianCalendar();
        fromC.setTime(from);
        toC.setTime(to);
        return formatDateDiff(fromC, toC);
    }

    public static String formatDateDiff(long date) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        Calendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }

    public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return translation("date.now");
        }
        if (toDate.after(fromDate)) {
            future = true;
        }
        StringBuilder sb = new StringBuilder();
        int[] types = new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND};
        String[] names = new String[]{translation("date.year"), translation("date.years"),
                translation("date.month"), translation("date.months"), translation("date.day"),
                translation("date.days"), translation("date.hour"), translation("date.hours"),
                translation("date.minute"), translation("date.minutes"), translation("date.second"),
                translation("date.seconds")};
        int accuracy = 0;
        for (int i = 0; i < types.length; i++) {
            if (accuracy > 2) {
                break;
            }
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                accuracy++;
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
            }
        }
        if (sb.length() == 0) {
            return translation("date.now");
        }
        return sb.toString().trim();
    }

    public static CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String inputChar = String.valueOf(context.getInput().charAt(ArgumentSuggestions.getPendingCursor(context)));
        if (inputChar.matches("^\\d+")) {
            return ArgumentSuggestions.suggestAtCursor(VALID_UNITS, context);
        }

        return builder.buildFuture();
    }

}
