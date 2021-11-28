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
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerPlayer;
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
            this.sendPacketToAll(ClientboundSetPlayerTeamPacket.createRemovePacket(fakeTeam));
        }
        final ServerPlayer player = KiloEssentials.getMinecraftServer().getPlayerList().getPlayer(uuid);
        if (Objects.nonNull(player)) {
            this.createFakeTeam(player).thenAccept(newFakeTeam -> {
                this.cachedTeams.put(player.getUUID(), newFakeTeam);
                this.sendPacketToAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(newFakeTeam, true));
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

    private void sendTeamList(ServerPlayer player) {
        for (FakeTeam cachedTeam : this.cachedTeams.values()) {
            player.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(cachedTeam, true));
        }
    }

    private long getGroupWeight(Group group) {
        return group.getWeight().isPresent() ? group.getWeight().getAsInt() : 0;
    }

    private CompletableFuture<FakeTeam> createFakeTeam(ServerPlayer player) {
        CompletableFuture<FakeTeam> fakeTeamFuture = new CompletableFuture<>();
        this.getLuckPermsUser(player.getUUID()).thenAccept(user -> {
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

    private void configureTeam(FakeTeam fakeTeam, User user, ServerPlayer player) {
        fakeTeam.setSeeFriendlyInvisibles(false);
        final CachedMetaData metaData = user.getCachedData().getMetaData();
        final String color = metaData.getMetaValue("color");
        if (color != null) {
            ChatFormatting formatting = ChatFormatting.getByName(color);
            if (formatting != null) fakeTeam.setColor(formatting);
        }
        fakeTeam.setPlayerPrefix(ComponentText.toText(Objects.requireNonNullElse(metaData.getPrefix(), "")));
        fakeTeam.setPlayerSuffix(ComponentText.toText(Objects.requireNonNullElse(metaData.getSuffix(), "")));
        fakeTeam.getPlayers().add(player.getScoreboardName());
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

    public void onJoin(ServerPlayer player) {
        if (KiloConfig.main().playerList().customOrder) {
            // Updated changed / added group for everyone
            this.getGroupForUser(player.getUUID()).thenAccept(group -> {
                this.createFakeTeam(player).thenAccept(fakeTeam -> {
                    this.cachedTeams.put(player.getUUID(), fakeTeam);
                    this.sendPacketToAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(fakeTeam, true));
                    // Send initial team list
                    this.sendTeamList(player);
                });
            });
        }
        super.onJoin(player);
    }

    public void onLeave(ServerPlayer player) {
        if (KiloConfig.main().playerList().customOrder) {
            final FakeTeam fakeTeam = this.cachedTeams.get(player.getUUID());
            if (fakeTeam != null) {
                this.sendPacketToAll(ClientboundSetPlayerTeamPacket.createRemovePacket(fakeTeam));
                this.cachedTeams.remove(player.getUUID());
            } else {
                LOGGER.error("Couldn't find fake team for " + player.getUUID());
            }
        }
        super.onLeave(player);
    }


}
