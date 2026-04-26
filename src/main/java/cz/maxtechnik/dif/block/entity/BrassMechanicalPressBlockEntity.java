package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;
public class BrassMechanicalPressBlockEntity extends MechanicalPressBlockEntity{
	public BrassMechanicalPressBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.BRASS_MECHANICAL_PRESS.get(),pos,state);
	}
	@Override
	public float calculateStressApplied(){
		float impact=8.F*4;
		this.lastStressApplied=impact;
		return impact;
	}
	@Override
	public float calculateAddedStressCapacity(){
		return 0;
	}
	@Override
	protected void applyBasinRecipe(){
		if(currentRecipe==null) return;
		for(int i=0;i<4;i++){
			if(!matchBasinRecipe(currentRecipe)) break;
			super.applyBasinRecipe();
		}
	}
	@Override
	public boolean tryProcessInWorld(ItemEntity itemEntity,boolean simulate){
		ItemStack item=itemEntity.getItem();
		Optional<PressingRecipe> recipe=getRecipe(item);
		if(recipe.isEmpty()) return false;
		if(simulate) return true;
		pressingBehaviour.particleItems.add(item);
		// Process up to 4 items
		int amountToProcess=Math.min(item.getCount(),4);
		for(int i=0;i<amountToProcess;i++){
			ItemStack singleItem=ItemHandlerHelper.copyStackWithSize(item,1);
			List<ItemStack> results=RecipeApplier.applyRecipeOn(level,singleItem,recipe.get(),true);
			for(ItemStack result: results){
				if(!result.isEmpty()){
					onItemPressed(result);
					assert level!=null;
					ItemEntity created=new ItemEntity(level,itemEntity.getX(),itemEntity.getY(),itemEntity.getZ(),result);
					created.setDefaultPickUpDelay();
					created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO,level.random,0.05F));
					level.addFreshEntity(created);
				}
			}
			item.shrink(1);
			if(item.isEmpty()) break;
		}
		return true;
	}
	@Override
	public boolean tryProcessOnBelt(TransportedItemStack input,List<ItemStack> outputList,boolean simulate){
		Optional<PressingRecipe> recipe=getRecipe(input.stack);
		if(recipe.isEmpty()) return false;
		if(simulate) return true;
		pressingBehaviour.particleItems.add(input.stack);
		// Process up to 4 items from the belt stack
		int amountToProcess=Math.min(input.stack.getCount(),4);
		for(int i=0;i<amountToProcess;i++){
			ItemStack singleItem=ItemHandlerHelper.copyStackWithSize(input.stack,1);
			List<ItemStack> outputs=RecipeApplier.applyRecipeOn(level,singleItem,recipe.get(),true);
			for(ItemStack created: outputs){
				if(!created.isEmpty()){
					onItemPressed(created);
					outputList.add(created);
				}
			}
			input.stack.shrink(1);
			if(input.stack.isEmpty()) break;
		}
		return true;
	}
}
