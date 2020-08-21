package org.kilocraft.essentials.events.player;

import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.event.player.OnSocialSpyWarning;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.List;

public class OnSocialSpyWarningImpl implements OnSocialSpyWarning {
    private final ServerCommandSource source;
    private final OnlineUser receiver;
    private final String message;
    private final List<String> marked;

    public OnSocialSpyWarningImpl(@NotNull final ServerCommandSource source, @NotNull final OnlineUser receiver, @NotNull final String message, @NotNull final List<String> marked) {
        this.source = source;
        this.receiver = receiver;
        this.message = message;
        this.marked = marked;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public List<String> getMarked() {
        return this.marked;
    }

    @Override
    public ServerCommandSource getSource() {
        return this.source;
    }

    @Override
    public OnlineUser getReceiver() {
        return this.receiver;
    }

}
