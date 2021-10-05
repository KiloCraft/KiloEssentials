package org.kilocraft.essentials.patch.technical;

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.ServerMetadata;

public class VersionCompability {

    private static int latestProtocolVersion = 0;

    private static final int pretendVersionProtocol = 1073741867;
    private static final String pretendVersionName = "21w39a";

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
        return false;
    }
}
