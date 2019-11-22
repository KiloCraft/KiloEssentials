package org.kilocraft.essentials.api.user;

import java.util.List;
import java.util.UUID;

public interface UsernameHistoryTracker {
    String getUsername();

    UUID getCurrentUuid();

    List<FormerUserEntry> previousNames();
}
