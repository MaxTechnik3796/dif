package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class BlazingModifierHandler{
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		Player player=event.getPlayer();
		ItemStack itemStack=player.getMainHandItem();
		Level world=(Level)event.getLevel();
		BlockPos pos=event.getPos();
		BlockState blockState=event.getState();
		if(itemStack.getOrCreateTag().getBoolean("BlazingModifier")&&itemStack.isCorrectToolForDrops(blockState)){
			List<ItemStack> drops=Block.getDrops(blockState,(ServerLevel)world,pos,world.getBlockEntity(pos),player,itemStack);
			boolean smeltedAny=false;
			for(ItemStack drop: drops){
				Optional<SmeltingRecipe> recipe=world.getRecipeManager().getRecipeFor(RecipeType.SMELTING,new SimpleContainer(drop),world);
				if(recipe.isPresent()&&!player.getAbilities().instabuild){
					ItemStack result=recipe.get().getResultItem(world.registryAccess()).copy();
					result.setCount(drop.getCount());
					Block.popResource(world,pos,result);
					smeltedAny=true;
				}
			}
			if(smeltedAny){
				world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
				event.setCanceled(true);
				world.playSound(null,pos,SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,0.5F,1.2F);
				((ServerLevel)world).sendParticles(ParticleTypes.FLAME,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,5,0.2,0.2,0.2,0.05);
			}
		}
	}
}