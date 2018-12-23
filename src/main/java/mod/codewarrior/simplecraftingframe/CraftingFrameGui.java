package mod.codewarrior.simplecraftingframe;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.container.RecipeBookGui;
import net.minecraft.client.gui.ingame.RecipeBookProvider;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.SlotActionType;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class CraftingFrameGui extends ContainerGui implements RecipeBookProvider {

    private static final Identifier BG_TEX = new Identifier("textures/gui/container/crafting_table.png");
    private static final Identifier field_2881 = new Identifier("textures/gui/recipe_button.png");
    private final RecipeBookGui recipeBookGui;
    private boolean isNarrow;
    private final PlayerInventory playerInventory;

    public CraftingFrameGui(PlayerInventory playerInventory, BlockPos pos) {
        super(new CraftingFrameContainer(playerInventory, playerInventory.player.world, pos));
        this.recipeBookGui = new CFRecipeBookGui();
        this.playerInventory = playerInventory;
    }

    protected void onInitialized() {
        super.onInitialized();
        this.isNarrow = this.width < 379;
        this.recipeBookGui.initialize(this.width, this.height, this.client, this.isNarrow, (CraftingContainer) this.container);
        this.left = this.recipeBookGui.findLeftEdge(this.isNarrow, this.width, this.containerWidth);
        this.listeners.add(this.recipeBookGui);
        /*this.addButton(new RecipeBookButtonWidget(10, this.left + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, field_2881) {
            public void onPressed(double var1, double var3) {
                CraftingFrameGui.this.recipeBookGui.reset(CraftingFrameGui.this.isNarrow);
                CraftingFrameGui.this.recipeBookGui.toggleOpen();
                CraftingFrameGui.this.left = CraftingFrameGui.this.recipeBookGui.findLeftEdge(CraftingFrameGui.this.isNarrow, CraftingFrameGui.this.width, CraftingFrameGui.this.containerWidth);
                this.setPos(CraftingFrameGui.this.left + 5, CraftingFrameGui.this.height / 2 - 49);
            }
        });*/
    }

    public GuiEventListener getFocused() {
        return this.recipeBookGui;
    }

    public void update() {
        super.update();
        this.recipeBookGui.update();
    }

    public void draw(int var1, int var2, float var3) {
        this.drawBackground();
        if (this.recipeBookGui.isOpen() && this.isNarrow) {
            this.drawBackground(var3, var1, var2);
            this.recipeBookGui.draw(var1, var2, var3);
        } else {
            this.recipeBookGui.draw(var1, var2, var3);
            super.draw(var1, var2, var3);
            this.recipeBookGui.drawGhostSlots(this.left, this.top, true, var3);
        }

        this.drawMousoverTooltip(var1, var2);
        this.recipeBookGui.drawTooltip(this.left, this.top, var1, var2);
    }


    protected void drawForeground(int var1, int var2) {
        this.fontRenderer.draw(I18n.translate("container.crafting_frame"), 28.0F, 6.0F, 4210752);
        this.fontRenderer.draw(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float) (this.containerHeight - 96 + 2), 4210752);
    }

    protected void drawBackground(float var1, int var2, int var3) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(BG_TEX);
        int var4 = this.left;
        int var5 = (this.height - this.containerHeight) / 2;
        this.drawTexturedRect(var4, var5, 0, 0, this.containerWidth, this.containerHeight);
    }

    protected boolean isPointWithinBounds(int var1, int var2, int var3, int var4, double var5, double var7) {
        return (!this.isNarrow || !this.recipeBookGui.isOpen()) && super.isPointWithinBounds(var1, var2, var3, var4, var5, var7);
    }

    public boolean mouseClicked(double var1, double var3, int var5) {
        if (this.recipeBookGui.mouseClicked(var1, var3, var5)) {
            return true;
        } else {
            return this.isNarrow && this.recipeBookGui.isOpen() ? true : super.mouseClicked(var1, var3, var5);
        }
    }

    protected boolean isClickOutsideBounds(double var1, double var3, int var5, int var6, int var7) {
        boolean var8 = var1 < (double) var5 || var3 < (double) var6 || var1 >= (double) (var5 + this.containerWidth) || var3 >= (double) (var6 + this.containerHeight);
        return this.recipeBookGui.isClickOutsideBounds(var1, var3, this.left, this.top, this.containerWidth, this.containerHeight, var7) && var8;
    }

    protected void onMouseClick(Slot var1, int var2, int var3, SlotActionType var4) {
        super.onMouseClick(var1, var2, var3, var4);
        this.recipeBookGui.slotClicked(var1);
    }

    public void onClosed() {
        this.recipeBookGui.close();
        super.onClosed();
    }

    @Override
    public void refreshRecipeBook() {
        this.recipeBookGui.refresh();
    }

    public RecipeBookGui getRecipeBookGui() {
        return this.recipeBookGui;
    }

    public static class CFRecipeBookGui extends RecipeBookGui {}
}
