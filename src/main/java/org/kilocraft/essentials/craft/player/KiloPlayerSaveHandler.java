package org.kilocraft.essentials.craft.player;

import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;

import java.io.File;

public class KiloPlayerSaveHandler extends NBTWorldData {
    private KiloPlayer player;

    public void register() {
        WorldDataLib.addIOCallback(this);
    }

    public KiloPlayerSaveHandler(KiloPlayer player) {
        this.player = player;
    }

    @Override
    public File getSaveFile(File file, File file1, boolean b) {
        return new File(System.getProperty("user.dir") + "/KiloEssentials/data/players/" + this.player.getUuid() + (b ? ".dat_old" : ".dat"));
    }

    @Override
    public CompoundTag toNBT(CompoundTag compoundTag) {
        CompoundTag tag = new CompoundTag();
        {
            CompoundTag meta = new CompoundTag();
            meta.putString("nick", this.player.getNickName());

            tag.put("meta", meta);
        }

        return tag;
    }

    @Override
    public void fromNBT(CompoundTag compoundTag) {
        this.player.setNickName(compoundTag.getString("meta.nick"));
    }

}
