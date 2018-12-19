package mod.codewarrior.simplecraftingframe.mixin;

import mod.codewarrior.simplecraftingframe.CraftingFrameGui;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.container.RecipeBookGui;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeBookGui.class)
public class RBGMixin extends Drawable {
    @Redirect(at = @At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickRecipe(ILnet/minecraft/recipe/Recipe;Z)V"), method="mouseClicked")
    public void clickRecipe(ClientPlayerInteractionManager obj, int int_1, Recipe Recipe_1, boolean boolean_1) {
        if (((RecipeBookGui)(Object) this) instanceof CraftingFrameGui.CFRecipeBookGui) {
            return;
        } else {
            obj.clickRecipe(int_1, Recipe_1, boolean_1);
        }
    }
}
