package cz.maxtechnik.dif.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
public class BattleAxeItem extends SwordItem{
	private static final Map<Block,Block> AXE_STRIPPABLES=(new ImmutableMap.Builder<Block,Block>())
			.put(Blocks.OAK_LOG,Blocks.STRIPPED_OAK_LOG)
			.put(Blocks.SPRUCE_LOG,Blocks.STRIPPED_SPRUCE_LOG)
			.put(Blocks.BIRCH_LOG,Blocks.STRIPPED_BIRCH_LOG)
			.put(Blocks.JUNGLE_LOG,Blocks.STRIPPED_JUNGLE_LOG)
			.put(Blocks.ACACIA_LOG,Blocks.STRIPPED_ACACIA_LOG)
			.put(Blocks.DARK_OAK_LOG,Blocks.STRIPPED_DARK_OAK_LOG)
			.put(Blocks.CRIMSON_STEM,Blocks.STRIPPED_CRIMSON_STEM)
			.put(Blocks.WARPED_STEM,Blocks.STRIPPED_WARPED_STEM)
			.build();
	public BattleAxeItem(Tier tier,float attackDamage,float attackSpeed,Properties properties){
		super(tier,properties.attributes(SwordItem.createAttributes(tier,attackDamage,attackSpeed)));
	}
	@Override
	public boolean canDisableShield(@NotNull ItemStack stack,@NotNull ItemStack shield,@NotNull LivingEntity entity,@NotNull LivingEntity attacker){
		return true;
	}
	@Override
	public float getDestroySpeed(@NotNull ItemStack stack,BlockState state){
		if(state.is(BlockTags.MINEABLE_WITH_AXE)){
			return this.getTier().getSpeed();
		}
		return super.getDestroySpeed(stack,state);
	}
	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack,BlockState state){
		return state.is(BlockTags.MINEABLE_WITH_AXE)||super.isCorrectToolForDrops(stack,state);
	}
	@Override
	public @NotNull InteractionResult useOn(UseOnContext context){
		Level level=context.getLevel();
		BlockPos blockpos=context.getClickedPos();
		Player player=context.getPlayer();
		BlockState blockstate=level.getBlockState(blockpos);
		Optional<BlockState> optional=Optional.ofNullable(AXE_STRIPPABLES.get(blockstate.getBlock())).map((block)->block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,blockstate.getValue(RotatedPillarBlock.AXIS)));
		if(optional.isPresent()){
			level.playSound(player,blockpos,SoundEvents.AXE_STRIP,SoundSource.BLOCKS,1.0F,1.0F);
			if(!level.isClientSide){
				level.setBlock(blockpos,optional.get(),11);
				if(player!=null){
					context.getItemInHand().hurtAndBreak(1,player,LivingEntity.getSlotForHand(context.getHand()));
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}else{
			return InteractionResult.PASS;
		}
	}
}