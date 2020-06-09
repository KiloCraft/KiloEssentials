package org.kilocraft.essentials.user;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.lang3.time.StopWatch;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.*;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class UserHandler {
    public static final short DATA_VERSION = 3;
    private static final File saveDir = KiloEssentials.getDataDirPath().resolve("users").toFile();

    void handleUser(final ServerUser serverUser) throws IOException {
        if (!this.loadUser(serverUser)) {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(serverUser).createNewFile();
            this.save(serverUser);
            this.handleUser(serverUser);
        }

    }

    void loadUserAndResolveName(final ServerUser user) throws IOException {
        if (this.getUserFile(user).exists()) {
            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(this.getUserFile(user)));
            user.fromTag(tag);
            user.name = tag.getString("name");
        }
    }

    private boolean loadUser(final ServerUser serverUser) throws IOException {
        if (this.getUserFile(serverUser).exists()) {
            serverUser.fromTag(NbtIo.readCompressed(new FileInputStream(this.getUserFile(serverUser))));
            return true;
        }
        return false;
    }

    void save(final ServerUser user) throws IOException {
        if (this.getUserFile(user).exists()) {
            NbtIo.writeCompressed(
                    user.toTag(),
                    new FileOutputStream(this.getUserFile(user))
            );
        } else {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(user).createNewFile();
            this.save(user);
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

    public File[] getUserFiles() {
        return KiloEssentials.getDataDirPath().resolve("users").toFile().listFiles();
    }


    public void upgrade() {
        File[] files = getUserFiles();
        if (files == null || files.length <= 0) {
            return;
        }

        int random = ThreadLocalRandom.current().nextInt(0, files.length);

        File file = files[random];
        UUID uuid = UUID.fromString(file.getName().replace(".dat", ""));

        try {
            if (upgrade(file, uuid)) {
                KiloEssentials.getLogger().info("Found old data format! Updating the user data format!");
                upgradeAll();
            }
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Failed at checking the user data!");
            e.printStackTrace();
        }
    }

    private void upgradeAll() {
        StopWatch watch = new StopWatch();
        watch.start();
        int updated = 0;
        File[] files = getUserFiles();

        for (File file : files) {
            UUID uuid = UUID.fromString(file.getName().replace(".dat", ""));
            try {
                if (upgrade(file, uuid)) {
                    updated++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        watch.stop();
        String timeElapsed = new DecimalFormat("##.##").format(watch.getTime(TimeUnit.MILLISECONDS));
        KiloEssentials.getLogger().info("Successfully upgraded the User data for " + updated + " users, time elapsed: " + timeElapsed + "ms");
    }

    private boolean upgrade(File file, UUID uuid) throws IOException {
        CompoundTag tag;

        try {
            tag = NbtIo.readCompressed(new FileInputStream(file));
        } catch (Exception e) {
            KiloEssentials.getLogger().warn("Broken user data! [" + uuid + "] Please check their user file!");
            return true;
        }

        if (!tag.contains("dataVer")) {
            tag.putShort("dataVer", DATA_VERSION);
        }

        short dataVer = tag.getShort("dataVer");

        if (dataVer != 0) {
            if (dataVer < DATA_VERSION) {
                NbtIo.writeCompressed(tag, new FileOutputStream(file));

                if (SharedConstants.isDevelopment) {
                    KiloEssentials.getLogger().info("Updated User data for user [" + tag.getString("name") + "/" + uuid.toString() + "]");
                }

                return true;
            }
        }

        return false;
    }
}
