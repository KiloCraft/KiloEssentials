package org.kilocraft.essentials.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;

public class PermissionUtil {
    private boolean luckPermsPresent;

    public PermissionUtil() {
        this.luckPermsPresent = luckPermsPresent();

        if (!luckPermsPresent) {
            KiloEssentials.getLogger().error("**** LuckPerms is not Present! Switching to vanilla operator system");
        }
    }

    public boolean hasPermission(ServerCommandSource src, String permission, int opLevel) {
        if (!luckPermsPresent) {
            return src.hasPermissionLevel(opLevel);
        }

        return testPermission(src, permission, opLevel);
    }

    private boolean testPermission(ServerCommandSource src, String perm, int op) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        try {
            ServerPlayerEntity player = src.getPlayer();
            User user = luckPerms.getUserManager().getUser(player.getUuid());

            if (user != null) {
                QueryOptions options = luckPerms.getContextManager().getQueryOptions(player);
                return user.getCachedData().getPermissionData(options).checkPermission(perm).asBoolean();
            }

        } catch (CommandSyntaxException ignored) {
        }

        return src.hasPermissionLevel(op);
    }

    private boolean luckPermsPresent() {
        try {
            LuckPermsProvider.get();
            Class.forName("net.luckperms.api.LuckPerms");
            ClassLoader.getSystemClassLoader().loadClass("net.luckperms.api.LuckPerms");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

}
