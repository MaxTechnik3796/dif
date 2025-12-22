package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;
import org.jetbrains.annotations.NotNull;
public class MataPlant extends SugarCaneBlock implements BonemealableBlock{
	private static final TagKey<net.minecraft.world.level.block.Block> FLOWER_PLANT_SOIL=BlockTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"flower_plant_soil"));
	public MataPlant(){
		super(Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).randomTicks().sound(SoundType.GRASS).instabreak().noCollission().offsetType(OffsetType.XZ).pushReaction(PushReaction.DESTROY));
	}
	@Override
	public int getFlammability(BlockState state,BlockGetter world,BlockPos pos,Direction face){
		return 100;
	}
	@Override
	public int getFireSpreadSpeed(BlockState state,BlockGetter world,BlockPos pos,Direction face){
		return 60;
	}
	@Override
	public boolean canSurvive(@NotNull BlockState blockstate,LevelReader worldIn,BlockPos pos){
		BlockPos blockpos=pos.below();
		BlockState groundState=worldIn.getBlockState(blockpos);
		return groundState.is(this)||groundState.is(FLOWER_PLANT_SOIL);
	}
	@Override
	public @NotNull PlantType getPlantType(@NotNull BlockGetter world,@NotNull BlockPos pos){
		return PlantType.PLAINS;
	}
	@Override
	public void randomTick(@NotNull BlockState blockstate,ServerLevel world,BlockPos pos,@NotNull RandomSource random){
		if(world.isEmptyBlock(pos.above())){
			int i=1;
			BlockPos checkPos=pos.below();
			while(world.getBlockState(checkPos).is(this)){
				i++;
				checkPos=checkPos.below();
			}
			if(i<DifModCommonConfig.mataPlantMaxHeight){
				int j=blockstate.getValue(AGE);
				if(ForgeHooks.onCropsGrowPre(world,pos,blockstate,true)){
					if(j==15){
						world.setBlockAndUpdate(pos.above(),defaultBlockState());
						world.setBlock(pos,blockstate.setValue(AGE,0),4);
					}else{
						world.setBlock(pos,blockstate.setValue(AGE,j+1),4);
					}
				}
			}
		}
	}
	@Override
	public boolean isValidBonemealTarget(@NotNull LevelReader worldIn,@NotNull BlockPos pos,@NotNull BlockState blockstate,boolean clientSide){
		return true;
	}
	@Override
	public boolean isBonemealSuccess(@NotNull Level world,@NotNull RandomSource random,@NotNull BlockPos pos,@NotNull BlockState blockstate){
		return true;
	}
	@Override
	public void performBonemeal(ServerLevel world,@NotNull RandomSource random,BlockPos pos,@NotNull BlockState blockstate){
		if(world.isEmptyBlock(pos.above())&&DifMod.rouletteBoolean(3)){
			world.setBlock(BlockPos.containing(pos.getX(),pos.getY()+1,pos.getZ()),DifModBlocks.MATA_PLANT.get().defaultBlockState(),3);
		}
	}
}
