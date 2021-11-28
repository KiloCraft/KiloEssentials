package org.kilocraft.essentials.util.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.StringText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Texter {
    private static final String SEPARATOR = "-----------------------------------------------------";

    public static MutableComponent newText(final String str) {
        return ComponentText.toText(str);
    }

    public static MutableComponent newText() {
        return new TextComponent("");
    }

    public static MutableComponent newRawText(final String string) {
        return new TextComponent(string);
    }

    public static MutableComponent blockStyle(MutableComponent text) {
        MutableComponent separator = new TextComponent(SEPARATOR).withStyle(ChatFormatting.GRAY);
        return new TextComponent("").append(separator).append(text).append(separator);
    }

    public static MutableComponent appendButton(MutableComponent text, MutableComponent hoverText, ClickEvent.Action action, String actionValue) {
        return text.withStyle((style) -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            style.withClickEvent(new ClickEvent(action, actionValue));
            return style;
        });
    }

    public static MutableComponent getButton(String title, String command, MutableComponent hoverText) {
        return newText(title).withStyle((style) -> style.withHoverEvent(Events.onHover(hoverText)).withClickEvent(Events.onClickRun(command)));
    }

    public static MutableComponent getButton(String title, String command, String string) {
        return newText(title).withStyle((style) -> style.withHoverEvent(Events.onHover(string)).withClickEvent(Events.onClickRun(command)));
    }

    public static MutableComponent confirmationMessage(String langKey, MutableComponent button) {
        return newText()
                .append(StringText.of(langKey))
                .append(" ")
                .append(button);
    }

    public enum TypeFormat {
        STRING("String", String.class, ChatFormatting.YELLOW),
        INTEGER("Integer", Integer.class, ChatFormatting.GOLD),
        DOUBLE("Double", Double.class, ChatFormatting.GOLD),
        FLOAT("Float", Float.class, ChatFormatting.GOLD),
        BYTE("Byte", Byte.class, ChatFormatting.RED),
        CHAR("Char", Character.class, ChatFormatting.AQUA),
        LONG("Long", Long.class, ChatFormatting.GOLD),
        BOOLEAN("Boolean", Boolean.class, ChatFormatting.GREEN),
        SHORT("Short", Short.class, ChatFormatting.GOLD),
        LIST("List", List.class, ChatFormatting.WHITE),
        MAP("Map", Map.class, ChatFormatting.WHITE);

        private final String name;
        private final Class<?> clazz;
        private final ChatFormatting defaultFormat;

        TypeFormat(String name, Class<?> clazz, ChatFormatting formatting) {
            this.name = name;
            this.clazz = clazz;
            this.defaultFormat = formatting;
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

        public String getName() {
            return this.name;
        }

        public ChatFormatting getDefaultFormatting() {
            return this.defaultFormat;
        }

    }

    public static class Legacy {
        public static MutableComponent append(MutableComponent original, MutableComponent textToAppend) {
            original.getSiblings().add(textToAppend);
            return original;
        }

        //        public static String toFormattedString(Text text) {
//            StringBuilder builder = new StringBuilder();
//            String main = "";
//
//            for (Text sibling : text.getSiblings()) {
//                String str_1 = sibling.asString();
//                if (!str_1.isEmpty()) {
//                    String str_2 = styleToString(sibling.getStyle());
//                    if (!str_2.equals(main)) {
//                        if (!main.isEmpty()) {
//                            builder.append(Formatting.RESET);
//                        }
//
//                        builder.append(str_2);
//                        main = str_2;
//                    }
//
//                    builder.append(str_1);
//                }
//            }
//
//            if (!main.isEmpty()) {
//                builder.append(Formatting.RESET);
//            }
//
//            return builder.toString();
//        }
        public static String toFormattedString(Component text) {
            return toFormattedString(text, false);
        }

        public static String toFormattedString(Component text, boolean skipSelf) {
            StringBuilder builder = new StringBuilder();
            if (!skipSelf) {
                builder.append(styleToString2(text.getStyle()));
                builder.append(text.getContents());
            }
            Style style = text.getStyle();
            for (Component sibling : text.getSiblings()) {
                if (style.equals(sibling.getStyle())) {
                    builder.append(styleToString(sibling.getStyle()));
                    builder.append(sibling.getContents());
                    builder.append(toFormattedString(sibling, true));
                } else {
                    builder.append(toFormattedString(sibling, false));
                }
            }

            return builder.toString();
        }


        public static String toFormattedString2(Component text) {
            return toFormattedString2(text, false);
        }

        public static String toFormattedString2(Component text, boolean skipSelf) {
            StringBuilder builder = new StringBuilder();
            if (!skipSelf) {
                builder.append(styleToString2(text.getStyle()));
                builder.append(text.getContents());
            }
            Style style = text.getStyle();
            for (Component sibling : text.getSiblings()) {
                if (style.equals(sibling.getStyle())) {
                    builder.append(styleToString2(sibling.getStyle()));
                    builder.append(sibling.getContents());
                    builder.append(toFormattedString2(sibling, true));
                } else {
                    builder.append(toFormattedString2(sibling, false));
                }
            }

            return builder.toString();
        }

        private static String styleToString2(Style style) {
            StringBuilder builder = new StringBuilder();
            if (style.isBold()) {
                builder.append("<bold>");
            }

            if (style.isItalic()) {
                builder.append("<italic>");
            }

            if (style.isUnderlined()) {
                builder.append("<underlined>");
            }

            if (style.isObfuscated()) {
                builder.append("<obfuscated>");
            }

            if (style.isStrikethrough()) {
                builder.append("<strikethrough>");
            }

            if (style.getColor() != null) {
                builder.append("<color:").append(style.getColor().serialize()).append(">");
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
                builder.append(ChatFormatting.BOLD);
            }

            if (style.isItalic()) {
                builder.append(ChatFormatting.ITALIC);
            }

            if (style.isUnderlined()) {
                builder.append(ChatFormatting.UNDERLINE);
            }

            if (style.isObfuscated()) {
                builder.append(ChatFormatting.OBFUSCATED);
            }

            if (style.isStrikethrough()) {
                builder.append(ChatFormatting.STRIKETHROUGH);
            }

            return builder.toString();
        }
    }

    public static class Events {
        public static ClickEvent onClickCopy(String string) {
            return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string);
        }

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

            return onClickRun("/" + builder);
        }

        public static ClickEvent onClickOpen(String url) {
            return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        }

        public static HoverEvent onHover(String text) {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentText.toText(text));
        }

        public static HoverEvent onHover(MutableComponent text) {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        }
    }

    public static class ArrayStyle {
        private final List<Object> list;
        private final ChatFormatting aFormat;
        private final ChatFormatting bFormat;
        private boolean nextColor = false;

        public ArrayStyle(ChatFormatting aFormat, ChatFormatting bFormat, @Nullable List<Object> list) {
            this.list = list == null ? new ArrayList<>() : list;
            this.aFormat = aFormat;
            this.bFormat = bFormat;
        }

        public ArrayStyle() {
            this(ChatFormatting.WHITE, ChatFormatting.GRAY, null);
        }

        public static ArrayStyle of(List<Object> list) {
            return new Texter.ArrayStyle(ChatFormatting.WHITE, ChatFormatting.GRAY, list);
        }

        public ArrayStyle append(String string) {
            this.list.add(string);
            return this;
        }

        public ArrayStyle append(Component text) {
            this.list.add(text);
            return this;
        }

        private ChatFormatting formatting() {
            this.nextColor = !this.nextColor;
            return !this.nextColor ? this.aFormat : this.bFormat;
        }

        public MutableComponent build() {
            MutableComponent text = newText();
            for (int i = 0; i < this.list.size(); i++) {
                Object obj = this.list.get(i);
                if (obj == null) {
                    text.append(newText(String.valueOf((Object) null)).withStyle(this.formatting()));
                } else if (obj instanceof Component) {
                    text.append((Component) obj);
                } else {
                    TypeFormat format = TypeFormat.getByClazz(obj.getClass());
                    text.append(newText(String.valueOf(obj)).withStyle(format == null ? this.formatting() : format.getDefaultFormatting()));
                }

                if (i != this.list.size()) {
                    text.append(" ");
                }
            }

            return text;
        }

    }

    public static class ListStyle {
        private final MutableComponent text;
        private final ChatFormatting primary;
        private final ChatFormatting aFormat;
        private final ChatFormatting bFormat;
        private final ChatFormatting borders;
        private final List<Object> list;
        private MutableComponent title;
        private int size;
        private boolean nextColor = false;

        public ListStyle(String title, ChatFormatting primary, ChatFormatting borders, ChatFormatting aFormat, ChatFormatting bFormat, @Nullable List<Object> list) {
            this.title = new TextComponent(title);
            this.text = new TextComponent("");
            this.primary = primary;
            this.aFormat = aFormat;
            this.bFormat = bFormat;
            this.borders = borders;
            this.list = list == null ? new ArrayList<>() : list;
        }

        public static ListStyle of(String title, ChatFormatting primary, ChatFormatting borders, ChatFormatting aFormat, ChatFormatting bFormat) {
            return new ListStyle(title, primary, borders, aFormat, bFormat, null);
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
            ChatFormatting formatting = this.nextColor ? this.bFormat : this.aFormat;
            MutableComponent text = obj instanceof MutableComponent ? ((MutableComponent) obj).withStyle(formatting) :
                    Texter.newText(String.valueOf(obj)).withStyle(formatting);
            if (hoverEvent != null) {
                text.withStyle((style) -> style.withHoverEvent(hoverEvent));
            }
            if (clickEvent != null) {
                text.withStyle((style) -> style.withClickEvent(clickEvent));
            }

            this.size++;
            this.nextColor = !this.nextColor;
            this.text.append(text).append(" ");
            return this;
        }

        public ListStyle setSize(int size) {
            this.size = size;
            return this;
        }

        public MutableComponent build() {
            this.title = new TextComponent("")
                    .append(new TextComponent(this.title.getString()).withStyle(this.primary))
                    .append(" ")
                    .append(new TextComponent("[").withStyle(this.borders))
                    .append(new TextComponent(String.valueOf(this.size)).withStyle(this.primary))
                    .append(new TextComponent("]: ")).withStyle(this.borders);

            if (!this.list.isEmpty()) {
                for (Object o : this.list) {
                    this.append(o);
                }
            }

            return this.title.append(this.text);
        }

    }

    public static class InfoBlockStyle {
        private final MutableComponent header;
        private final MutableComponent text;
        private final ChatFormatting primary;
        private final ChatFormatting secondary;
        private final ChatFormatting borders;
        private MutableComponent lineStarter;
        private MutableComponent valueObjectSeparator;
        private boolean useLineStarter = false;

        public InfoBlockStyle(String title, ChatFormatting primary, ChatFormatting secondary, ChatFormatting borders) {
            this.header = new TextComponent("")
                    .append(new TextComponent("- [ ").withStyle(borders))
                    .append(newText(title).withStyle(primary))
                    .append(" ] ")
                    .append(SEPARATOR.substring(ComponentText.clearFormatting(title).length() + 4))
                    .withStyle(borders);
            this.text = new TextComponent("");
            this.primary = primary;
            this.secondary = secondary;
            this.borders = borders;
            this.lineStarter = new TextComponent("- ").withStyle(ChatFormatting.DARK_GRAY);
            this.valueObjectSeparator = new TextComponent(": ").withStyle(borders);
        }

        public static InfoBlockStyle of(String title) {
            return of(title, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GRAY, false);
        }

        public static InfoBlockStyle of(String title, ChatFormatting primary, ChatFormatting secondary, ChatFormatting borders, boolean lineStarter) {
            InfoBlockStyle infoBlockStyle = new InfoBlockStyle(title, primary, secondary, borders);
            infoBlockStyle.useLineStarter = lineStarter;
            return infoBlockStyle;
        }

        public InfoBlockStyle setLineStarter(MutableComponent text) {
            if (!this.useLineStarter)
                this.useLineStarter = true;

            this.lineStarter = text;
            return this;
        }

        public InfoBlockStyle setValueObjectSeparator(MutableComponent text) {
            this.valueObjectSeparator = text;
            return this;
        }

        public InfoBlockStyle append(List<?> objects, String title) {
            MutableComponent text = newText();
            for (int i = 0; i < objects.size(); i++) {
                Object obj = objects.get(i);
                if (obj == null) {
                    this.text.append(String.valueOf((Object) null));
                } else if (obj instanceof Component) {
                    MutableComponent mutable = (MutableComponent) obj;
                    this.text.withStyle((style) -> {
                        if (mutable.getStyle().getHoverEvent() != null) {
                            style.withHoverEvent(mutable.getStyle().getHoverEvent());
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
                            .withStyle(typeFormat != null ? typeFormat.getDefaultFormatting() : this.secondary));

                    if (i != objects.size()) {
                        text.append(new TextComponent(", ").withStyle(this.borders));
                    }
                }
            }

            return this.append(title, text, false, true);
        }

        public InfoBlockStyle append(String title, String[] subTitles, Object... objects) {
            MutableComponent text = new TextComponent("");
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof Component) {
                    MutableComponent objectToText = (MutableComponent) objects[i];
                    text.append(objectToText);
                } else if (objects[i] instanceof List<?>) {
                    List<?> list = (List<?>) objects[i];
                    text.append(new TextComponent("[").withStyle(this.borders))
                            .append(new TextComponent(this.valueObjectSeparator.getString()).withStyle(this.borders));

                    for (int i1 = 0; i1 < list.size() && i1 < 6; i1++) {
                        text.append(String.valueOf(list.get(i1))).withStyle(this.secondary);

                        if (i != 6 && i != list.size()) {
                            text.append(", ").withStyle(this.borders);
                        } else {
                            text.append("...").withStyle(this.borders);
                        }
                    }

                    text.append(new TextComponent("]").withStyle(this.borders));
                } else if (subTitles[i] != null) {
                    TypeFormat typeFormat = TypeFormat.getByClazz(objects[i].getClass());

                    text.append(new TextComponent(subTitles[i]).withStyle(this.secondary))
                            .append(new TextComponent(this.valueObjectSeparator.getString()).withStyle(this.borders))
                            .append(ComponentText.toText(String.valueOf(objects[i]))
                                    .withStyle(typeFormat != null ? typeFormat.getDefaultFormatting() : this.secondary)
                            );

                    if (i != subTitles.length - 1) {
                        text.append(", ").withStyle(this.borders);
                    }
                }
            }

            return this.append(title, text, false, true);
        }

        public InfoBlockStyle append(String[] titles, Object... objects) {
            for (int i = 0; i < titles.length; i++) {
                this.append(false, false, titles[i], objects[i]).space();
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
            this.text.append(ComponentText.toText(String.valueOf(obj))
                    .withStyle(typeFormat != null ? typeFormat.getDefaultFormatting() : this.secondary));
            return this;
        }

        public InfoBlockStyle appendRaw(Object obj) {
            TypeFormat typeFormat = TypeFormat.getByClazz(obj.getClass());
            this.text.append(new TextComponent(String.valueOf(obj))
                    .withStyle(typeFormat != null ? typeFormat.getDefaultFormatting() : this.secondary));
            return this;
        }

        public InfoBlockStyle append(String title, Object obj) {
            return this.append(true, true, title, obj);
        }

        public InfoBlockStyle append(MutableComponent text) {
            this.text.append(text);
            return this;
        }

        public InfoBlockStyle append(String title, MutableComponent text) {
            return this.append(title, text, true, true);
        }

        public InfoBlockStyle append(boolean separateLine, boolean nextLine, String title, Object obj) {
            TypeFormat typeFormat = TypeFormat.getByClazz(obj.getClass());
            return this.append(
                    title,
                    ComponentText.toText(String.valueOf(obj))
                            .withStyle(typeFormat != null ? typeFormat.getDefaultFormatting() : this.secondary),
                    separateLine,
                    nextLine
            );
        }

        public InfoBlockStyle append(String title, MutableComponent text, boolean separateLine, boolean nextLine) {
            if (nextLine)
                this.text.append("\n");

            if (separateLine && this.useLineStarter)
                this.text.append(this.lineStarter);

            this.text.append(new TextComponent(title).withStyle(this.borders)).append(this.valueObjectSeparator).append(text);
            return this;
        }

        public MutableComponent build() {
            return new TextComponent("").append(this.header).append(this.text.append("\n")).append(new TextComponent(SEPARATOR).withStyle(this.borders));
        }
    }
}
