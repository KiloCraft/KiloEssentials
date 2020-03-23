package org.kilocraft.essentials.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;

import java.util.UUID;

public class UserUtils {
    private static PermissionUtil.Manager permManager = KiloEssentials.getInstance().getPermissionUtil().getManager();

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

        return Team.modifyText(((OnlineUser) user).getPlayer().getScoreboardTeam(), new LiteralText(user.getFormattedDisplayName()));
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
}
