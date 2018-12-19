package mod.codewarrior.simplecraftingframe;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformations;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.Direction;

public class CraftingFrameBlockEntityRenderer extends BlockEntityRenderer<CraftingFrameBlockEntity> {
    @Override
    public void render(CraftingFrameBlockEntity entity, double double_1, double double_2, double double_3, float float_1, int int_1) {
        ItemStack ItemStack_1 = entity.getOutputStack();
        if (!ItemStack_1.isEmpty()) {
            BlockState state = entity.getWorld().getBlockState(entity.getPos());
            Direction dir = state.get(CraftingFrameBlock.field_11177);

            GlStateManager.pushMatrix();
            GlStateManager.translated(double_1, double_2, double_3);
            GlStateManager.translated(0.5, 0.5, 0.5);
            GlStateManager.rotated(180 - dir.asRotation(), 0., 1., 0.);
            GlStateManager.translated(0.0, 0.0, 7.5/16);
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);



            MinecraftClient.getInstance().getItemRenderer()
                    .renderItemWithTransformation(ItemStack_1, ModelTransformations.Type.FIXED);

            GlStateManager.popMatrix();
        }
    }
}
