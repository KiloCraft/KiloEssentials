package org.kilocraft.essentials.util.player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.PermissionUtil;
import org.kilocraft.essentials.util.SimpleProcess;
import org.kilocraft.essentials.util.text.Texter;

import java.util.Date;
import java.util.UUID;

public class UserUtils {
    private static final ServerUserManager USER_MANAGER = (ServerUserManager) KiloServer.getServer().getUserManager();
    private static final PermissionUtil.Manager PERM_MANAGER = KiloEssentials.getInstance().getPermissionUtil().getManager();

    public static MutableText getDisplayNameWithMeta(OnlineUser user, boolean nickName) {
        if (PERM_MANAGER == PermissionUtil.Manager.LUCKPERMS) {
            return ComponentText.toText(getDisplayNameWithMetaAsString(user, nickName));
        }

        return user.asPlayer().getScoreboardTeam() == null ? Texter.newText(user.getFormattedDisplayName()) :
                Team.modifyText(user.asPlayer().getScoreboardTeam(), new LiteralText(user.getFormattedDisplayName()));
    }

    public static String getDisplayNameWithMetaAsString(OnlineUser user, boolean nickName) {
        StringBuilder builder = new StringBuilder();
        if (PERM_MANAGER == PermissionUtil.Manager.LUCKPERMS) {
            CachedMetaData metaData = getLuckyMetaData(user.getUuid());
            String prefix = metaData.getPrefix();
            String suffix = metaData.getSuffix();

            if (prefix != null) {
                builder.append(prefix);
            }

            builder.append(nickName ? user.getFormattedDisplayName() : user.getUsername());

            if (suffix != null) {
                builder.append(suffix);
            }

            return builder.toString();
        }
        return user.getFormattedDisplayName();
    }

    private static net.luckperms.api.model.user.User getLuckyUser(UUID uuid) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        return luckPerms.getUserManager().getUser(uuid);
    }

    private static CachedMetaData getLuckyMetaData(UUID uuid) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        QueryOptions options = luckPerms.getContextManager().getStaticQueryOptions();
        return getLuckyUser(uuid).getCachedData().getMetaData(options);
    }

    public static class Process {
        public static boolean isIn(@NotNull final OnlineUser user, String processId) {
            return USER_MANAGER.getInProcessUsers().containsKey(user.getUuid()) && USER_MANAGER.getInProcessUsers().get(user.getUuid()).getId().equals(processId);
        }

        public static void add(@NotNull final OnlineUser user, SimpleProcess<?> process) {
            USER_MANAGER.getInProcessUsers().put(user.getUuid(), process);
        }

        public static void remove(@NotNull final OnlineUser user) {
            USER_MANAGER.getInProcessUsers().remove(user.getUuid());
        }

        public static boolean isInAny(@NotNull final OnlineUser user) {
            return USER_MANAGER.getInProcessUsers().containsKey(user.getUuid());
        }

        @Nullable
        public static <T> SimpleProcess<T> get(@NotNull final OnlineUser user) {
            return (SimpleProcess<T>) USER_MANAGER.getInProcessUsers().get(user.getUuid());
        }
    }

    public static class TpaRequests {
        public static boolean hasRequest(final OnlineUser src, final OnlineUser target) {
            if (PairMap.isInMap(src)) {
                if (PairMap.get(src).getLeft().getLeft().equals(target.getUuid())) {
                    if (new Date().getTime() - PairMap.get(src).getRight() > 60000) {
                        PairMap.remove(src);
                        return false;
                    } else {
                        return true;
                    }
                }
            }

            return false;
        }

        public static boolean useRequestAndGetType(final OnlineUser src) {
            boolean toSender = PairMap.get(src).getLeft().getRight();
            PairMap.remove(src);
            return toSender;
        }

        public static void remove(@NotNull final OnlineUser src) {
            PairMap.remove(src);
        }

        public static void add(@NotNull final OnlineUser src, @NotNull final OnlineUser target, boolean here) {
            USER_MANAGER.getTeleportRequestsMap().put(
                    src.getUuid(),
                    new Pair<>(
                            new Pair<>(
                                    target.getUuid(),
                                    here
                            ),
                            new Date().getTime()
                    )
            );
        }

        private static class PairMap {
            private static Pair<Pair<UUID, Boolean>, Long> get(@NotNull final OnlineUser user) {
                return USER_MANAGER.getTeleportRequestsMap().get(user.getUuid());
            }

            private static void remove(@NotNull final OnlineUser user) {
                USER_MANAGER.getTeleportRequestsMap().remove(user.getUuid());
            }

            private static boolean isInMap(@NotNull final OnlineUser user) {
                return USER_MANAGER.getTeleportRequestsMap().containsKey(user.getUuid());
            }

        }
    }

    public static class Animate {
        public static void swingHand(PlayerEntity player) {
            swingHand(player, Hand.MAIN_HAND);
        }

        public static void swingHand(PlayerEntity player, Hand hand) {
            player.swingHand(hand, true);
        }

    }

}
