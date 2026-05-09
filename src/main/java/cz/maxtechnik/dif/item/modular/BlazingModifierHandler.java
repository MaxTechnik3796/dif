package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.GAME)
public class BlazingModifierHandler{
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		Player player=event.getPlayer();
		ItemStack itemStack=player.getMainHandItem();
		Level world=(Level)event.getLevel();
		BlockPos pos=event.getPos();
		BlockState blockState=event.getState();
		// 1. OPRAVA: V 1.21 se k NBT (custom datům) přistupuje přes DataComponents.CUSTOM_DATA
		// Předpokládám, že vaše "D" mělo být DataComponents.CUSTOM_DATA
		var customData=itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
		if(customData!=null&&customData.copyTag().getBoolean("BlazingModifier")&&itemStack.isCorrectToolForDrops(blockState)){
			List<ItemStack> drops=Block.getDrops(blockState,(ServerLevel)world,pos,world.getBlockEntity(pos),player,itemStack);
			boolean smeltedAny=false;
			for(ItemStack drop: drops){
				// 2. OPRAVA: Použití SingleRecipeInput místo SimpleContainer
				SingleRecipeInput input=new SingleRecipeInput(drop);
				// getRecipeFor nyní vrací Optional<RecipeHolder<SmeltingRecipe>>
				var recipeOptional=world.getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,world);
				if(recipeOptional.isPresent()&&!player.getAbilities().instabuild){
					// 3. OPRAVA: Získání výsledku přes assemble (v 1.21 preferovaný způsob)
					// Musíme jít přes .value() z RecipeHolderu
					ItemStack result=recipeOptional.get().value().assemble(input,world.registryAccess()).copy();
					result.setCount(drop.getCount());
					Block.popResource(world,pos,result);
					smeltedAny=true;
				}else{
					// Pokud recept neexistuje, vypadneme standardní drop
					Block.popResource(world,pos,drop);
				}
			}
			if(smeltedAny){
				world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
				event.setCanceled(true); // Zrušíme původní dropy
				world.playSound(null,pos,SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,0.5F,1.2F);
				((ServerLevel)world).sendParticles(ParticleTypes.FLAME,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,5,0.2,0.2,0.2,0.05);
			}
		}
	}
}