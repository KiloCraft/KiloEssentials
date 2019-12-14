package org.kilocraft.essentials.api.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;

public interface CommandSourceUser extends OnlineUser {
    boolean isConsole();

    @Nullable OnlineUser getUser() throws CommandSyntaxException;
}
