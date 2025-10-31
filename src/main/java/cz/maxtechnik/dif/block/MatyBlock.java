
package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.init.DifModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MatyBlock extends Block {
	public MatyBlock() {
		super(Properties.of().sound(SoundType.MOSS).strength(0.9F,7F));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
		return 15;
	}
	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return true;
	}
	@Override
	public void stepOn(@NotNull Level world,@NotNull BlockPos pos,@NotNull BlockState blockstate,@NotNull Entity entity){
		super.stepOn(world,pos,blockstate,entity);
		if(entity instanceof LivingEntity livingEntity){
			livingEntity.addEffect(new MobEffectInstance(DifModMobEffects.REDSTONE_IQ.get(),60,0));
		}
	}
}
