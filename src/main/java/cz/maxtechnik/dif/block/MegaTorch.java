package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.TorchSavedData; // Zkontroluj, že odpovídá tvému balíčku!
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class MegaTorch extends Block{
	public static final BooleanProperty FORMED=BooleanProperty.create("formed");
	public static final IntegerProperty PART=IntegerProperty.create("part",0,4);
	public MegaTorch(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FORMED,false).setValue(PART,0));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FORMED,PART);
	}
	@Override
	public void onPlace(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean isMoving){
		if(!level.isClientSide) checkMultiblock(level,pos);
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		if(!level.isClientSide) checkMultiblock(level,pos);
	}
	// NOVÉ: Extrémně důležité pro zabránění "neviditelných" torčí, když hráč blok rozbije
	@Override
	public void onRemove(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())){
			// Pokud byl zničený blok součástí zformované torče
			if(!level.isClientSide&&level instanceof ServerLevel serverLevel&&state.getValue(FORMED)){
				// Dopočítáme, kde byl spodek torče na základě toho, jakou část (PART) hráč zničil
				BlockPos bottom=pos.below(state.getValue(PART));
				TorchSavedData.get(serverLevel).removeTorch(bottom);
			}
			super.onRemove(state,level,pos,newState,isMoving);
		}
	}
	private void checkMultiblock(Level level,BlockPos pos){
		if(!level.getBlockState(pos).is(this)) return;
		BlockPos bottom=findBottom(level,pos);
		int height=countHeight(level,bottom);
		boolean isValid=height==5&&noHorizontalNeighbors(level,bottom);
		boolean isCurrentlyFormed=level.getBlockState(bottom).getValue(FORMED);
		if(isValid&&!isCurrentlyFormed){
			for(int i=0;i<5;i++){
				level.setBlock(bottom.above(i),this.defaultBlockState().setValue(FORMED,true).setValue(PART,i),3);
			}
			if(level instanceof ServerLevel serverLevel){
				TorchSavedData.get(serverLevel).addTorch(bottom);
			}
		}else if(!isValid&&isCurrentlyFormed){
			for(int i=0;i<height;i++){
				BlockState s=level.getBlockState(bottom.above(i));
				if(s.is(this)&&s.getValue(FORMED)){
					level.setBlock(bottom.above(i),s.setValue(FORMED,false).setValue(PART,0),3);
				}
			}
			if(level instanceof ServerLevel serverLevel){
				TorchSavedData.get(serverLevel).removeTorch(bottom);
			}
		}
	}
	private BlockPos findBottom(Level level,BlockPos pos){
		BlockPos current=pos;
		while(level.getBlockState(current.below()).is(this)){
			current=current.below();
		}
		return current;
	}
	private int countHeight(Level level,BlockPos bottom){
		int height=0;
		while(level.getBlockState(bottom.above(height)).is(this)){
			height++;
		}
		return height;
	}
	private boolean noHorizontalNeighbors(Level level,BlockPos bottom){
		for(int i=0;i<5;i++){
			for(Direction dir: Direction.Plane.HORIZONTAL){
				if(level.getBlockState(bottom.above(i).relative(dir)).is(this)) return false;
			}
		}
		return true;
	}
	public static final TagKey<EntityType<?>> BLOCKED_MOBS=TagKey.create(
			Registries.ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"mega_torch_blocked")
	);
}