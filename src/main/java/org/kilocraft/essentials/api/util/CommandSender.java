package org.kilocraft.essentials.api.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

public class CommandSender {
    public static boolean isConsole(ServerCommandSource source) {
        boolean bool = false;
        try {
            source.getPlayer();
        } catch (CommandSyntaxException e) {
            bool = true;
        }

        return bool;
    }
}
