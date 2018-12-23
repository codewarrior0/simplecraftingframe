package mod.codewarrior.simplecraftingframe;

import net.minecraft.container.SlotActionType;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CraftingFrameContainer extends CraftingContainer {

    private final PlayerEntity player;
    private World world;
    private BlockPos pos;
    CraftingFrameBlockEntity entity;

    public Inventory craftingInv;
    public CraftingResultInventory resultInv;

    public CraftingFrameContainer(PlayerInventory playerInventory, World world, BlockPos pos) {
        this.player = playerInventory.player;
        this.world = world;
        this.pos = pos;

        this.entity = (CraftingFrameBlockEntity)world.getBlockEntity(pos);
        this.craftingInv = entity.inventory;
        this.resultInv = entity.resultInv;

        this.addSlot(new GhostSlot(resultInv, 0, 124, 35));

        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 3; ++column) {
                this.addSlot(new GhostSlot(this.craftingInv, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }

        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new GhostSlot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for(int column = 0; column < 9; ++column) {
            this.addSlot(new GhostSlot(playerInventory, column, 8 + column * 18, 142));
        }

        this.onContentChanged(craftingInv);

    }

    public void loadRecipe(Recipe recipe) {

    }

    @Override
    public boolean canUse(PlayerEntity var1) {
        return true;
    }

    public void onContentChanged(Inventory Inventory_1) {
        this.onCraftingContentChanged(this.world, this.player, this.craftingInv, this.resultInv);
        SimpleCraftingFrame.sendFrameUpdate(entity);
    }

    public ItemStack transferSlot(PlayerEntity PlayerEntity_1, int int_1) {
        ItemStack ItemStack_1 = ItemStack.EMPTY;
        Slot slot = this.slotList.get(int_1);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            ItemStack_1 = stack.copy();
            if (int_1 == 0) {
                stack.getItem().onCrafted(stack, this.world, PlayerEntity_1);
                if (!this.insertItem(stack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onStackChanged(stack, ItemStack_1);
            } else if (int_1 >= 10 && int_1 < 37) {
                if (!this.insertItem(stack, 37, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (int_1 >= 37 && int_1 < 46) {
                if (!this.insertItem(stack, 10, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack, 10, 46, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stack.getAmount() == ItemStack_1.getAmount()) {
                return ItemStack.EMPTY;
            }

            ItemStack ItemStack_3 = slot.onTakeItem(PlayerEntity_1, stack);
            if (int_1 == 0) {
                PlayerEntity_1.dropItem(ItemStack_3, false);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack onSlotClick(int slot, int button, SlotActionType action, PlayerEntity player) {
        if(slot == 0) return ItemStack.EMPTY;

        if(slot < 10 && slot >= 0) {
            if (action == SlotActionType.PICKUP || action == SlotActionType.QUICK_CRAFT) {
                ItemStack cursorStack = player.inventory.getCursorStack();
                if (!cursorStack.isEmpty()) {
                    ItemStack ghost = cursorStack.copy();
                    ghost.setAmount(1);
                    this.slotList.get(slot).setStack(ghost);
                } else {
                    this.slotList.get(slot).setStack(ItemStack.EMPTY);
                }
                this.onContentChanged(craftingInv);

                return ItemStack.EMPTY;

            } else if (action == SlotActionType.THROW) {
                this.slotList.get(slot).setStack(ItemStack.EMPTY);
                this.onContentChanged(craftingInv);
                return ItemStack.EMPTY;

            } else if (action == SlotActionType.QUICK_MOVE) {
                return ItemStack.EMPTY;
            }
        }

        return super.onSlotClick(slot, button, action, player);


    }

    @Override
    public void populateRecipeFinder(RecipeFinder recipeFinder) {

    }

    @Override
    public void clearCraftingSlots() {
        this.craftingInv.clearInv();
        this.resultInv.clearInv();
    }

    @Override
    public boolean matches(Recipe recipe) {
        return recipe.matches(this.craftingInv, this.player.world);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return 3;
    }

    @Override
    public int getCraftingHeight() {
        return 3;
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }
}
