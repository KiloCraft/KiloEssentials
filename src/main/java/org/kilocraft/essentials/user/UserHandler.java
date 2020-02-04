package org.kilocraft.essentials.user;

import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.config_old.KiloConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UserHandler {
    private static File saveDir = new File(System.getProperty("user.dir") + "/KiloEssentials/users/");

    public void handleUser(ServerUser serverUser) throws IOException {
        if (!loadUser(serverUser)) {
            saveDir.mkdirs();
            getUserFile(serverUser).createNewFile();
            saveData(serverUser);
            handleUser(serverUser);
        }

    }

    public boolean loadUser(ServerUser serverUser) throws IOException {
        if (getUserFile(serverUser).exists()) {
            serverUser.deserialize(NbtIo.readCompressed(new FileInputStream(getUserFile(serverUser))));
            return true;
        }
        return false;
    }

    void saveData(ServerUser serverUser) throws IOException {
        if (getUserFile(serverUser).exists())
            NbtIo.writeCompressed(
                    serverUser.serialize(),
                    new FileOutputStream(getUserFile(serverUser))
            );
        else {
            saveDir.mkdirs();
            getUserFile(serverUser).createNewFile();
            saveData(serverUser);
        }
    }

    boolean userExists(UUID uuid) {
        return getUserFile(uuid).exists();
    }

    public File getUserFile(ServerUser serverUser) {
        return getUserFile(serverUser.uuid);
    }

    public File getUserFile(UUID uuid) {
        return new File( KiloConfig.getWorkingDirectory() + "/KiloEssentials/users/" + uuid.toString() + ".dat");
    }

}
