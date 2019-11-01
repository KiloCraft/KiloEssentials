package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserHandler {
    private File dirFile = new File(System.getProperty("user.dir") + "/KiloEssentials/Users/");

    public void handleUser(User user) {
        try {
            if (!loadData(user)) saveData(user);
        } catch (IOException e) {
            //PASS, Creating the user
            /**
              @N  remove for the BETA build
             */
            e.printStackTrace();
        }
    }

    public boolean loadData(User user) throws IOException {
        boolean exists = getUserFile(user).exists();
        if (exists) {
            user.deserialize(
                    NbtIo.readCompressed(new FileInputStream(getUserFile(user))),
                    user.getUuid()
            );
        }
        return exists;
    }

    public boolean saveData(User user) throws IOException {
        boolean made =  getUserFile(user).createNewFile();
        if (getUserFile(user).exists())
            NbtIo.writeCompressed(
                    user.serialize(),
                    new FileOutputStream(getUserFile(user))
            );

        return made;
    }

    private File getUserFile(User user) {
        return new File(dirFile.getAbsolutePath() + user.getUuid().toString() + ".dat");
    }

}
