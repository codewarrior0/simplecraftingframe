package mod.codewarrior.simplecraftingframe;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import javafx.util.Pair;
import net.fabricmc.fabric.block.entity.ClientSerializable;
import net.fabricmc.fabric.client.render.BlockEntityRendererRegistry;
import net.fabricmc.fabric.impl.client.gui.GuiProviderImpl;
import net.fabricmc.fabric.impl.container.ContainerProviderImpl;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.network.packet.BlockEntityUpdateClientPacket;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.InventoryUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.stream.IntStream;

import static mod.codewarrior.simplecraftingframe.CraftingFrameBlock.CONTAINER_ID;

public class CraftingFrameBlockEntity extends BlockEntity implements ClientSerializable {
    public static final BlockEntityType<CraftingFrameBlockEntity> TYPE = new BlockEntityType<>(CraftingFrameBlockEntity::new, null);

    private DefaultedList<ItemStack> invStacks = DefaultedList.create(9, ItemStack.EMPTY);
    private DefaultedList<ItemStack> resultStack = DefaultedList.create(1, ItemStack.EMPTY);

    public CraftingResultInventory resultInv = new CraftingResultInventory() {

        @Override
        public ItemStack getInvStack(int slot) {
            return slot >= this.getInvSize() ? ItemStack.EMPTY : resultStack.get(slot);
        }

        @Override
        public ItemStack takeInvStack(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeInvStack(int slot) {
            resultStack.set(slot, ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }

        @Override
        public void setInvStack(int slot, ItemStack stack) {
            resultStack.set(slot, stack);
        }
    };

    public Inventory inventory = new CraftingInventory(null, 3, 3) {

        @Override
        public TextComponent getName() {
            return new TranslatableTextComponent("simplecraftingframe:container.crafting_frame");
        }

        @Override
        public void clearInv() {

        }

        @Override
        public int getInvSize() {
            return 9;
        }

        @Override
        public boolean isInvEmpty() {
            return invStacks.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getInvStack(int slot) {
            return slot >= this.getInvSize() ? ItemStack.EMPTY : invStacks.get(slot);
        }

        @Override
        public ItemStack takeInvStack(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeInvStack(int slot) {
            invStacks.set(slot, ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }

        @Override
        public void setInvStack(int slot, ItemStack stack) {
            invStacks.set(slot, stack);
        }

        @Override
        public void markDirty() {

        }

        @Override
        public boolean canPlayerUseInv(PlayerEntity var1) {
            return true;
        }
    };

    public CraftingFrameBlockEntity(BlockEntityType<?> var1) {
        super(var1);
    }

    public CraftingFrameBlockEntity() {
        super(TYPE);
    }

    public void fromTag(CompoundTag compound) {
        super.fromTag(compound);

        if(compound.containsKey("grid")) {
            InventoryUtil.deserialize(compound.getCompound("grid"), this.invStacks);
            for(int slot=0; slot<9; slot++) {
                this.inventory.setInvStack(slot, this.invStacks.get(slot));
            }
            if(compound.containsKey("result")) {
                InventoryUtil.deserialize(compound.getCompound("result"), resultStack);
                resultInv.setInvStack(0, resultStack.get(0));
            }

        }
    }

    public CompoundTag toTag(CompoundTag compound) {
        super.toTag(compound);
        CompoundTag grid = new CompoundTag();
        InventoryUtil.serialize(grid, this.invStacks);
        compound.put("grid", grid);
        CompoundTag result = new CompoundTag();
        InventoryUtil.serialize(result, this.resultStack);
        compound.put("result", result);

        return compound;
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        InventoryUtil.deserialize(compoundTag.getCompound("result"), resultStack);
        resultInv.setInvStack(0, resultStack.get(0));
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        CompoundTag result = new CompoundTag();
        InventoryUtil.serialize(result, this.resultStack);
        compoundTag.put("result", result);
        return compoundTag;
    }

    @Override
    public BlockEntityUpdateClientPacket toUpdatePacket() {
        return super.toUpdatePacket();
    }

    public ItemStack getOutputStack() {
        return this.resultStack.get(0);
    }

    public void performCraft(PlayerEntity player, boolean craftStack) {
        RecipeFinder finder = new RecipeFinder();
        Recipe recipe = this.world.getServer().getRecipeManager().get(this.inventory, this.world);
        if(recipe == null) return;

        player.inventory.populateRecipeFinder(finder);

        BlockState state = this.getWorld().getBlockState(this.pos);
        Direction dir = state.get(CraftingFrameBlock.field_11177);
        BlockPos neighbor = this.pos.offset(dir.getOpposite());
        BlockState neighborState = this.getWorld().getBlockState(neighbor);

        BlockEntity entity = world.getBlockEntity(neighbor);
        final Inventory inv;

        if(entity instanceof ChestBlockEntity && neighborState.getBlock() instanceof ChestBlock) {
            ChestBlock chest = (ChestBlock) neighborState.getBlock();
            inv = chest.getContainer(neighborState, this.getWorld(), neighbor, true);
        }
        else if(entity instanceof Inventory) {
            inv = (Inventory)entity;
        }
        else {
            inv = null;
        }
        if(inv != null) {
            IntStream.range(0, inv.getInvSize()).mapToObj(inv::getInvStack).forEach(finder::addNormalItem);
        }

        IntList inputs = new IntArrayList();
        if(!finder.findRecipe(recipe, inputs)) return;

        for(int itemId: inputs) {
            ItemStack inputKind = RecipeFinder.getStackFromId(itemId);
            int inputSlot = player.inventory.method_7371(inputKind);
            if (inputSlot != -1) {
                if (player.inventory.getInvStack(inputSlot).getAmount() == 1) {
                    player.inventory.removeInvStack(inputSlot);
                } else {
                    player.inventory.takeInvStack(inputSlot, 1);
                }
            }
            else if(inv != null) {
                for (int slot=0; slot < inv.getInvSize(); slot++) {
                    ItemStack stack = inv.getInvStack(slot);
                    if(!stack.isDamaged() && !stack.hasEnchantments() && !stack.hasDisplayName()
                            && stack.getItem() == inputKind.getItem()
                            && ItemStack.areTagsEqual(stack, inputKind)
                    ) {
                        if (stack.getAmount() == 1) {
                            inv.removeInvStack(slot);
                        } else {
                            inv.takeInvStack(slot, 1);
                        }
                        break;
                    }
                }
//                IntStream.range(0, inv.getInvSize())
//                        .mapToObj((i) -> new Pair<>(i, inv.getInvStack(i)))
//                        .filter((pair) -> !pair.getValue().isDamaged() &&
//                                !pair.getValue().hasEnchantments() &&
//                                !pair.getValue().hasDisplayName() &&
//                                ItemStack.areEqual(pair.getValue(), inputKind))
//                        .findFirst()
//                        .ifPresent((pair) -> {
//                            if (pair.getValue().getAmount() == 1) {
//                                inv.removeInvStack(pair.getKey());
//                            } else {
//                                inv.takeInvStack(pair.getKey(), 1);
//                            }
//                        });
            }

        }
        ItemEntity droppedItem = new ItemEntity(this.world,
                this.pos.getX() + 0.5,
                this.pos.getY() + 0.5,
                this.pos.getZ() + 0.5);

        droppedItem.setStack(this.resultInv.getInvStack(0).copy());
        player.world.spawnEntity(droppedItem);
    }

}
