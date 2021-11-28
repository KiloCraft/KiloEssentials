package org.kilocraft.essentials.patch.technical;

import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import org.kilocraft.essentials.config.KiloConfig;

public class VersionCompatibility {

    private static int latestProtocolVersion = 0;

    public static void onHandshake(ClientIntentionPacket packet) {
        latestProtocolVersion = packet.getProtocolVersion();
    }

    public static boolean shouldPretendProtocolVersion() {
        return latestProtocolVersion == KiloConfig.main().versionCompatibility.versionProtocol;
    }

    public static ServerStatus.Version getPretendMetaVersion() {
        return new ServerStatus.Version(KiloConfig.main().versionCompatibility.versionName, KiloConfig.main().versionCompatibility.versionProtocol);
    }

    public static boolean isEnabled() {
        return KiloConfig.main().versionCompatibility.enabled;
    }
}
