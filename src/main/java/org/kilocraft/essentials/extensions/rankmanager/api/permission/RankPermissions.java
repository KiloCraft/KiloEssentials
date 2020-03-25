package org.kilocraft.essentials.extensions.rankmanager.api.permission;

import java.util.List;

public interface RankPermissions {
    /**
     * Checks if the rank is authorized for that permission or not
     *
     * @param node The permission node to check
     * @return is Authorized
     */
    boolean isAuthorized(String node);

    /**
     * Sets if a rank is authorized for a permission
     *
     * @param node The Permission node to set
     * @param authorized Set the Permission
     * @return true if the status of that permission has changed
     */
    boolean setAuthorized(String node, boolean authorized);

    /**
     * Gets the Permissions as a List of Strings
     *
     * @return List of the Permissions
     */
    List<String> getPermissions();
}
