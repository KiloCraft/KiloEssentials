package org.kilocraft.essentials.extensions.votifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.votifier.crypto.RSAIO;
import org.kilocraft.essentials.extensions.votifier.crypto.RSAKeygen;

import java.io.File;
import java.security.KeyPair;

public class Votifier {

    private static final Logger LOGGER = LogManager.getLogger("KiloEssentials|Votifier");
    private static final String version = "1.1";
    private static VoteReceiver voteReceiver;
    private static KeyPair keyPair;

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static void onEnable() {
        String hostAddr = KiloEssentials.getMinecraftServer().getServerIp();
        if (hostAddr == null || hostAddr.length() == 0) {
            hostAddr = "0.0.0.0";
        }

        File rsaDirectory = KiloEssentials.getEssentialsPath().resolve("votifier").toFile();
        try {
            if (!rsaDirectory.exists()) {
                rsaDirectory.mkdir();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            LOGGER.error("Error reading configuration file or RSA keys", ex);
            gracefulExit();
            return;
        }

        // Initialize the receiver.
        String host = hostAddr;
        int port = KiloConfig.main().votifier().port;

        try {
            voteReceiver = new VoteReceiver(host, port);
            voteReceiver.start();

            LOGGER.info("Starting up Votifier...");
        } catch (Exception ex) {
            gracefulExit();
        }
    }

    public static void onDisable() {
        // Interrupt the vote receiver.
        if (voteReceiver != null) {
            voteReceiver.shutdown();
            LOGGER.info("Votifier stopped.");
        }
    }

    private static void gracefulExit() {
        LOGGER.error("Votifier did not initialize properly!");
    }

    public static String getVersion() {
        return version;
    }
}