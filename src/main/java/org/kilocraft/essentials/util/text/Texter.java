package org.kilocraft.essentials.util.text;

import net.minecraft.SharedConstants;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.text.TextFormat;

import java.util.*;

public class Texter {
    private static final String SEPARATOR = "-----------------------------------------------------";

    public static MutableText newText(final String str) {
        return new LiteralText(TextFormat.translate(str));
    }

    public static TranslatableText newTranslatable(String key, Object... objects) {
        return new TranslatableText(key, objects);
    }

    public static MutableText newText() {
        return new LiteralText("");
    }

    public static MutableText newText(@Nullable final String... strings) {
        MutableText text = newText();
        for (String string : strings) {
            text.append(string);
        }
        return text;
    }

    public static MutableText newRawText(final String string) {
        return new LiteralText(string);
    }

    public static MutableText exceptionToText(Exception e, boolean requireDevMode) {
        MutableText text = new LiteralText(e.getMessage() == null ? e.getClass().getName() : e.getMessage());

        if (!requireDevMode && SharedConstants.isDevelopment) {
            StackTraceElement[] stackTraceElements = e.getStackTrace();

            for (int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                text.append("\n\n").append(stackTraceElements[i].getMethodName())
                        .append("\n ")
                        .append(stackTraceElements[i].getFileName())
                        .append(":")
                        .append(String.valueOf(stackTraceElements[i].getLineNumber()));
            }
        }

        return text;
    }

    public static MutableText blockStyle(MutableText text) {
        MutableText separator = new LiteralText(SEPARATOR).formatted(Formatting.GRAY);
        return new LiteralText("").append(separator).append(text).append(separator);
    }

    public static MutableText appendButton(MutableText text, MutableText hoverText, ClickEvent.Action action, String actionValue) {
        return text.styled((style) -> {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            style.withClickEvent(new ClickEvent(action, actionValue));
            return style;
        });
    }

    public static MutableText getButton(String title, String command, MutableText hoverText) {
        return newText(title).styled((style) -> style.setHoverEvent(Events.onHover(hoverText)).withClickEvent(Events.onClickRun(command)));
    }

    public static MutableText getButton(String title, String command, String string) {
        return newText(title).styled((style) -> style.setHoverEvent(Events.onHover(string)).withClickEvent(Events.onClickRun(command)));
    }

    public static MutableText confirmationMessage(String langKey, MutableText button) {
        return newText()
                .append(LangText.getFormatter(true, langKey))
                .append(" ")
                .append(button);
    }

    public static class Legacy {
        public static MutableText append(MutableText original, MutableText textToAppend) {
            original.getSiblings().add(textToAppend);
            return original;
        }

        public static String toFormattedString(Text text) {
            StringBuilder builder = new StringBuilder();
            String main = "";
            for (Text sibling : text.getSiblings()) {
                String str_1 = sibling.asString();
                if (!str_1.isEmpty()) {
                    String str_2 = styleToString(sibling.getStyle());
                    if (!str_2.equals(main)) {
                        if (!main.isEmpty()) {
                            builder.append(Formatting.RESET);
                        }

                        builder.append(str_2);
                        main = str_2;
                    }

                    builder.append(str_1);
                }
            }

            if (!main.isEmpty()) {
                builder.append(Formatting.RESET);
            }

            return builder.toString();
        }

        private static String styleToString(Style style) {
            if (style.isEmpty()) {
                return style.getColor() != null ? style.getColor().toString() : "";
            }

            StringBuilder builder = new StringBuilder();
            if (style.getColor() != null) {
                builder.append(style.getColor());
            }

            if (style.isBold()) {
                builder.append(Formatting.BOLD);
            }

            if (style.isItalic()) {
                builder.append(Formatting.ITALIC);
            }

            if (style.isUnderlined()) {
                builder.append(Formatting.UNDERLINE);
            }

            if (style.isObfuscated()) {
                builder.append(Formatting.OBFUSCATED);
            }

            if (style.isStrikethrough()) {
                builder.append(Formatting.STRIKETHROUGH);
            }

            return builder.toString();
        }
    }

    public static class Events {
        public static ClickEvent onClickSuggest(String command) {
            return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
        }

        public static ClickEvent onClickRun(String command) {
            return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        }

        public static ClickEvent onClickRun(String... command) {
            StringBuilder builder = new StringBuilder();
            for (String s : command) {
                builder.append(s);
            }

            return onClickRun("/" + builder.toString());
        }

        public static ClickEvent onClickOpen(String url) {
            return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        }

        public static HoverEvent onHover(String text) {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(TextFormat.translate(text)));
        }

        public static HoverEvent onHover(MutableText text) {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        }
    }

    public static class ArrayStyle {
        private final List<Object> list;
        private boolean nextColor = false;
        private final Formatting aFormat;
        private final Formatting bFormat;

        public static ArrayStyle of(List<Object> list) {
            return new Texter.ArrayStyle(Formatting.WHITE, Formatting.GRAY, list);
        }

        public ArrayStyle(Formatting aFormat, Formatting bFormat, @Nullable List<Object> list) {
            this.list = list == null ? new ArrayList<>() : list;
            this.aFormat = aFormat;
            this.bFormat = bFormat;
        }

        public ArrayStyle() {
            this(Formatting.WHITE, Formatting.GRAY, null);
        }

        public ArrayStyle append(String string) {
            this.list.add(string);
            return this;
        }

        public ArrayStyle append(Text text) {
            this.list.add(text);
            return this;
        }

        private Formatting formatting() {
            nextColor = !nextColor;
            return !nextColor ? aFormat : bFormat;
        }

        public MutableText build() {
            MutableText text = newText();
            for (int i = 0; i < this.list.size(); i++) {
                Object obj = this.list.get(i);
                if (obj == null) {
                    text.append(newText(String.valueOf((Object) null)).formatted(formatting()));
                } else if (obj instanceof Text) {
                    text.append((Text) obj);
                } else {
                    TypeFormat format = TypeFormat.getByClazz(obj.getClass());
                    text.append(newText(String.valueOf(obj)).formatted(format == null ? formatting() : format.getDefaultFormatting()));
                }

                if (i != this.list.size()) {
                    text.append(" ");
                }
            }

            return text;
        }

    }

    public static class ListStyle {
        private MutableText title;
        private final MutableText text;
        private final Formatting primary;
        private final Formatting aFormat;
        private final Formatting bFormat;
        private final Formatting borders;
        private final List<Object> list;
        private int size;
        private boolean nextColor = false;

        public static ListStyle of(String title, Formatting primary, Formatting borders, Formatting aFormat, Formatting bFormat) {
            return new ListStyle(title, primary, borders, aFormat, bFormat, null);
        }

        public ListStyle(String title, Formatting primary, Formatting borders, Formatting aFormat, Formatting bFormat, @Nullable List<Object> list) {
            this.title = new LiteralText(title);
            this.text = new LiteralText("");
            this.primary = primary;
            this.aFormat = aFormat;
            this.bFormat = bFormat;
            this.borders = borders;
            this.list = list == null ? new ArrayList<>() : list;
        }

        public ListStyle append(Object... objects) {
            for (Object object : objects) {
                this.append(object, null, null);
            }

            return this;
        }

        public ListStyle append(@Nullable HoverEvent hoverEvent, @Nullable ClickEvent clickEvent, Object... objects) {
            for (Object object : objects) {
                this.append(object, hoverEvent, clickEvent);
            }

            return this;
        }

        public ListStyle append(Object obj, @Nullable HoverEvent hoverEvent, @Nullable ClickEvent clickEvent) {
            Formatting formatting = nextColor ? bFormat : aFormat;
            MutableText text = obj instanceof MutableText ? ((MutableText) obj).formatted(formatting) :
                    Texter.newText(String.valueOf(obj)).formatted(formatting);
            if (hoverEvent != null) {
                text.styled((style) -> style.setHoverEvent(hoverEvent));
            }
            if (clickEvent != null) {
                text.styled((style) -> style.withClickEvent(clickEvent));
            }

            this.size++;
            nextColor = !nextColor;
            this.text.append(text).append(" ");
            return this;
        }

        public ListStyle setSize(int size) {
            this.size = size;
            return this;
        }

        public MutableText build() {
            this.title = new LiteralText("")
                    .append(new LiteralText(this.title.getString()).formatted(primary))
                    .append(" ")
                    .append(new LiteralText("[").formatted(borders))
                    .append(new LiteralText(String.valueOf(this.size)).formatted(this.primary))
                    .append(new LiteralText("]: ")).formatted(borders);

            if (!this.list.isEmpty()) {
                for (Object o : this.list) {
                    this.append(o);
                }
            }

            return this.title.append(this.text);
        }

    }

    public static class InfoBlockStyle {
        private final MutableText header;
        private final MutableText text;
        private final Formatting primary;
        private final Formatting secondary;
        private final Formatting borders;
        private MutableText lineStarter;
        private MutableText valueObjectSeparator;
        private boolean useLineStarter = false;

        public static InfoBlockStyle of(String title) {
            return of(title, Formatting.GOLD, Formatting.YELLOW, Formatting.GRAY, false);
        }

        public static InfoBlockStyle of(String title, Formatting primary, Formatting secondary, Formatting borders, boolean lineStarter) {
            InfoBlockStyle infoBlockStyle = new InfoBlockStyle(title, primary, secondary, borders);
            infoBlockStyle.useLineStarter = lineStarter;
            return infoBlockStyle;
        }

        public InfoBlockStyle(String title, Formatting primary, Formatting secondary, Formatting borders) {
            this.header = new LiteralText("")
                    .append(new LiteralText("- [ ").formatted(borders))
                    .append(newText(title).formatted(primary))
                    .append(" ] ")
                    .append(SEPARATOR.substring(TextFormat.removeAlternateColorCodes('&', title).length() + 4))
                    .formatted(borders);
            this.text = new LiteralText("");
            this.primary = primary;
            this.secondary = secondary;
            this.borders = borders;
            this.lineStarter = new LiteralText("- ").formatted(Formatting.DARK_GRAY);
            this.valueObjectSeparator = new LiteralText(": ").formatted(borders);
        }

        public InfoBlockStyle setLineStarter(MutableText text) {
            if (!this.useLineStarter)
                this.useLineStarter = true;

            this.lineStarter = text;
            return this;
        }

        public InfoBlockStyle setValueObjectSeparator(MutableText text) {
            this.valueObjectSeparator = text;
            return this;
        }
        
        public InfoBlockStyle append(List<?> objects, String title) {
            MutableText text = newText();
            for (int i = 0; i < objects.size(); i++) {
                Object obj = objects.get(i);
                if (obj == null) {
                    this.text.append(String.valueOf((Object) null));
                } else if (obj instanceof Text) {
                    MutableText mutable = (MutableText) obj;
                    this.text.styled((style) -> {
                        if (mutable.getStyle().getHoverEvent() != null) {
                            style.setHoverEvent(mutable.getStyle().getHoverEvent());
                        }

                        if (mutable.getStyle().getClickEvent() != null) {
                            style.withClickEvent(mutable.getStyle().getClickEvent());
                        }
                        return style;
                    });
                    text.append(mutable);
                } else {
                    TypeFormat typeFormat = TypeFormat.getByClazz(objects.get(i).getClass());
                    text.append(Texter.newText(String.valueOf(objects.get(i)))
                            .formatted(typeFormat != null ? typeFormat.getDefaultFormatting() : secondary));

                    if (i != objects.size()) {
                        text.append(new LiteralText(", ").formatted(borders));
                    }
                }
            }

            return this.append(title, text, false, true);
        }

        public InfoBlockStyle append(String title, String[] subTitles, Object... objects) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof Text) {
                    MutableText objectToText = (MutableText) objects[i];
                    text.styled((style) -> {
                        if (objectToText.getStyle().getHoverEvent() != null) {
                            style.setHoverEvent(objectToText.getStyle().getHoverEvent());
                        }

                        if (objectToText.getStyle().getClickEvent() != null) {
                            style.withClickEvent(objectToText.getStyle().getClickEvent());
                        }
                        return style;
                    });
                } else if (objects[i] instanceof List<?>) {
                    List<?> list = (List<?>) objects[i];
                    text.append(new LiteralText("[").formatted(borders))
                            .append(new LiteralText(valueObjectSeparator.getString()).formatted(borders));

                    for (int i1 = 0; i1 < list.size() && i1 < 6; i1++) {
                        text.append(String.valueOf(list.get(i1))).formatted(secondary);

                        if (i != 6 && i != list.size()) {
                            text.append(", ").formatted(borders);
                        } else {
                            text.append("...").formatted(borders);
                        }
                    }

                    text.append(new LiteralText("]").formatted(borders));
                }
                else if (subTitles[i] != null) {
                    TypeFormat typeFormat = TypeFormat.getByClazz(objects[i].getClass());

                    text.append(new LiteralText(subTitles[i]).formatted(secondary))
                            .append(new LiteralText(valueObjectSeparator.getString()).formatted(borders))
                            .append(new LiteralText(TextFormat.translate(String.valueOf(objects[i])))
                                    .formatted(typeFormat != null ? typeFormat.getDefaultFormatting() : secondary)
                            );

                    if (i != subTitles.length - 1) {
                        text.append(", ").formatted(borders);
                    }
                }
            }

            return this.append(title, text, false, true);
        }

        public InfoBlockStyle append(String[] titles, Object... objects) {
            for (int i = 0; i < titles.length; i++) {
                append(false, false, titles[i], objects[i]).space();
            }

            return this;
        }

        public InfoBlockStyle space() {
            this.text.append(" ");
            return this;
        }

        public InfoBlockStyle newLine() {
            this.text.append("\n");
            return this;
        }

        public InfoBlockStyle append(Object obj) {
            TypeFormat typeFormat = TypeFormat.getByClazz(obj.getClass());
            this.text.append(new LiteralText(TextFormat.translate(String.valueOf(obj)))
                    .formatted(typeFormat != null ? typeFormat.getDefaultFormatting() : secondary));
            return this;
        }

        public InfoBlockStyle appendRaw(Object obj) {
            TypeFormat typeFormat = TypeFormat.getByClazz(obj.getClass());
            this.text.append(new LiteralText(String.valueOf(obj))
                    .formatted(typeFormat != null ? typeFormat.getDefaultFormatting() : secondary));
            return this;
        }

        public InfoBlockStyle append(String title, Object obj) {
            return this.append(true, true, title, obj);
        }

        public InfoBlockStyle append(MutableText text) {
            this.text.append(text);
            return this;
        }

        public InfoBlockStyle append(String title, MutableText text) {
            return this.append(title, text, true, true);
        }

        public InfoBlockStyle append(boolean separateLine, boolean nextLine, String title, Object obj) {
            TypeFormat typeFormat = TypeFormat.getByClazz(obj.getClass());
            return this.append(
                    title,
                    new LiteralText(TextFormat.translate(String.valueOf(obj)))
                            .formatted(typeFormat != null ? typeFormat.getDefaultFormatting() : secondary),
                    separateLine,
                    nextLine
            );
        }

        public InfoBlockStyle append(String title, MutableText text, boolean separateLine, boolean nextLine) {
            if (nextLine)
                this.text.append("\n");

            if (separateLine && useLineStarter)
                this.text.append(lineStarter);

            this.text.append(new LiteralText(title).formatted(borders)).append(valueObjectSeparator).append(text);
            return this;
        }

        public MutableText build() {
            return new LiteralText("").append(header).append(this.text).append(new LiteralText(SEPARATOR).formatted(borders));
        }
    }

    public enum TypeFormat {
        STRING("String", String.class, Formatting.YELLOW),
        INTEGER("Integer", Integer.class, Formatting.GOLD),
        DOUBLE("Double", Double.class, Formatting.GOLD),
        FLOAT("Float", Float.class, Formatting.GOLD),
        BYTE("Byte", Byte.class, Formatting.RED),
        CHAR("Char", Character.class, Formatting.AQUA),
        LONG("Long", Long.class, Formatting.GOLD),
        BOOLEAN("Boolean", Boolean.class, Formatting.GREEN),
        SHORT("Short", Short.class, Formatting.GOLD),
        LIST("List", List.class, Formatting.WHITE),
        MAP("Map", Map.class, Formatting.WHITE);

        private final String name;
        private final Class<?> clazz;
        private final Formatting defaultFormat;
        TypeFormat(String name, Class<?> clazz, Formatting formatting) {
            this.name = name;
            this.clazz = clazz;
            this.defaultFormat = formatting;
        }

        public String getName() {
            return name;
        }

        public Formatting getDefaultFormatting() {
            return defaultFormat;
        }

        @Nullable
        public static Texter.TypeFormat getByName(String name) {
            for (TypeFormat value : values()) {
                if (value.name.equals(name)) {
                    return value;
                }
            }

            return null;
        }

        @Nullable
        public static Texter.TypeFormat getByClazz(Class<?> clazz) {
            for (TypeFormat value : values()) {
                if (value.clazz.equals(clazz))
                    return value;
            }

            return null;
        }

    }
}
