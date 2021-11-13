package org.kilocraft.essentials.patch.technical;

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.ServerMetadata;

public class VersionCompatibility {

    private static int latestProtocolVersion = 0;

    private static final int pretendVersionProtocol = 1073741873;
    private static final String pretendVersionName = "1.18-pre1";

    public static void onHandshake(HandshakeC2SPacket packet) {
        latestProtocolVersion = packet.getProtocolVersion();
    }

    public static boolean shouldPretendProtocolVersion() {
        return latestProtocolVersion == pretendVersionProtocol;
    }

    public static ServerMetadata.Version getPretendMetaVersion() {
        return new ServerMetadata.Version(pretendVersionName, pretendVersionProtocol);
    }

    public static boolean isEnabled() {
        return true;
    }
}
