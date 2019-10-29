package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class UserManager {
    private static List<User> loadedUsers = new ArrayList<>();
    private File saveDir = new File(KiloConifg.getWorkingDirectory() + "/users/");

    public UserManager() {

    }

    private boolean createNewUserFile(User user) throws IOException {
        saveDir.mkdirs();
        File userFile = new File(saveDir.getAbsolutePath() + "/" + user.getUuid().toString());
        return userFile.createNewFile();
    }

    private void saveUser(User user) throws IOException {
        FileOutputStream stream = new FileOutputStream(saveDir.getAbsolutePath() + "/" + user.getUuid().toString());
        NbtIo.writeCompressed(user.serialize(), stream);
    }

    private void loadUser(UUID uuid) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(saveDir.getAbsolutePath() + "/" + uuid.toString());
        
    }

}
