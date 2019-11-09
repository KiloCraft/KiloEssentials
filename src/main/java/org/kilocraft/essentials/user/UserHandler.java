package org.kilocraft.essentials.user;

import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.config.KiloConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserHandler {
    private static File saveDir = new File(System.getProperty("user.dir") + "/KiloEssentials/users/");

    public void handleUser(ServerUser serverUser) throws IOException {
        if (getUserFile(serverUser).exists())
            serverUser.deserialize(NbtIo.readCompressed(new FileInputStream(getUserFile(serverUser))));
        else {
            saveDir.mkdirs();
            getUserFile(serverUser).createNewFile();
            saveData(serverUser);
            handleUser(serverUser);
        }

    }

    public void loadUser(ServerUser serverUser) throws IOException {
        if (getUserFile(serverUser).exists())
            serverUser.deserialize(NbtIo.readCompressed(new FileInputStream(getUserFile(serverUser))));

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

    private File getUserFile(ServerUser serverUser) {
        return new File( KiloConfig.getWorkingDirectory() + "/KiloEssentials/users/" + serverUser.getUuid().toString() + ".dat");
    }

}
