package org.kilocraft.essentials.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.setting.Settings;

import java.util.Date;
import java.util.UUID;

public class UserUtils {
    private static final ServerUserManager manager = ((ServerUserManager) KiloServer.getServer().getUserManager());
    private static PermissionUtil.Manager permManager = KiloEssentials.getInstance().getPermissionUtil().getManager();

    public static boolean isIgnoring(@NotNull final User user, String username) {
        return user.getSetting(Settings.IGNORE_LIST).containsKey(username);
    }

    public static Text getDisplayNameWithMeta(User user, boolean nickName) {
        if (permManager == PermissionUtil.Manager.LUCKPERMS) {
            StringBuilder builder = new StringBuilder();
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

            return Texter.toText(builder.toString());
        }

        return Team.modifyText(((OnlineUser) user).asPlayer().getScoreboardTeam(), new LiteralText(user.getFormattedDisplayName()));
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
            return manager.getInProcessUsers().containsKey(user.getUuid()) && manager.getInProcessUsers().get(user.getUuid()).getId().equals(processId);
        }

        public static void add(@NotNull final OnlineUser user, SimpleProcess<?> process) {
            manager.getInProcessUsers().put(user.getUuid(), process);
        }

        public static void remove(@NotNull final OnlineUser user) {
            manager.getInProcessUsers().remove(user.getUuid());
        }

        @Nullable
        public static <T> SimpleProcess<T> get(@NotNull final OnlineUser user) {
            return (SimpleProcess<T>) manager.getInProcessUsers().get(user.getUuid());
        }
    }

    public static class TpaRequests {
        public static boolean hasRequest(final OnlineUser src, final OnlineUser target) {
            if (PairMap.isInMap(src)) {
                if (PairMap.get(src).getLeft().getLeft().equals(target.getUuid())) {
                    if (new Date().getTime() - PairMap.get(src).getRight() > 60000) {
                        PairMap.remove(src);
                    } else {
                        return true;
                    }
                }
            }

            return false;
        }

        public static boolean useRequest(final OnlineUser src) {
            boolean bool = PairMap.get(src).getLeft().getRight();;
            PairMap.remove(src);
            return bool;
        }

        public static void remove(@NotNull final OnlineUser src) {
            PairMap.remove(src);
        }

        public static void add(@NotNull final OnlineUser src, @NotNull final OnlineUser target, boolean here) {
            manager.getTeleportRequestsMap().put(
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

        public static Pair<Pair<UUID, Boolean>, Long> get(@NotNull final OnlineUser src) {
            return PairMap.get(src);
        }

        private static class PairMap {
            private static Pair<Pair<UUID, Boolean>, Long> get(@NotNull final OnlineUser user) {
                return manager.getTeleportRequestsMap().get(user.getUuid());
            }

            private static void remove(@NotNull final OnlineUser user) {
                manager.getTeleportRequestsMap().remove(user.getUuid());
            }

            private static boolean isInMap(@NotNull final OnlineUser user) {
                return manager.getTeleportRequestsMap().containsKey(user.getUuid());
            }

        }
    }
}
