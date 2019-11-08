package org.kilocraft.essentials.craft.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;
import org.kilocraft.essentials.api.KiloServer;

public class GUI implements Inventory {

    DefaultedList<ItemStack> itemStacks;
    private int size;

    public GUI(int size){
        this.size = size;
        for(int i = 0; i < getInvSize(); i++){
            KiloServer.getServer().sendMessage("Size: " + getInvSize());
            KiloServer.getServer().sendMessage("i: " + i);
            itemStacks.set(i, new ItemStack(Items.DIAMOND, 1));
        }
    }

    @Override
    public int getInvSize() {
        return this.size;
    }

    @Override
    public boolean isInvEmpty() {
        if(itemStacks.size() == 0) return true;
        return false;
    }

    @Override
    public ItemStack getInvStack(int i) {
        return itemStacks.get(i);
    }

    @Override
    public ItemStack takeInvStack(int i, int i1) {
        return null;
    }

    @Override
    public ItemStack removeInvStack(int i) {
        return null;
    }

    @Override
    public void setInvStack(int i, ItemStack itemStack) {
        itemStacks.set(i, itemStack);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity playerEntity) {
        return true;
    }

    @Override
    public void clear() {
        itemStacks.clear();
    }
}
