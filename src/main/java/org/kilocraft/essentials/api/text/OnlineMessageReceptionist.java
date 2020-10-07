package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.chat.TextMessage;

public interface OnlineMessageReceptionist extends EntityIdentifiable {
    void sendMessage(final String message);

    void sendMessage(final TextMessage message);

    void sendMessage(final Text text);

    void sendMessage(@NotNull final Component component);

    void sendLangMessage(@NotNull final String key, @Nullable final Object... objects);

    int sendError(final String message);

    void sendError(final TextMessage message);

    void sendError(final Text text);

    void sendLangError(@NotNull final String key, @Nullable final Object... objects);
}
