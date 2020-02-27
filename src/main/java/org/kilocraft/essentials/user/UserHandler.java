package org.kilocraft.essentials.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UserHandler {
    private static final File saveDir = new File(KiloEssentials.getDataDirectory() + "users/");

    public void handleUser(final ServerUser serverUser) throws IOException {
        if (!this.loadUser(serverUser)) {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(serverUser).createNewFile();
            this.saveData(serverUser);
            this.handleUser(serverUser);
        }

    }

    public boolean loadUserAndResolveName(final ServerUser user) throws IOException {
        if (this.getUserFile(user).exists()) {
            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(this.getUserFile(user)));
            user.deserialize(tag);
            user.name = tag.getString("name");
            return true;
        }
        return false;
    }

    public boolean loadUser(final ServerUser serverUser) throws IOException {
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

    public File getUserFile(final ServerUser serverUser) {
        return this.getUserFile(serverUser.uuid);
    }

    public File getUserFile(final UUID uuid) {
        return KiloEssentials.getDataDirPath().resolve("users").resolve(uuid.toString() + ".dat").toFile();
    }

    public File[] getUserFiles() {
        return KiloEssentials.getDataDirPath().resolve("users").toFile().listFiles();
    }

}
