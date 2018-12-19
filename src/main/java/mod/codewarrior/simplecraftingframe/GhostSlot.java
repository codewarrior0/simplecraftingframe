package mod.codewarrior.simplecraftingframe;

import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class GhostSlot extends Slot {
    private final int invSlot;
    public GhostSlot(Inventory Inventory_1, int int_1, int int_2, int int_3) {
        super(Inventory_1, int_1, int_2, int_3);
        invSlot = int_1;
    }

    public ItemStack onTakeItem(PlayerEntity PlayerEntity_1, ItemStack ItemStack_1) {
        this.markDirty();
        return ItemStack.EMPTY;
    }

    public ItemStack takeStack(int int_1) {
        return this.inventory.takeInvStack(this.invSlot, int_1);
    }
}
