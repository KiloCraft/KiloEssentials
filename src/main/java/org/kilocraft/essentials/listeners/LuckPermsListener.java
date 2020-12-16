package org.kilocraft.essentials.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;

public class LuckPermsListener {

    public LuckPermsListener(LuckPerms api) {
        EventBus eventBus = api.getEventBus();
        eventBus.subscribe(NodeAddEvent.class, e -> onPermChange(e.getTarget(), e.getNode().getKey()));
        eventBus.subscribe(NodeRemoveEvent.class, e -> onPermChange(e.getTarget(), e.getNode().getKey()));
    }

    void onPermChange(PermissionHolder target, String node) {
        if (target instanceof User) {
            ServerPlayerEntity player = KiloEssentials.getServer().getPlayer(((User) target).getUniqueId());
            if (player != null) {
                if (node.startsWith("group.")) {
                    KiloServer.getServer().getMetaManager().updateDisplayName(player);
                }
                KiloServer.getServer().getPlayerManager().sendCommandTree(player);
            }
        }
    }

}
