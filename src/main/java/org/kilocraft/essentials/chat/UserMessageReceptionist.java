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

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getId() {
        return this.id;
    }
}
