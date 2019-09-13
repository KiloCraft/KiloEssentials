package org.kilocraft.essentials.api;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;

public class KiloPerms {
    public static boolean testFor(ServerCommandSource source, String permissionNode) {
        boolean result = false;
        try {
            result = Thimble.PERMISSIONS.hasPermission(permissionNode, source.getPlayer().getGameProfile().getId());
        } catch (CommandSyntaxException e) {
            Mod.getLogger().error("KiloPerms: Can not find the player to check the permission, results to false");
        }

        return result;
    }
}
