package org.kilocraft.essentials.chat;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.text.MessageReceptionist;
import org.kilocraft.essentials.api.user.User;

import java.util.UUID;

public class UserMessageReceptionist implements MessageReceptionist {
    private String name;
    private UUID id;

    public UserMessageReceptionist(@NotNull final User user) {
        this.name = user.getUsername();
        this.id = user.getUuid();
    }

    public UserMessageReceptionist(@NotNull final String name, @NotNull final UUID uuid) {
        this.name = name;
        this.id = uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getId() {
        return this.id;
    }
}
