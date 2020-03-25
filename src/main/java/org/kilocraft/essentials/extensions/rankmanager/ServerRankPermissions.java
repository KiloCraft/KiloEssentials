package org.kilocraft.essentials.extensions.rankmanager;

import org.kilocraft.essentials.extensions.rankmanager.api.permission.RankPermissions;

import java.util.ArrayList;
import java.util.List;

public class ServerRankPermissions implements RankPermissions {
    private List<String> list;

    public ServerRankPermissions() {
        this.list = new ArrayList<>();
    }

    @Override
    public boolean isAuthorized(String node) {
        return this.list.contains(node);
    }

    @Override
    public boolean setAuthorized(String node, boolean authorized) {
        if (authorized && !this.list.contains(node)) {
            this.list.add(node);
            return true;
        } else if (this.list.contains(node)) {
            this.list.remove(node);
            return true;
        }

        return false;
    }

    @Override
    public List<String> getPermissions() {
        return this.list;
    }
}
