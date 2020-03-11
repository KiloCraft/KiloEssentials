package org.kilocraft.essentials.api.text;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PagedText {
    private static final Map<String, PagedText> cached = new HashMap<>();
    private String id;
    private List<String> lines;
    private int timeToLive;
    private TimeUnit unit;
    private Formatting main, primary, borders;
    private List<String> pages;

    public PagedText(String id) {
        this(id, 30, TimeUnit.MINUTES);
    }

    public PagedText(final String id, final int timeToLive, final TimeUnit unit) {
        this.id = id;
        this.timeToLive = timeToLive;
        this.unit = unit;
        this.lines = new ArrayList<>();
    }

    public PagedText append(final String string) {
        this.lines.add(string);
        return this;
    }

    public PagedText withFormatting(final Formatting main, final Formatting primary, final Formatting borders) {
        this.main = main;
        this.primary = primary;
        this.borders = borders;
        return this;
    }

    public PagedText build() {
        cached.put(id, this);
        return this;
    }

    public void sendPage(final ServerCommandSource source, final int page, final int linesInPage, final String title, final String command, boolean force) {
        if (!force && cached.containsKey(id)) {
            PagedText paged = cached.get(id);

            if (paged.pages != null) {
                Date lastsUntil = new Date(paged.unit.toMillis(paged.timeToLive));

                System.out.println(lastsUntil);
                System.out.println(new Date());

                if (lastsUntil.getTime() >= new Date().getTime()) {
                    this.pages = paged.pages;
                }
            }
        } else {
            this.pages = this.getPages(linesInPage);
        }

        Formatting f1 = main == null ? Formatting.GOLD : main;
        Formatting f2 = primary == null ? Formatting.YELLOW : main;
        Formatting f3 = borders == null ? Formatting.GRAY : main;
        int prevPage = page - 2;
        int thisPage = page - 1;
        int nextPage = page + 1;
        final String SEPARATOR = "-----------------------------------------------------";
        Text header =  new LiteralText("")
                .append(new LiteralText("- [ ").formatted(f3))
                .append(new LiteralText(title).formatted(f1))
                .append(" ] ")
                .append(SEPARATOR.substring(TextFormat.removeAlternateColorCodes('&', title).length() + 4))
                .formatted(f3);

        Text button_prev = new LiteralText("")
                .append(new LiteralText("<-").formatted(Formatting.WHITE, Formatting.BOLD))
                .append(" ").append(new LiteralText("Prev").formatted(f1))
                .styled((style) -> {
                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText((prevPage >= 0) ? "<<<" : "|<").formatted(f3)));
                    if (prevPage >= 0)
                        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.replace("%page%",  String.valueOf(page - 1))));
                });

        Text button_next = new LiteralText("")
                .append(new LiteralText("Next").formatted(f1))
                .append(" ").append(new LiteralText("->").formatted(Formatting.WHITE, Formatting.BOLD)).append(" ")
                .styled((style) -> {
                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText((nextPage <= pages.size()) ? ">>>" : ">|").formatted(f3)));
                    if (nextPage <= pages.size())
                        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.replace("%page%",  String.valueOf(nextPage))));
                });

        Text buttons = new LiteralText("")
                .append(new LiteralText("[ ").formatted(Formatting.GRAY))
                .append(button_prev)
                .append(" ")
                .append(
                        new LiteralText(String.valueOf(page)).formatted(Formatting.GREEN)
                                .append(new LiteralText("/").formatted(f3))
                                .append(new LiteralText(String.valueOf(pages.size())).formatted(Formatting.GREEN))
                )
                .append(" ")
                .append(button_next)
                .append(new LiteralText("] ").formatted(f3));

        Text footer = new LiteralText("- ")
                .formatted(Formatting.GRAY)
                .append(buttons).append(new LiteralText(" ------------------------------").formatted(Formatting.GRAY));

        header.append("\n").append(new LiteralText(TextFormat.translate(pages.get(thisPage)))).append("\n").append(footer);
        source.sendFeedback(header, false);
    }

    private List<String> getPages(final int limit) {
        final List<String> pages = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (String line : this.lines) {
            if (i == limit) {
                i = 0;
                pages.add(builder.toString());

                System.out.println("Page Added: \n " + builder.toString());

                builder = new StringBuilder();
                continue;
            }

            builder.append(line);
        }

        return pages;
    }

}
