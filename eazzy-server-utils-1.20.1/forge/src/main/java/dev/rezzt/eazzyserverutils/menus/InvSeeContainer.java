package dev.rezzt.eazzyserverutils.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InvSeeContainer implements Container {
    private final Inventory playerInventory;

    public InvSeeContainer(Inventory playerInventory) {
        this.playerInventory = playerInventory;
    }

    @Override
    public int getContainerSize() {
        return 45;
    }

    @Override
    public boolean isEmpty() {
        return this.playerInventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < this.playerInventory.getContainerSize()) {
            return this.playerInventory.getItem(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < this.playerInventory.getContainerSize()) {
            return this.playerInventory.removeItem(slot, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < this.playerInventory.getContainerSize()) {
            return this.playerInventory.removeItemNoUpdate(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < this.playerInventory.getContainerSize()) {
            this.playerInventory.setItem(slot, stack);
        }
    }

    @Override
    public void setChanged() {
        this.playerInventory.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.playerInventory.clearContent();
    }
}
