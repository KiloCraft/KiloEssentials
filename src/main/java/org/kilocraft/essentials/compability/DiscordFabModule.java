package org.kilocraft.essentials.compability;

import com.github.hansi132.discordfab.discordbot.ChatSynchronizer;
import com.github.hansi132.discordfab.discordbot.DiscordFab;
import com.github.hansi132.discordfab.discordbot.DiscordFabMod;
import com.github.hansi132.discordfab.discordbot.api.Field;
import com.github.hansi132.discordfab.discordbot.api.events.AdvancedDiscordAlertEvent;
import com.github.hansi132.discordfab.discordbot.api.events.DiscordMessageEvent;
import com.github.hansi132.discordfab.discordbot.api.events.MinecraftMessageEvent;
import com.github.hansi132.discordfab.discordbot.util.MinecraftAvatar;
import net.minecraft.network.MessageType;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.events.ChatEvents;
import org.kilocraft.essentials.events.PunishEvents;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DiscordFabModule {

    private static final String STAFF_REPORTS_CHANNEL_ID = "staff_reports";
    private static final String STAFF_CHANNEL_ID = "staff";

    public static boolean isLoaded() {
        try {
            return DiscordFab.getInstance() != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static void registerEvents() {
        PunishEvents.BAN.register((source, victim, reason, ipBan, expiry, silent) -> {
            String title = (expiry > 0 ? "Temporary" : "Permanent") + " " + (ipBan ? "IpBan" : "Ban");
            sendStaffReport(title, source, victim, reason, expiry, Color.RED);
        });
        PunishEvents.MUTE.register((source, victim, reason, expiry, silent) -> {
            String title = (expiry > 0 ? "Temporary" : "Permanent") + " Mute";
            sendStaffReport(title, source, victim, reason, expiry, Color.YELLOW);
        });
        ChatEvents.FLAGGED_MESSAGE.register(DiscordFabModule::sendFlaggedMessageReport);
        ChatEvents.CHAT_MESSAGE.register((player, message, channel) -> {
            if (isLoaded()) MinecraftMessageEvent.EVENT.invoker().onMessage(channel.getId(), player.getUuid(), message);
        });
        if (isLoaded()) DiscordMessageEvent.EVENT.register(DiscordFabModule::handleDiscordMessage);
    }

    private static boolean handleDiscordMessage(String minecraftChannelId, String name, UUID sender, String message) {
        if (minecraftChannelId.equals(STAFF_CHANNEL_ID)) {
            // Create message (using lang) with prefix (name) "@DrexHD >> hey"
            ServerChat.Channel.STAFF.send(ComponentText.toText(ModConstants.translation("compability.discordfab.chat.staff", name, message)), MessageType.CHAT, sender);
            return true;
        } else if (minecraftChannelId.equals(DiscordFabMod.PUBLIC_CHANNEL_ID)){
            ServerChat.Channel.PUBLIC.send(ComponentText.toText(ModConstants.translation("compability.discordfab.chat.public", name, message)), MessageType.CHAT, sender);
            return true;
        } else {
            // If we return true, discordfab will see this message as handled and won't sent it to minecraft
            return KiloEssentials.getUserManager().getPunishmentManager().isMuted(sender);
        }
    }

    private static void sendStaffReport(String title, CommandSourceUser source, EntityIdentifiable victim, String reason, long expiry, Color color) {
        if (!isLoaded()) return;
        String thumbnailUrl = MinecraftAvatar.generateUrl(
                victim.getId(),
                MinecraftAvatar.RenderType.BODY,
                MinecraftAvatar.RenderType.Model.DEFAULT,
                256,
                6,
                true
        );
        Field reasonField = new Field("Reason:", reason);
        final Date expireDate = new Date(expiry + 1000);
        Field timeField = new Field("Time:", expiry > 0 ? TimeDifferenceUtil.formatDateDiff(new Date(), expireDate) : "Permanent");
        Field expiryField = new Field("Expiry:", expiry > 0 ? expireDate.toString() : "Never");
        Field nameField = new Field("Name:", victim.getName());
        Field uuidField = new Field("UUID:", victim.getId().toString());
        AdvancedDiscordAlertEvent.EVENT.invoker().onAlert(STAFF_REPORTS_CHANNEL_ID, title, null, source.getName(), ChatSynchronizer.getMCAvatarURL(source.getUuid()), thumbnailUrl, color, reasonField, timeField, expiryField, nameField, uuidField);
    }

    private static void sendFlaggedMessageReport(OnlineUser sender, final String input, final List<String> flagged) {
        if (!isLoaded() || flagged.isEmpty()) return;

        Field messageField = new Field("Message:", input);
        Field flaggedField = new Field("Flagged:", getFlaggedMessage(flagged));
        AdvancedDiscordAlertEvent.EVENT.invoker().onAlert(STAFF_REPORTS_CHANNEL_ID, "Flagged Message", null, sender.getName(), ChatSynchronizer.getMCAvatarURL(sender.getUuid()), null, Color.ORANGE, messageField, flaggedField);
    }

    private static String getFlaggedMessage(final List<String> flagged) {
        if (flagged.size() > 1) {
            final String commaSeparated = String.join(", ", flagged.subList(0, flagged.size() - 1));
            return commaSeparated + " and " + flagged.get(flagged.size() - 1);
        } else {
            return flagged.get(0);
        }
    }

}
