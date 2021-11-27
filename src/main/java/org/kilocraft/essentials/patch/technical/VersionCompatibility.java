package org.kilocraft.essentials.patch.technical;

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.ServerMetadata;
import org.kilocraft.essentials.config.KiloConfig;

public class VersionCompatibility {

    private static int latestProtocolVersion = 0;

    public static void onHandshake(HandshakeC2SPacket packet) {
        latestProtocolVersion = packet.getProtocolVersion();
    }

    public static boolean shouldPretendProtocolVersion() {
        return latestProtocolVersion == KiloConfig.main().versionCompatibility.versionProtocol;
    }

    public static ServerMetadata.Version getPretendMetaVersion() {
        return new ServerMetadata.Version(KiloConfig.main().versionCompatibility.versionName, KiloConfig.main().versionCompatibility.versionProtocol);
    }

    public static boolean isEnabled() {
        return KiloConfig.main().versionCompatibility.enabled;
    }
}
