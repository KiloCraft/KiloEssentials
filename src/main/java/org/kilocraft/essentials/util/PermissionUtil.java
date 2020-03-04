package org.kilocraft.essentials.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;

public class PermissionUtil {
    private final Manager manager;

    public PermissionUtil() {
        Logger logger = (Logger) KiloEssentials.getLogger();
        logger.info("Setting up Permissions...");
        this.manager = Manager.fromString(KiloConfig.main().permissionManager());

        if (!checkPresent(manager)) {
            logger.error("**** Permission Manager is not Present! Switching to vanilla operator system");
            return;
        }

        if (manager == Manager.THIMBLE) {
            Thimble.permissionWriters.add((map, server) -> {
                for (final EssentialPermission perm : EssentialPermission.values()) {
                    map.registerPermission(perm.getNode(), PermChangeBehavior.UPDATE_COMMAND_TREE);
                }

                for (final CommandPermission perm : CommandPermission.values()) {
                    map.registerPermission(perm.getNode(), PermChangeBehavior.UPDATE_COMMAND_TREE);
                }

                for (int i = 1; i <= KiloConfig.main().homesLimit; i++) {
                    map.registerPermission(CommandPermission.HOME_LIMIT.getNode() + "." + i, PermChangeBehavior.UPDATE_COMMAND_TREE);
                }
            });
        }

        logger.info("Registered " + (CommandPermission.values().length + EssentialPermission.values().length) + " permission nodes.");
    }

    public boolean hasPermission(ServerCommandSource src, String permission, int opLevel) {
        if (manager == Manager.LUCKPERMS) {
            return fromLuckPerms(src, permission, opLevel);
        }

        if (manager == Manager.THIMBLE) {
            return fromThimble(src, permission, opLevel);
        }

        return src.hasPermissionLevel(opLevel);
    }

    private boolean fromLuckPerms(ServerCommandSource src, String perm, int op) {
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

    private boolean fromThimble(ServerCommandSource src, String perm, int op) {
        return Thimble.hasPermissionOrOp(src, perm, op);
    }

    private boolean checkPresent(Manager manager) {
        if (manager == Manager.NONE) {
            return false;
        }

        try {
            if (manager == Manager.LUCKPERMS) {
                LuckPermsProvider.get();
            }

            Class.forName(manager.getClassPath());
            ClassLoader.getSystemClassLoader().loadClass(manager.getClassPath());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Manager getManager() {
        return this.manager;
    }

    private enum Manager {
        NONE("none", ""),
        LUCKPERMS("LuckPerms", "net.luckperms.api.LuckPerms"),
        THIMBLE("Thimble", "io.github.indicode.fabric.permissions.Thimble");

        private final String name;
        private final String classPath;

        Manager(final String name, final String classPath) {
            this.name = name;
            this.classPath = classPath;
        }

        public String getName() {
            return this.name;
        }

        public String getClassPath() {
            return this.classPath;
        }

        @NotNull
        public static Manager fromString(String str) {
            for (Manager value : Manager.values()) {
                if (value.name.equalsIgnoreCase(str)) {
                    return value;
                }
            }

            return Manager.NONE;
        }
    }

}
