package org.kilocraft.essentials.api.util.tablist;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TabListData {

    private final Map<Long, FakeTeam> cachedTeams = new HashMap<>();

    public TabListData() {
        if (KiloConfig.main().playerList().customOrder) {
            if (!KiloEssentials.getInstance().hasLuckPerms()) {
                KiloEssentials.getLogger().info("Disabled custom tab order, because luckperms isn't present");
            } else {
                // Initialize team list
                this.updateTeamList();
                this.registerLuckPermEvents();
            }
        }
    }

    private void updateTeamList() {
        this.getFakeTeams().thenAccept(fakeTeams -> {
            // Send team remove packets for all old teams to all online players
            for (FakeTeam cachedTeam : this.cachedTeams.values()) {
                this.sendPacketToAll(TeamS2CPacket.updateRemovedTeam(cachedTeam));
            }

            // Send updated team list to all online players
            this.cachedTeams.clear();
            this.cachedTeams.putAll(fakeTeams);
            for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
                this.sendTeamList(player);
            }
        });
    }

    private boolean useCustomOrder() {
        return KiloConfig.main().playerList().customOrder && KiloEssentials.getInstance().hasLuckPerms();
    }

    private void registerLuckPermEvents() {
        LuckPerms api = LuckPermsProvider.get();
        EventBus eventBus = api.getEventBus();
        eventBus.subscribe(NodeAddEvent.class, event -> this.onNodeChanged(event.getNode()));
        eventBus.subscribe(NodeRemoveEvent.class, event -> this.onNodeChanged(event.getNode()));
    }

    private void onNodeChanged(Node node) {
        if (node instanceof InheritanceNode) {
            this.updateTeamList();
        }
    }

    private void sendTeamList(ServerPlayerEntity player) {
        for (FakeTeam cachedTeam : this.cachedTeams.values()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(cachedTeam, true));
        }
    }

    /**
     * This methods allows to update a players team without resending all teams using {@link TabListData#updateTeamList()}
     *
     * @param playerName       The name of the player who's primary group changed
     * @param leftTeamWeight   The weight of the group the player left
     * @param joinedTeamWeight The weight of the group the player joined
     */
    public void changedTeam(String playerName, @Nullable Long leftTeamWeight, @Nullable Long joinedTeamWeight) {
        if (leftTeamWeight != null && leftTeamWeight.equals(joinedTeamWeight)) return;
        this.handleTeamLeave(playerName, leftTeamWeight);
        this.handleTeamJoin(playerName, joinedTeamWeight);
    }

    private void handleTeamLeave(String playerName, @Nullable Long leftTeamWeight) {
        if (leftTeamWeight != null) {
            FakeTeam fakeTeam = this.cachedTeams.get(leftTeamWeight);
            if (fakeTeam != null) {
                fakeTeam.getPlayerList().remove(playerName);
                if (fakeTeam.getPlayerList().isEmpty()) {
                    this.sendPacketToAll(TeamS2CPacket.updateRemovedTeam(fakeTeam));
                    this.cachedTeams.remove(leftTeamWeight);
                } else {
                    this.sendPacketToAll(TeamS2CPacket.changePlayerTeam(fakeTeam, playerName, TeamS2CPacket.Operation.REMOVE));
                }
            } else {
                KiloEssentials.getLogger().error(playerName + " left a group with weight " + leftTeamWeight + ", which was not in the team cache");
            }
        }
    }

    private void handleTeamJoin(String playerName, @Nullable Long joinedTeamWeight) {
        if (joinedTeamWeight != null) {
            FakeTeam fakeTeam = this.cachedTeams.get(joinedTeamWeight);
            if (fakeTeam != null) {
                fakeTeam.getPlayerList().add(playerName);
                this.sendPacketToAll(TeamS2CPacket.changePlayerTeam(fakeTeam, playerName, TeamS2CPacket.Operation.ADD));
            } else {
                fakeTeam = this.createFakeTeam(joinedTeamWeight);
                this.cachedTeams.put(joinedTeamWeight, fakeTeam);
                this.sendPacketToAll(TeamS2CPacket.updateTeam(fakeTeam, true));
            }
        }
    }


    private void sendPacketToAll(Packet<?> packet) {
        KiloEssentials.getInstance().sendGlobalPacket(packet);
    }

    private CompletableFuture<Map<Long, FakeTeam>> getFakeTeams() {
        CompletableFuture<Map<Long, FakeTeam>> future = new CompletableFuture<>();
        Map<Long, FakeTeam> weightToFakeTeam = new HashMap<>();
        // I don't know if there is a better way to wait for all these async calls to finish
        AtomicInteger finishedIterations = new AtomicInteger();
        List<ServerPlayerEntity> playerList = KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList();
        int loopSize = playerList.size();
        for (ServerPlayerEntity player : playerList) {
            UUID uuid = player.getUuid();
            this.getGroupForUser(LuckPermsProvider.get(), uuid).thenAccept(group -> {
                long weight = this.getGroupWeight(group);
                FakeTeam fakeTeam;
                if (!weightToFakeTeam.containsKey(weight)) {
                    fakeTeam = this.createFakeTeam(weight);
                    weightToFakeTeam.put(weight, fakeTeam);
                } else {
                    fakeTeam = weightToFakeTeam.get(weight);
                }
                fakeTeam.getPlayerList().add(player.getEntityName());
                finishedIterations.incrementAndGet();
            });
        }
        this.waitUntilLoopFinished(finishedIterations, loopSize).thenAccept(finished -> {
            if (finished) {
                future.complete(weightToFakeTeam);
            } else {
                KiloEssentials.getLogger().warn("Couldn't retrieve luckperms group data required for tab list order within 500ms");
            }
        });
        return future;
    }

    private long getGroupWeight(Group group) {
        return group.getWeight().isPresent() ? group.getWeight().getAsInt() : 0;
    }

    private CompletableFuture<Boolean> waitUntilLoopFinished(AtomicInteger finishedIterations, int loopSize) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            // Block until above loop is fully finished
            long startTime = System.nanoTime();
            long timeoutNanos = 500000000;
            while (finishedIterations.get() < loopSize) {
                if (System.nanoTime() - startTime >= timeoutNanos) {
                    future.complete(false);
                    break;
                }
            }
            future.complete(true);
        });
        return future;
    }

    private FakeTeam createFakeTeam(long weight) {
        String name = String.format("%016d", KiloConfig.main().playerList().topToBottom ? weight : 1000000000000000L - weight);
        FakeTeam fakeTeam = new FakeTeam(name);
        fakeTeam.setShowFriendlyInvisibles(false);
        return fakeTeam;
    }

    private CompletableFuture<Group> getGroupForUser(LuckPerms api, UUID uuid) {
        CompletableFuture<Group> luckPermsGroupFuture = new CompletableFuture<>();
        this.getLuckPermsUser(api.getUserManager(), uuid).thenAccept(luckPermsUser -> {
            this.getLuckPermsGroup(api.getGroupManager(), luckPermsUser.getPrimaryGroup()).thenAccept(luckPermsGroupFuture::complete);
        });
        return luckPermsGroupFuture;
    }

    private CompletableFuture<User> getLuckPermsUser(UserManager userManager, UUID uuid) {
        CompletableFuture<User> luckPermsUserFuture = new CompletableFuture<>();
        if (!userManager.isLoaded(uuid)) {
            userManager.loadUser(uuid).thenAccept(luckPermsUserFuture::complete);
        } else {
            luckPermsUserFuture.complete(userManager.getUser(uuid));
        }
        return luckPermsUserFuture;
    }

    private CompletableFuture<Group> getLuckPermsGroup(GroupManager groupManager, String groupName) {
        CompletableFuture<Group> luckPermsGroupFuture = new CompletableFuture<>();
        if (!groupManager.isLoaded(groupName)) {
            groupManager.loadGroup(groupName).thenAccept(optionalGroup -> {
                if (optionalGroup.isPresent()) {
                    luckPermsGroupFuture.complete(optionalGroup.get());
                } else {
                    KiloEssentials.getLogger().error("Luckperms group " + groupName + " couldn't be found for an unknown reason");
                }
            });
        } else {
            luckPermsGroupFuture.complete(groupManager.getGroup(groupName));
        }
        return luckPermsGroupFuture;
    }

    private void updateTabHeaderFooter(@NotNull ServerPlayerEntity player) {
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getHeader())),
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getFooter()))
        );

        player.networkHandler.sendPacket(packet);
    }

    private void updateTabHeaderFooterEveryone() {
        for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
            this.updateTabHeaderFooter(player);
        }
    }

    private static String formatFor(@NotNull final ServerPlayerEntity player, @NotNull final String string) {
        final OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        String s = ConfigVariableFactory.replaceServerVariables(string);
        return ConfigVariableFactory.replaceOnlineUserVariables(s, user);
    }

    public void onTick() {
        this.updateTabHeaderFooterEveryone();
    }

    public void onJoin(ServerPlayerEntity player) {
        if (this.useCustomOrder()) {
            // Send initial team list
            this.sendTeamList(player);
            // Updated changed / added group for everyone
            this.getGroupForUser(LuckPermsProvider.get(), player.getUuid()).thenAccept(group -> {
                this.changedTeam(player.getEntityName(), null, this.getGroupWeight(group));
            });
        }
        // Send header update to everyone
        this.updateTabHeaderFooterEveryone();
    }


}
