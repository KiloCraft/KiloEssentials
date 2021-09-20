package org.kilocraft.essentials.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.chat.ChatConfigSection;
import org.kilocraft.essentials.events.ChatEvents;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessage {

    private static final int LINK_MAX_LENGTH = 20;
    private final TextComponent.Builder builder = Component.text();
    private final List<OnlineUser> pinged = new ArrayList<>();
    private final OnlineUser sender;
    private final ServerChat.Channel channel;
    private ServerChat.MentionTypes mentionType = ServerChat.MentionTypes.PUBLIC;
    private static final ChatConfigSection config = KiloConfig.main().chat();
    private static final String ITEM_REGEX = "\\[item\\]";
    private static final String URL_REGEX = "(?:https?:\\/\\/)?(?:www\\.)?([-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b)[-a-zA-Z0-9()@:%_\\+.~#?&//=]*";
    private static final String EVERYONE_REGEX = "@everyone";
    private static final String HEART_REGEX = "(<3)|♥";

    public ChatMessage(final OnlineUser sender, final String input, final ServerChat.Channel channel) {
        this.sender = sender;
        this.channel = channel;
        // Fire chat event
        ChatEvents.CHAT_MESSAGE.invoker().onChat(sender.asPlayer(), input, channel);
        this.builder.append(ComponentText.of(ConfigVariableFactory.replaceUserVariables(channel.getFormat(), sender))
                .style(style -> style.hoverEvent(HoverEvent.showText(sender.hoverEvent()))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/msg " + sender.getUsername() + " "))));
        this.builder.append(this.parse(input));
    }

    private Component parse(final String input) {
        TextComponent.Builder builder = Component.text();
        if (input == null || input.equals("")) return builder.build();
        Matcher itemMatcher = Pattern.compile(ITEM_REGEX).matcher(input);
        Matcher urlMatcher = Pattern.compile(URL_REGEX).matcher(input);
        Matcher everyoneMatcher = Pattern.compile(EVERYONE_REGEX).matcher(input);
        Matcher hearthMatcher = Pattern.compile(HEART_REGEX).matcher(input);
        if (itemMatcher.find() && this.hasPermission(EssentialPermission.CHAT_SHOW_ITEM)) {
            this.parseMatcher(builder, input, itemMatcher, s -> this.itemComponent());
        } else if (urlMatcher.find() && this.hasPermission(EssentialPermission.CHAT_URL)) {
            this.parseMatcher(builder, input, urlMatcher, s -> this.urlComponent(s, urlMatcher.group(1)));
        } else if (everyoneMatcher.find() && this.hasPermission(EssentialPermission.CHAT_PING_EVERYONE)) {
            this.parseMatcher(builder, input, everyoneMatcher, s -> this.everyoneComponent());
        } else if (hearthMatcher.find()) {
            this.parseMatcher(builder, input, hearthMatcher, s -> this.hearthComponent());
        } else {
            if (this.hasPermission(EssentialPermission.CHAT_PING_OTHER)) {
                for (final OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
                    if (!user.hasPermission(EssentialPermission.CHAT_GET_PINGED)) continue;
                    Matcher nickNameMatcher = Pattern.compile(ComponentText.clearFormatting(user.getDisplayName().toLowerCase())).matcher(input.toLowerCase());
                    Matcher userNameMatcher = Pattern.compile(user.getUsername()).matcher(input);
                    if (userNameMatcher.find()) {
                        this.parseMatcher(builder, input, userNameMatcher, s -> this.pingUserComponent(user));
                        this.pinged.add(user);
                        return builder.build();
                    } else if (nickNameMatcher.find()) {
                        this.parseMatcher(builder, input, nickNameMatcher, s -> this.pingUserComponent(user));
                        this.pinged.add(user);
                        return builder.build();
                    }
                }
            }
            builder.append(ComponentText.of(input));
        }
        return builder.build();
    }

    private boolean hasPermission(EssentialPermission permission) {
        return KiloEssentials.hasPermissionNode(this.sender.getCommandSource(), permission);
    }

    private void parseMatcher(TextComponent.Builder builder, String input, Matcher matcher, Function<String, Component> function) {
        String prefix = input.substring(0, Math.max(0, matcher.start()));
        String matched = matcher.group(0);
        String suffix = input.substring(Math.min(matcher.end(), input.length()));
        builder.append(this.parse(prefix));
        builder.append(function.apply(matched));
        builder.append(this.parse(suffix));
    }

    private Component itemComponent() {
        TextComponent.Builder builder = Component.text();
        ItemStack itemStack = this.sender.asPlayer().getMainHandStack();
        NbtCompound tag = itemStack.getNbt();
        builder.append(Component.text("["))
                .append(ComponentText.toComponent(itemStack.getName()))
                .append(Component.text("]"));
        builder.style(
                style -> style
                        .hoverEvent(
                                HoverEvent.showItem(
                                        Key.key(RegistryUtils.toIdentifier(itemStack.getItem())), 1,
                                        BinaryTagHolder.of(tag == null ? new NbtCompound().toString() : tag.toString())
                                )
                        )
        );
        return builder.build();
    }

    private Component urlComponent(final String wholeUrl, final String mainUrl) {
        TextComponent.Builder builder = Component.text();
        String shortenedUrl = mainUrl.substring(0, Math.min(mainUrl.length(), LINK_MAX_LENGTH));
        if (mainUrl.length() > LINK_MAX_LENGTH) {
            builder.content(shortenedUrl + "..." + mainUrl.substring(mainUrl.length() - 5) + " ");
        } else {
            builder.append(ComponentText.of(mainUrl));
        }
        builder.color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC, TextDecoration.UNDERLINED);
        builder.style(
                style -> style
                        .clickEvent(ClickEvent.openUrl(wholeUrl))
                        .hoverEvent(HoverEvent.showText(
                                Component.text(wholeUrl).color(NamedTextColor.WHITE)
                                        .append(Component.text("\nClick to open!").color(NamedTextColor.AQUA))
                                )
                        )
        );
        return builder.build();
    }

    private Component everyoneComponent() {
        this.pinged.addAll(KiloEssentials.getUserManager().getOnlineUsersAsList());
        this.mentionType = ServerChat.MentionTypes.EVERYONE;
        return Component.text("@everyone").color(NamedTextColor.AQUA);
    }

    private Component hearthComponent() {
        return Component.text("♥").color(NamedTextColor.RED);
    }

    private Component pingUserComponent(final OnlineUser user) {
        if (user.getNickname().isPresent()) {
            return ComponentText.of(user.getNickname().get());
        } else {
            return Component.text(user.getName()).color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC);
        }
    }

    public void send() {
        TextComponent textComponent = this.builder.build();
        for (OnlineUser user : this.pinged) {
            boolean canPing = user.getPreference(Preferences.CHAT_CHANNEL) == this.channel;
            if (config.ping().pingSound().enabled && canPing) {
                ServerChat.pingUser(user, this.mentionType);
            }
        }
        Text text = ComponentText.toText(textComponent);
        this.channel.send(text, MessageType.CHAT, this.sender.getUuid());
        KiloChat.broadCastToConsole(text.getString());
    }

}
