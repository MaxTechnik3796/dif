package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class CanolaPlant extends FlowerBlock implements BonemealableBlock{
	private static final TagKey<Block> FLOWER_PLANT_SOIL=BlockTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"flower_plant_soil"));
	public CanolaPlant(){
		super(new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(MobEffects.CONFUSION,100))),BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).sound(SoundType.GRASS).instabreak().noCollission().offsetType(BlockBehaviour.OffsetType.NONE).pushReaction(PushReaction.DESTROY));
	}
	@Override
	public int getFlammability(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull Direction face){
		return 100;
	}
	@Override
	public int getFireSpreadSpeed(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull Direction face){
		return 60;
	}
	@Override
	public boolean mayPlaceOn(BlockState groundState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return groundState.is(FLOWER_PLANT_SOIL);
	}
	@Override
	public boolean canSurvive(@NotNull BlockState blockstate,LevelReader worldIn,BlockPos pos){
		BlockPos blockpos=pos.below();
		BlockState groundState=worldIn.getBlockState(blockpos);
		return this.mayPlaceOn(groundState,worldIn,blockpos);
	}
	@Override
	public boolean isValidBonemealTarget(@NotNull LevelReader worldIn,@NotNull BlockPos pos,@NotNull BlockState blockstate){
		return true;
	}
	@Override
	public boolean isBonemealSuccess(@NotNull Level world,@NotNull RandomSource random,@NotNull BlockPos pos,@NotNull BlockState blockstate){
		return true;
	}
	@Override
	public void performBonemeal(@NotNull ServerLevel world,@NotNull RandomSource random,BlockPos pos,@NotNull BlockState blockstate){
		ItemEntity entityToSpawn=new ItemEntity(world,pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,new ItemStack(DifModBlocks.CANOLA_PLANT.get()));
		entityToSpawn.setPickUpDelay(10);
		world.addFreshEntity(entityToSpawn);
	}
}
