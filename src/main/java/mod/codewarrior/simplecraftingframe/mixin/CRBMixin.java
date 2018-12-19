package mod.codewarrior.simplecraftingframe.mixin;

import com.google.common.collect.Lists;
import mod.codewarrior.simplecraftingframe.CraftingFrameContainer;
import net.minecraft.client.recipe.book.ClientRecipeBook;
import net.minecraft.client.recipe.book.RecipeBookGroup;
import net.minecraft.container.Container;
import net.minecraft.recipe.book.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientRecipeBook.class)
public class CRBMixin extends RecipeBook {
    @Inject(at = @At("HEAD"), method = "getGroupsForContainer", cancellable = true)
    private static void getGroupsForContainer(Container container, CallbackInfoReturnable<List<RecipeBookGroup>> info)
    {
        if(!(container instanceof CraftingFrameContainer)) return;

        info.setReturnValue(
                Lists.newArrayList(
                        RecipeBookGroup.SEARCH,
                        RecipeBookGroup.EQUIPMENT,
                        RecipeBookGroup.BUILDING_BLOCKS,
                        RecipeBookGroup.MISC,
                        RecipeBookGroup.REDSTONE));
        info.cancel();


    }

}
