package mod.codewarrior.simplecraftingframe;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.block.entity.ClientSerializable;
import net.fabricmc.fabric.client.render.BlockEntityRendererRegistry;
import net.fabricmc.fabric.impl.client.gui.GuiProviderImpl;
import net.fabricmc.fabric.impl.container.ContainerProviderImpl;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.container.FurnaceContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.block.BlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.recipe.FurnaceInputSlotFiller;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.packet.CraftRequestServerPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import static mod.codewarrior.simplecraftingframe.CraftingFrameBlock.CONTAINER_ID;

public class SimpleCraftingFrame implements ModInitializer {
	public static final String MOD_ID = "simplecraftingframe";
	public static CraftingFrameBlock CRAFTINGFRAMEBLOCK;
	public static BlockEntityType<CraftingFrameBlockEntity> CRAFTINGFRAMEENTITY;

	public static final Identifier CRAFT_REQUEST = new Identifier("simplecraftingframe", "craft_request");
	public static final Identifier FRAME_UPDATE = new Identifier("simplecraftingframe", "frame_update");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
		CRAFTINGFRAMEBLOCK = Registry.register(Registry.BLOCK, MOD_ID + ":crafting_frame", new CraftingFrameBlock(Block.Settings.of(Material.WOOD).strength(0.5f, 0.5f)));

		CRAFTINGFRAMEENTITY = Registry.register(Registry.BLOCK_ENTITY, MOD_ID + ":crafting_frame", CraftingFrameBlockEntity.TYPE);

		Registry.register(Registry.ITEM, MOD_ID + ":crafting_frame", new BlockItem(CRAFTINGFRAMEBLOCK, new Item.Settings().itemGroup(ItemGroup.DECORATIONS)));
		CustomPayloadPacketRegistry.SERVER.register(CRAFT_REQUEST, SimpleCraftingFrame::onCraftRequest);

		CustomPayloadPacketRegistry.CLIENT.register(FRAME_UPDATE, SimpleCraftingFrame::onFrameUpdate);


		ContainerProviderImpl.INSTANCE.registerFactory(CONTAINER_ID, (id, playerEntity, buf) -> new CraftingFrameContainer(playerEntity.inventory, playerEntity.world, buf.readBlockPos()));
		GuiProviderImpl.INSTANCE.registerFactory(CONTAINER_ID, (id, playerEntity, buf) -> new CraftingFrameGui(playerEntity.inventory, buf.readBlockPos()));
		BlockEntityRendererRegistry.INSTANCE.register(CraftingFrameBlockEntity.class, new CraftingFrameBlockEntityRenderer());

	}

	public static void sendFrameUpdate(CraftingFrameBlockEntity entity) {
		if(entity.getWorld().isClient) return;

		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(entity.getPos());
		buf.writeCompoundTag(entity.toClientTag(new CompoundTag()));

		CustomPayloadClientPacket packet = new CustomPayloadClientPacket(FRAME_UPDATE, buf);
		entity.getWorld().getServer().getPlayerManager().sendToAll(packet);
	}

	public static void onFrameUpdate(PacketContext ctx, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		CompoundTag tag = buf.readCompoundTag();
		BlockEntity entity = ctx.getPlayer().getEntityWorld().getBlockEntity(pos);
		if (entity instanceof ClientSerializable) {
			ClientSerializable cs = (ClientSerializable)entity;
			cs.fromClientTag(tag);
		}
	}

	public static void onCraftRequest(PacketContext ctx, PacketByteBuf buf) {
		byte syncId = buf.readByte();
		Identifier recipeId = buf.readIdentifier();

		ctx.getTaskQueue().execute(() -> {
			doCraftRequest(syncId, recipeId, (ServerPlayerEntity)ctx.getPlayer());
		});
	}

	public static void doCraftRequest(byte syncId, Identifier recipeId, ServerPlayerEntity player) {
		player.method_14234();
		if (!player.isSpectator() && player.container.syncId == syncId && player.container.method_7622(player)) {
			Recipe recipe = player.server.getRecipeManager().get(recipeId);
			if (player.container instanceof CraftingFrameContainer) {
				((CraftingFrameContainer) player.container).loadRecipe(recipe);
			}
		}
	}
}
