package org.kilocraft.essentials.extensions.rankmanager.api.permission;

import java.util.List;

public interface RankPermission {
    /**
     * Checks if the rank is authorized for that permission or not
     *
     * @param node The permission node to check
     * @return is Authorized
     */
    boolean isAuthorized(String node);

    /**
     *
     * @return
     */
    List<String> getPermissions();

    boolean setAuthorized(String node, boolean authorized);
}
