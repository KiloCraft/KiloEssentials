package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserHandler {
    private static File saveDir = new File(System.getProperty("user.dir") + "/KiloEssentials/users/");

    public void handleUser(User user) throws IOException {
        if (getUserFile(user).exists())
            user.deserialize(NbtIo.readCompressed(new FileInputStream(getUserFile(user))));
        else {
            saveDir.mkdirs();
            getUserFile(user).createNewFile();
            saveData(user);
            handleUser(user);
        }

    }

    public void loadUser(User user) throws IOException {
        if (getUserFile(user).exists())
            user.deserialize(NbtIo.readCompressed(new FileInputStream(getUserFile(user))));

    }

    void saveData(User user) throws IOException {
        if (getUserFile(user).exists())
            NbtIo.writeCompressed(
                    user.serialize(),
                    new FileOutputStream(getUserFile(user))
            );
        else {
            saveDir.mkdirs();
            getUserFile(user).createNewFile();
            saveData(user);
        }
    }

    private File getUserFile(User user) {
        return new File( KiloConifg.getWorkingDirectory() + "/KiloEssentials/users/" + user.getUuidAsString() + ".dat");
    }

}
