package cz.maxtechnik.dif.block.mata;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
public class MataPlant extends SugarCaneBlock implements BonemealableBlock{
	// Sugar cane roste při AGE 15 -> reset, my rosteme při AGE 7 (2x rychleji)
	private static final int GROWTH_AGE=7;
	private static final TagKey<net.minecraft.world.level.block.Block> FLOWER_PLANT_SOIL=
			BlockTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"flower_plant_soil"));
	public MataPlant(){
		super(Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.randomTicks()
				.sound(SoundType.GRASS)
				.instabreak()
				.noCollission()
				.offsetType(OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY));
	}
	@Override
	public boolean canSurvive(@NotNull BlockState blockstate,LevelReader worldIn,BlockPos pos){
		BlockPos blockpos=pos.below();
		BlockState groundState=worldIn.getBlockState(blockpos);
		return groundState.is(this)||groundState.is(FLOWER_PLANT_SOIL);
	}
	@Override
	public void randomTick(@NotNull BlockState blockstate,@NotNull ServerLevel world,
	                       @NotNull BlockPos pos,@NotNull RandomSource random){
		if(!world.isEmptyBlock(pos.above())) return;
		// Počítej výšku – optimalizováno early return
		int height=1;
		BlockPos checkPos=pos.below();
		while(world.getBlockState(checkPos).is(this)){
			height++;
			if(height>=DifModCommonConfig.mataPlantMaxHeight) return; // už dost vysoké
			checkPos=checkPos.below();
		}
		int age=blockstate.getValue(AGE);
		if(age>=GROWTH_AGE){
			world.setBlockAndUpdate(pos.above(),defaultBlockState());
			world.setBlock(pos,blockstate.setValue(AGE,0),4);
		}else{
			world.setBlock(pos,blockstate.setValue(AGE,age+1),4);
		}
	}
	@Override
	public boolean isValidBonemealTarget(@NotNull LevelReader worldIn,@NotNull BlockPos pos,
	                                     @NotNull BlockState blockstate){
		return true;
	}
	@Override
	public boolean isBonemealSuccess(@NotNull Level world,@NotNull RandomSource random,
	                                 @NotNull BlockPos pos,@NotNull BlockState blockstate){
		return true;
	}
	@Override
	public void performBonemeal(@NotNull ServerLevel world,@NotNull RandomSource random,
	                            @NotNull BlockPos pos,@NotNull BlockState blockstate){
		if(world.isEmptyBlock(pos.above())&&DifMod.rouletteBoolean(3)){
			world.setBlock(
					pos.above(),
					DifModBlocks.MATA_PLANT.get().defaultBlockState(),
					3
			);
		}
	}
}