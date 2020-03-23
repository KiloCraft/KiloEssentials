package org.kilocraft.essentials.user;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.lang3.time.StopWatch;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.NBTUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UserHandler {
    public static final short DATA_VERSION = 1;
    private static final File saveDir = KiloEssentials.getDataDirPath().resolve("users").toFile();

    void handleUser(final ServerUser serverUser) throws IOException {
        if (!this.loadUser(serverUser)) {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(serverUser).createNewFile();
            this.saveData(serverUser);
            this.handleUser(serverUser);
        }

    }

    void loadUserAndResolveName(final ServerUser user) throws IOException {
        if (this.getUserFile(user).exists()) {
            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(this.getUserFile(user)));
            user.deserialize(tag);
            user.name = tag.getString("name");
        }
    }

    private boolean loadUser(final ServerUser serverUser) throws IOException {
        if (this.getUserFile(serverUser).exists()) {
            serverUser.deserialize(NbtIo.readCompressed(new FileInputStream(this.getUserFile(serverUser))));
            return true;
        }
        return false;
    }

    void saveData(final ServerUser serverUser) throws IOException {
        if (this.getUserFile(serverUser).exists())
            NbtIo.writeCompressed(
                    serverUser.serialize(),
                    new FileOutputStream(this.getUserFile(serverUser))
            );
        else {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(serverUser).createNewFile();
            this.saveData(serverUser);
        }
    }

    boolean userExists(final UUID uuid) {
        return this.getUserFile(uuid).exists();
    }

    private File getUserFile(final ServerUser serverUser) {
        return this.getUserFile(serverUser.uuid);
    }

    private File getUserFile(final UUID uuid) {
        return KiloEssentials.getDataDirPath().resolve("users").resolve(uuid.toString() + ".dat").toFile();
    }

    File[] getUserFiles() {
        return KiloEssentials.getDataDirPath().resolve("users").toFile().listFiles();
    }

    public void upgrade() {
        StopWatch watch = new StopWatch();
        watch.start();
        int i = 0;
        File[] files = this.getUserFiles();

        try {
            for (File file : files) {
                if (!file.exists() || !file.getName().toLowerCase(Locale.ROOT).endsWith(".dat")) {
                    continue;
                }

                ServerUser user = new ServerUser(UUID.fromString(file.getName().replaceFirst(".dat", "")));

                if (user.dataVer != 0) {
                    if (user.dataVer < DATA_VERSION) {
                        user.saveData();

                        if (SharedConstants.isDevelopment) {
                            KiloEssentials.getLogger().info("Updated User data for user [" + user.name + "/" + user.uuid + "]");
                        }

                        i++;
                    }
                }

            }

            watch.stop();
            if (i >= 1) {
                String timeElapsed = new DecimalFormat("##.##").format(watch.getTime(TimeUnit.MILLISECONDS));
                KiloEssentials.getLogger().info("Successfully upgraded the User data for " + i + " users, time elapsed: " + timeElapsed + "ms");
            }

        } catch (Exception e) {
            KiloEssentials.getLogger().error("Failed to Upgrade the User Data! " + i + " Successful / " + (files.length - i) + " Filed");
            e.printStackTrace();
        }
    }
}
