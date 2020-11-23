package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OnlineMessageReceptionist {

    void sendMessage(final String message);

    void sendMessage(final Text text);

    void sendMessage(@NotNull final Component component);

    void sendLangMessage(@NotNull final String key, @Nullable final Object... objects);

    int sendError(final String message);

    void sendPermissionError(@NotNull String hover);

    void sendLangError(@NotNull final String key, @Nullable final Object... objects);
}
