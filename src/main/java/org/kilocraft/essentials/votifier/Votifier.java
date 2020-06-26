package org.kilocraft.essentials.votifier;

import java.io.*;
import java.security.KeyPair;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.votifier.crypto.RSAIO;
import org.kilocraft.essentials.votifier.crypto.RSAKeygen;

/**
 * The main Votifier plugin class.
 *
 * @author Blake Beaupain
 * @author Kramer Campbell
 */
public class Votifier {

    /** The logger instance. */
    private static final Logger LOG = KiloEssentials.getLogger();

    /** The Votifier instance. */
    private static Votifier instance;

    /** The current Votifier version. */
    private String version;

    /** The vote receiver. */
    private VoteReceiver voteReceiver;

    /** The RSA key pair. */
    private KeyPair keyPair;


    /**
     * Attach custom log filter to logger.
     */

    public void onEnable() {
        Votifier.instance = this;

        // Set the plugin version.
        version = "1.0";


        /*
         * Use IP address from server.properties as a default for
         * configurations. Do not use InetAddress.getLocalHost() as it most
         * likely will return the main server address instead of the address
         * assigned to the server.
         */
        String hostAddr = KiloEssentials.getServer().getMinecraftServer().getServerIp();
        if (hostAddr == null || hostAddr.length() == 0) {
            hostAddr = "0.0.0.0";
        }

        /*
         * Create RSA directory and keys if it does not exist; otherwise, read
         * keys.
         */
        File rsaDirectory = KiloEssentials.getEssentialsPath().resolve("votifier").toFile();
        LOG.info(rsaDirectory.getAbsolutePath());
        System.out.println(rsaDirectory.getAbsolutePath());
        try {
            if (!rsaDirectory.exists()) {
                rsaDirectory.mkdir();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            LOG.error("Error reading configuration file or RSA keys", ex);
            gracefulExit();
            return;
        }

        // Initialize the receiver.
        String host = hostAddr;
        /*TODO Add to config*/
        int port = KiloConfig.main().votifier().port;

        try {
            voteReceiver = new VoteReceiver(this, host, port);
            voteReceiver.start();

            LOG.info("Votifier enabled.");
        } catch (Exception ex) {
            gracefulExit();
            return;
        }
    }

    public void onDisable() {
        // Interrupt the vote receiver.
        if (voteReceiver != null) {
            voteReceiver.shutdown();
        }
        LOG.info("Votifier disabled.");
    }

    private void gracefulExit() {
        LOG.error("Votifier did not initialize properly!");
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static Votifier getInstance() {
        return instance;
    }

    /**
     * Gets the version.
     *
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the vote receiver.
     *
     * @return The vote receiver
     */
    public VoteReceiver getVoteReceiver() {
        return voteReceiver;
    }

    /**
     * Gets the keyPair.
     *
     * @return The keyPair
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }

}
