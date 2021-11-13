package org.kilocraft.essentials.api.util.tablist;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.ChatMetaNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.WeightNode;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LuckpermsTabListData extends TabListData {

    private final Map<UUID, FakeTeam> cachedTeams = new HashMap<>();
    private static final LuckPerms API = LuckPermsProvider.get();

    public LuckpermsTabListData(KiloEssentials kiloEssentials) {
        super(kiloEssentials);
        this.registerLuckPermEvents();
    }

    private void registerLuckPermEvents() {
        EventBus eventBus = API.getEventBus();
        eventBus.subscribe(NodeAddEvent.class, event -> this.onNodesChanged(event, event.getNode()));
        eventBus.subscribe(NodeRemoveEvent.class, event -> this.onNodesChanged(event, event.getNode()));
        eventBus.subscribe(NodeClearEvent.class, event -> this.onNodesChanged(event, event.getNodes().toArray(Node[]::new)));
    }

    private void onNodesChanged(NodeMutateEvent event, Node... nodes) {
        boolean shouldUpdate = false;
        for (Node node : nodes) {
            if (
                    node instanceof InheritanceNode ||
                            node instanceof ChatMetaNode ||
                            node instanceof MetaNode ||
                            node instanceof WeightNode
            ) {
                shouldUpdate = true;
                break;
            }
        }
        if (shouldUpdate) {
            final PermissionHolder target = event.getTarget();
            if (target instanceof Group group) {
                this.getOnlineUsersInGroup(group.getName()).thenAccept(users -> {
                    for (User user : users) {
                        this.onChange(user);
                    }
                });
            } else if (target instanceof User user) {
                this.onChange(user);
            }
        }
    }

    private void onChange(User user) {
        UUID uuid = user.getUniqueId();
        final FakeTeam fakeTeam = this.cachedTeams.get(uuid);
        if (fakeTeam != null) {
            this.sendPacketToAll(TeamS2CPacket.updateRemovedTeam(fakeTeam));
        }
        final ServerPlayerEntity player = KiloEssentials.getMinecraftServer().getPlayerManager().getPlayer(uuid);
        if (Objects.nonNull(player)) {
            this.createFakeTeam(player).thenAccept(newFakeTeam -> {
                this.cachedTeams.put(player.getUuid(), newFakeTeam);
                this.sendPacketToAll(TeamS2CPacket.updateTeam(newFakeTeam, true));
            });
        }
    }

    private CompletableFuture<List<User>> getOnlineUsersInGroup(String groupName) {
        NodeMatcher<InheritanceNode> matcher = NodeMatcher.key(InheritanceNode.builder(groupName).build());
        return API.getUserManager().searchAll(matcher).thenApply(results -> results.keySet().stream()
                .map(uuid -> API.getUserManager().getUser(uuid))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private void sendTeamList(ServerPlayerEntity player) {
        for (FakeTeam cachedTeam : this.cachedTeams.values()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(cachedTeam, true));
        }
    }

    private long getGroupWeight(Group group) {
        return group.getWeight().isPresent() ? group.getWeight().getAsInt() : 0;
    }

    private CompletableFuture<FakeTeam> createFakeTeam(ServerPlayerEntity player) {
        CompletableFuture<FakeTeam> fakeTeamFuture = new CompletableFuture<>();
        this.getLuckPermsUser(player.getUuid()).thenAccept(user -> {
            this.getLuckPermsGroup(user.getPrimaryGroup()).thenAccept(group -> {
                final long weight = this.getGroupWeight(group);
                String name = String.format("%016d%s", KiloConfig.main().playerList().topToBottom ? weight : 1000000000000000L - weight, user.getUsername());
                FakeTeam fakeTeam = new FakeTeam(name);
                this.configureTeam(fakeTeam, user, player);
                fakeTeamFuture.complete(fakeTeam);
            });
        });
        return fakeTeamFuture;
    }

    private void configureTeam(FakeTeam fakeTeam, User user, ServerPlayerEntity player) {
        fakeTeam.setShowFriendlyInvisibles(false);
        final CachedMetaData metaData = user.getCachedData().getMetaData();
        final String color = metaData.getMetaValue("color");
        if (color != null) {
            Formatting formatting = Formatting.byName(color);
            if (formatting != null) fakeTeam.setColor(formatting);
        }
        fakeTeam.setPrefix(ComponentText.toText(Objects.requireNonNullElse(metaData.getPrefix(), "")));
        fakeTeam.setSuffix(ComponentText.toText(Objects.requireNonNullElse(metaData.getSuffix(), "")));
        fakeTeam.getPlayerList().add(player.getEntityName());
    }

    private CompletableFuture<Group> getGroupForUser(UUID uuid) {
        CompletableFuture<Group> luckPermsGroupFuture = new CompletableFuture<>();
        this.getLuckPermsUser(uuid).thenAccept(luckPermsUser -> {
            this.getLuckPermsGroup(luckPermsUser.getPrimaryGroup()).thenAccept(luckPermsGroupFuture::complete);
        });
        return luckPermsGroupFuture;
    }

    private CompletableFuture<User> getLuckPermsUser(UUID uuid) {
        UserManager userManager = API.getUserManager();
        CompletableFuture<User> luckPermsUserFuture = new CompletableFuture<>();
        if (!userManager.isLoaded(uuid)) {
            userManager.loadUser(uuid).thenAccept(luckPermsUserFuture::complete);
        } else {
            luckPermsUserFuture.complete(userManager.getUser(uuid));
        }
        return luckPermsUserFuture;
    }

    private CompletableFuture<Group> getLuckPermsGroup(String groupName) {
        GroupManager groupManager = API.getGroupManager();
        CompletableFuture<Group> luckPermsGroupFuture = new CompletableFuture<>();
        if (!groupManager.isLoaded(groupName)) {
            groupManager.loadGroup(groupName).thenAccept(optionalGroup -> {
                if (optionalGroup.isPresent()) {
                    luckPermsGroupFuture.complete(optionalGroup.get());
                } else {
                    LOGGER.error("Luckperms group " + groupName + " couldn't be found for an unknown reason");
                }
            });
        } else {
            luckPermsGroupFuture.complete(groupManager.getGroup(groupName));
        }
        return luckPermsGroupFuture;
    }

    public void onJoin(ServerPlayerEntity player) {
        if (KiloConfig.main().playerList().customOrder) {
            // Send initial team list
            this.sendTeamList(player);
            // Updated changed / added group for everyone
            this.getGroupForUser(player.getUuid()).thenAccept(group -> {
                this.createFakeTeam(player).thenAccept(fakeTeam -> {
                    this.cachedTeams.put(player.getUuid(), fakeTeam);
                    this.sendPacketToAll(TeamS2CPacket.updateTeam(fakeTeam, true));
                });
            });
        }
        super.onJoin(player);
    }

    public void onLeave(ServerPlayerEntity player) {
        if (KiloConfig.main().playerList().customOrder) {
            final FakeTeam fakeTeam = this.cachedTeams.get(player.getUuid());
            if (fakeTeam != null) {
                this.sendPacketToAll(TeamS2CPacket.updateRemovedTeam(fakeTeam));
                this.cachedTeams.remove(player.getUuid());
            } else {
                LOGGER.error("Couldn't find fake team for " + player.getUuid());
            }
        }
        super.onLeave(player);
    }


}
