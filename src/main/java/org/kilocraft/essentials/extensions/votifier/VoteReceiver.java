package org.kilocraft.essentials.extensions.votifier;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.util.Vote;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.votifier.crypto.RSA;

import javax.crypto.BadPaddingException;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The vote receiving server.
 *
 * @author Blake Beaupain
 * @author Kramer Campbell
 */
public class VoteReceiver extends Thread {

    /**
     * The logger instance.
     */
    private static final Logger LOG = Logger.getLogger("Votifier");

    private final Votifier plugin;

    /**
     * The host to listen on.
     */
    private final String host;

    /**
     * The port to listen on.
     */
    private final int port;

    /**
     * The server socket.
     */
    private ServerSocket server;

    /**
     * The running flag.
     */
    private boolean running = true;

    /**
     * Instantiates a new vote receiver.
     *
     * @param host The host to listen on
     * @param port The port to listen on
     */
    public VoteReceiver(final Votifier plugin, String host, int port)
            throws Exception {
        this.plugin = plugin;
        this.host = host;
        this.port = port;

        initialize();
    }

    private void initialize() throws Exception {
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(host, port));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,
                    "Error initializing vote receiver. Please verify that the configured");
            LOG.log(Level.SEVERE,
                    "IP address and port are not already in use. This is a common problem");
            LOG.log(Level.SEVERE,
                    "with hosting services and, if so, you should check with your hosting provider.",
                    ex);
            throw new Exception(ex);
        }
    }

    /**
     * Shuts the vote receiver down cleanly.
     */
    public void shutdown() {
        running = false;
        if (server == null)
            return;
        try {
            server.close();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Unable to shut down vote receiver cleanly.");
        }
    }

    @Override
    public void run() {

        // Main loop.
        while (running) {
            try {
                Socket socket = server.accept();
                socket.setSoTimeout(5000); // Don't hang on slow connections.
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()));
                InputStream in = socket.getInputStream();

                // Send them our version.
                writer.write("VOTIFIER " + Votifier.getInstance().getVersion());
                writer.newLine();
                writer.flush();

                // Read the 256 byte block.
                byte[] block = new byte[256];
                in.read(block, 0, block.length);

                // Decrypt the block.
                block = RSA.decrypt(block, Votifier.getInstance().getKeyPair()
                        .getPrivate());
                int position = 0;

                // Perform the opcode check.
                String opcode = readString(block, position);
                position += opcode.length() + 1;
                if (!opcode.equals("VOTE")) {
                    // Something went wrong in RSA.
                    throw new Exception("Unable to decode RSA");
                }

                // Parse the block.
                String serviceName = readString(block, position);
                position += serviceName.length() + 1;
                String username = readString(block, position);
                position += username.length() + 1;
                String address = readString(block, position);
                position += address.length() + 1;
                String timeStamp = readString(block, position);
                position += timeStamp.length() + 1;

                // Create the vote.
                final Vote vote = new Vote();
                vote.setServiceName(serviceName);
                vote.setUsername(username);
                vote.setAddress(address);
                vote.setTimeStamp(timeStamp);

                if (SharedConstants.isDevelopment)
                    LOG.info("Received vote record -> " + vote);

                MinecraftServer server = KiloEssentials.getServer().getMinecraftServer();
                ServerCommandSource source = server.getCommandSource();
                for (String command : KiloConfig.main().votifier().commands) {
                    GameProfile gameProfile = server.getUserCache().findByName(vote.getUsername());
                    String name = gameProfile.isComplete() ? gameProfile.getName() : vote.getUsername();
                    command = command.replace("%PLAYER%", name)
                            .replace("%SERVICE%", vote.getServiceName())
                            .replace("%TIMESTAMP%", vote.getTimeStamp())
                            .replace("%ADDRESS%", vote.getAddress());
                    server.getCommandManager().execute(source, command);
                }


                // Clean up.
                writer.close();
                in.close();
                socket.close();
            } catch (SocketException ex) {
                LOG.log(Level.WARNING, "Protocol error. Ignoring packet - "
                        + ex.getLocalizedMessage());
            } catch (BadPaddingException ex) {
                LOG.log(Level.WARNING,
                        "Unable to decrypt vote record. Make sure that that your public key");
                LOG.log(Level.WARNING,
                        "matches the one you gave the server list.", ex);
            } catch (Exception ex) {
                LOG.log(Level.WARNING,
                        "Exception caught while receiving a vote notification",
                        ex);
            }
        }
    }

    /**
     * Reads a string from a block of data.
     *
     * @param data The data to read from
     * @return The string
     */
    private String readString(byte[] data, int offset) {
        StringBuilder builder = new StringBuilder();
        for (int i = offset; i < data.length; i++) {
            if (data[i] == '\n')
                break; // Delimiter reached.
            builder.append((char) data[i]);
        }
        return builder.toString();
    }
}