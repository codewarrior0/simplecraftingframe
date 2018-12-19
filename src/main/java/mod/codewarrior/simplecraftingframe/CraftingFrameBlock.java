package mod.codewarrior.simplecraftingframe;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.loot.context.LootContext;

import java.util.List;
import java.util.function.Consumer;

public class CraftingFrameBlock extends HorizontalFacingBlock implements BlockEntityProvider, Consumer<PacketByteBuf> {

    public static final Identifier CONTAINER_ID = new Identifier("simplecraftingframe:crafting_frame");
    public static final DirectionProperty FACING = field_11177;

    public CraftingFrameBlock(Settings var1) {
        super(var1);
        setDefaultState(this.stateFactory.getDefaultState().with(FACING, Direction.NORTH));
    }

    protected void appendProperties(StateFactory.Builder<Block, BlockState> StateFactory$Builder_1) {
        StateFactory$Builder_1.with(FACING);
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, Direction dir, float hitX, float hitY, float hitZ) {
        if (world.isClient) {
            return true;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CraftingFrameBlockEntity)) return false;

        CraftingFrameBlockEntity entity = (CraftingFrameBlockEntity) be;

        entity.performCraft(player, player.isSneaking());
        return true;
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (world.isClient) {
            return;
        } else if (player.isSneaking()) {
            if (showGui(world, pos, (ServerPlayerEntity) player)) return;
            return;
        }
    }

    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState oldState, boolean boolean_1) {
        if (state.getBlock() != oldState.getBlock()) {
            super.onBlockRemoved(state, world, pos, oldState, boolean_1);
            world.removeBlockEntity(pos);
        }
    }

    private boolean showGui(World world, BlockPos pos, ServerPlayerEntity player) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CraftingFrameBlockEntity)) return true;

        ContainerProviderRegistry.INSTANCE.openContainer(CONTAINER_ID, player, (buf) -> {
            buf.writeBlockPos(pos);
        });
        return false;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView var1) {
        return new CraftingFrameBlockEntity();
    }

    @Override
    public void accept(PacketByteBuf buf) {

    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction dir = Direction.NORTH;
        for (Direction placeDir: Direction.getEntityFacingOrder(ctx.getPlayer())) {
            if (placeDir.getHorizontal() != -1) {
                dir = placeDir.getOpposite();
                break;
            }
        }
        return this.getDefaultState().with(FACING, dir);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if(placer instanceof ServerPlayerEntity) {
            showGui(world, pos, (ServerPlayerEntity) placer);
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState blockState_1, LootContext.Builder lootContext$Builder_1) {
        return super.getDroppedStacks(blockState_1, lootContext$Builder_1);
    }

    @Override
    public VoxelShape getBoundingShape(BlockState state, BlockView world, BlockPos pos) {
        switch (state.get(FACING)) {
            case NORTH:
                return NORTH_BOX;
            case EAST:
                return EAST_BOX;
            case SOUTH:
                return SOUTH_BOX;
            case WEST:
            default:
                return WEST_BOX;

        }
    }

    protected static final VoxelShape NORTH_BOX = VoxelShapes.cube(2./16, 3./16, 15./16, 14./16, 15./16, 16./16);
    protected static final VoxelShape SOUTH_BOX = VoxelShapes.cube(2./16, 3./16, 0./16, 14./16, 15./16, 1./16);
    protected static final VoxelShape EAST_BOX = VoxelShapes.cube(0./16, 3./16, 2./16, 1./16, 15./16, 14./16);
    protected static final VoxelShape WEST_BOX = VoxelShapes.cube(15./16, 3./16, 2./16, 16./16, 15./16, 14./16);

}
