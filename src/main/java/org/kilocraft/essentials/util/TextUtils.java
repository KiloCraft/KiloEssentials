package org.kilocraft.essentials.util;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.chat.TextFormat;

import java.util.List;
import java.util.Map;

public class TextUtils {
    private static final String SEPARATOR = "-----------------------------------------------------";

    public static Text blockStyle(Text text) {
        Text separator = new LiteralText(SEPARATOR).formatted(Formatting.GRAY);
        return new LiteralText("").append(separator).append(text).append(separator);
    }

    public static Text appendButton(Text text, Text hoverText, ClickEvent.Action action, String actionValue) {
        return text.styled((style) -> {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            style.setClickEvent(new ClickEvent(action, actionValue));
        });
    }

    public static class InfoBlockStyle {
        private Text header;
        private Text text;
        private Formatting primary;
        private Formatting secondary;
        private Formatting borders;
        private Text lineStarter;
        private Text valueObjectSeparator;
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
                    .append(new LiteralText(title).formatted(primary))
                    .append(" ] " + SEPARATOR.substring(title.length() + 4)).formatted(borders);
            this.text = new LiteralText("");
            this.primary = primary;
            this.secondary = secondary;
            this.borders = borders;
            this.lineStarter = new LiteralText("- ").formatted(Formatting.DARK_GRAY);
            this.valueObjectSeparator = new LiteralText(": ").formatted(borders);
        }

        public InfoBlockStyle setLineStarter(Text text) {
            if (!this.useLineStarter)
                this.useLineStarter = true;

            this.lineStarter = text;
            return this;
        }

        public InfoBlockStyle setValueObjectSeparator(Text text) {
            this.valueObjectSeparator = text;
            return this;
        }

        public InfoBlockStyle append(String title, String[] subTitles, Object... objects) {
            Text text = new LiteralText("");

            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof Text) {
                    Text objectToText = (Text) objects[i];
                        text.styled((style) -> {
                            if (objectToText.getStyle().getHoverEvent() != null)
                                style.setHoverEvent(objectToText.getStyle().getHoverEvent());

                            if (objectToText.getStyle().getClickEvent() != null) {
                                style.setClickEvent(objectToText.getStyle().getClickEvent());
                            }
                        });
                }
                else if (objects[i] instanceof List<?>) {
                    List<?> list = (List<?>) objects[i];
                    int anInt = 0;
                    text.append(new LiteralText("[").formatted(borders))
                            .append(new LiteralText(valueObjectSeparator.getString()).formatted(borders));

                    for (int i1 = 0; i1 < list.size() && i1 < 6; i1++) {
                        text.append(list.get(i1).toString()).formatted(secondary);

                        if (i != 6 && i != list.size())
                            text.append(", ").formatted(borders);
                        else
                            text.append("...").formatted(borders);
                        anInt++;
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

                    if (i != subTitles.length - 1)
                        text.append(", ").formatted(borders);
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

        public InfoBlockStyle append(Text text) {
            this.text.append(text);
            return this;
        }

        public InfoBlockStyle append(String title, Object obj) {
            return this.append(true, true, title, obj);
        }

        public InfoBlockStyle append(String title, Text text) {
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

        public InfoBlockStyle append(String title, Text text, boolean separateLine, boolean nextLine) {
            if (nextLine)
                this.text.append("\n");

            if (separateLine && useLineStarter)
                this.text.append(lineStarter);

            this.text.append(new LiteralText(title).formatted(borders)).append(valueObjectSeparator).append(text);
            return this;
        }

        public Text get() {
            return new LiteralText("").append(header).append(this.text).append(new LiteralText(SEPARATOR).formatted(borders));
        }
    }

    public static class PagedStyle {

    }

    public enum TypeFormat {
        STRING("string", String.class, Formatting.YELLOW),
        INTEGER("integer", Integer.class, Formatting.GOLD),
        DOUBLE("double", Double.class, Formatting.GOLD),
        FLOAT("float", Float.class, Formatting.GOLD),
        BYTE("byte", Byte.class, Formatting.RED),
        CHAR("char", Character.class, Formatting.AQUA),
        LONG("long", Long.class, Formatting.GOLD),
        BOOLEAN("boolean", Boolean.class, Formatting.GREEN),
        SHORT("short", Short.class, Formatting.GOLD),
        LIST("list", List.class, Formatting.WHITE),
        MAP("map", Map.class, Formatting.WHITE);

        private String name;
        private Class<?> clazz;
        private Formatting defaultFormat;
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
        public static TextUtils.TypeFormat getByName(String name) {
            for (TypeFormat value : values()) {
                if (value.name.equals(name))
                    return value;
            }

            return null;
        }

        @Nullable
        public static TextUtils.TypeFormat getByClazz(Class<?> clazz) {
            for (TypeFormat value : values()) {
                if (value.clazz.equals(clazz))
                    return value;
            }

            return null;
        }

    }
}
