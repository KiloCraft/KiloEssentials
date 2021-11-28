package org.kilocraft.essentials.util.player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.text.Texter;

import java.util.Date;
import java.util.UUID;

public class UserUtils {
    private static final ServerUserManager USER_MANAGER = KiloEssentials.getUserManager();

    public static MutableComponent getDisplayNameWithMeta(OnlineUser user, boolean nickName) {
        if (KiloEssentials.getInstance().hasLuckPerms()) {
            return ComponentText.toText(getDisplayNameWithMetaAsString(user, nickName));
        }

        return user.asPlayer().getTeam() == null ? Texter.newText(user.getFormattedDisplayName()) :
                PlayerTeam.formatNameForTeam(user.asPlayer().getTeam(), new TextComponent(user.getFormattedDisplayName()));
    }

    public static String getDisplayNameWithMetaAsString(OnlineUser user, boolean nickName) {
        StringBuilder builder = new StringBuilder();
        if (KiloEssentials.getInstance().hasLuckPerms()) {
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

    public static class TpaRequests {
        public static boolean hasRequest(final OnlineUser src, final OnlineUser target) {
            if (PairMap.isInMap(src)) {
                if (PairMap.get(src).getA().getA().equals(target.getUuid())) {
                    if (new Date().getTime() - PairMap.get(src).getB() > 60000) {
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
            boolean toSender = PairMap.get(src).getA().getB();
            PairMap.remove(src);
            return toSender;
        }

        public static void remove(@NotNull final OnlineUser src) {
            PairMap.remove(src);
        }

        public static void add(@NotNull final OnlineUser src, @NotNull final OnlineUser target, boolean here) {
            USER_MANAGER.getTeleportRequestsMap().put(
                    src.getUuid(),
                    new Tuple<>(
                            new Tuple<>(
                                    target.getUuid(),
                                    here
                            ),
                            new Date().getTime()
                    )
            );
        }

        private static class PairMap {
            private static Tuple<Tuple<UUID, Boolean>, Long> get(@NotNull final OnlineUser user) {
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

}
