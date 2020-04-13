package org.kilocraft.essentials.chat;

import java.util.UUID;

public class DirectMessage extends TextMessage {
    private UUID sourceId;
    private String sourceName;
    private UUID targetId;
    private String targetName;

    public DirectMessage(String src, UUID srcId, String target, UUID targetId, String message) {
        super(message);
        this.sourceName = src;
        this.sourceId = srcId;
        this.targetName = target;
        this.targetId = targetId;
    }

    public DirectMessage(String message, boolean formatText) {
        super(message, formatText);
    }


}
