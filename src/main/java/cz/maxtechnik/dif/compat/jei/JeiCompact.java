package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
public class JeiCompact{
	public static abstract class Plugin implements IModPlugin{
		private final ResourceLocation uid;
		protected Plugin(String id){
			this.uid=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,id);
		}
		@Override
		public @NotNull ResourceLocation getPluginUid(){
			return uid;
		}
	}
	public static abstract class Category<T> implements IRecipeCategory<T>{
		private final RecipeType<T> type;
		private final Component title;
		private final IDrawable background;
		private final IDrawable icon;
		protected Category(IGuiHelper helper,int w,int h,ItemStack iconStack,RecipeType<T> type,String titleKey){
			this.type=type;
			this.title=Component.translatable(titleKey);
			this.background=helper.createBlankDrawable(w,h);
			this.icon=helper.createDrawableItemStack(iconStack);
		}
		@Override
		public @NotNull RecipeType<T> getRecipeType(){
			return type;
		}
		@Override
		public @NotNull Component getTitle(){
			return title;
		}
		@SuppressWarnings("removal")
		@Override
		public @NotNull IDrawable getBackground(){
			return background;
		}
		@Override
		public @NotNull IDrawable getIcon(){
			return icon;
		}
	}
}
