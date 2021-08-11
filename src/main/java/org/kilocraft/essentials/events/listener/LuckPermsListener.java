package org.kilocraft.essentials.events.listener;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.servermeta.ServerMetaManager;

public class LuckPermsListener {

    public static void register() {
        EventBus eventBus = LuckPermsProvider.get().getEventBus();
        eventBus.subscribe(NodeAddEvent.class, e -> onPermChange(e.getTarget(), e.getNode().getKey()));
        eventBus.subscribe(NodeRemoveEvent.class, e -> onPermChange(e.getTarget(), e.getNode().getKey()));
    }

    private static void onPermChange(PermissionHolder target, String node) {
        if (target instanceof User user) {
            ServerPlayerEntity player = KiloEssentials.getMinecraftServer().getPlayerManager().getPlayer(user.getUniqueId());
            if (player != null) {
                if (node.startsWith("group.")) {
                    ServerMetaManager.updateDisplayName(player);
                    ServerMetaManager.updateForAll();
                }
                KiloEssentials.getMinecraftServer().getPlayerManager().sendCommandTree(player);
            }
        }
    }

}
