package cz.maxtechnik.dif.block;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * Destilační tank — multiblok věž.
 * Stavění: stejné jako Create Fluid Tank — postav základnu (1×1, 2×2 nebo 3×3),
 * pak klikni s dalším blokem na vrchní stěnu → automaticky se přidá patro
 */
public class DistillationTank extends FluidTankBlock{
	public DistillationTank(){
		super(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.METAL).requiresCorrectToolForDrops(),false);
		registerDefaultState(defaultBlockState().setValue(TOP,true)
				.setValue(BOTTOM,true)
				.setValue(SHAPE,Shape.PLAIN));
	}
	@Override
	public InteractionResult onWrenched(BlockState state,UseOnContext context){
		return InteractionResult.PASS;
	}
	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType(){
		return DifModBlockEntities.DISTILLATION_TANK.get();
	}
	//Ticker — Create logiku + naši recipe logiku
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState blockState,BlockEntityType<T> type){
		BlockEntityTicker<T> createTicker=super.getTicker(level,blockState,type);
		if(level.isClientSide) return createTicker;
		if(type!=DifModBlockEntities.DISTILLATION_TANK.get()) return createTicker;
		return (lvl,pos,state,be)->{
			if(createTicker!=null) createTicker.tick(lvl,pos,state,be);
			if(be instanceof DistillationTankBlockEntity dbe){
				DistillationTankBlockEntity.serverTick(lvl,dbe);
			}
		};
	}
	//Sousedící blok se změnil → invaliduj cache věže
	@Override
	public void neighborChanged(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block neighborBlock,@NotNull BlockPos fromPos,boolean isMoving){
		super.neighborChanged(blockState,level,pos,neighborBlock,fromPos,isMoving);
		// Zajímají nás jen změny bezprostředně pod nebo nad námi
		if(!fromPos.equals(pos.above())&&!fromPos.equals(pos.below())) return;
		if(level.getBlockEntity(pos) instanceof DistillationTankBlockEntity dbe){
			DistillationTankBlockEntity master=dbe.getTowerMaster();
			if(master!=null) master.notifyMultiUpdated();
		}
	}
	@Override
	public void onPlace(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,
	                    @NotNull BlockState oldState,boolean isMoving){
		super.onPlace(state,level,pos,oldState,isMoving);
		if(level.isClientSide) return;
		if(state.getBlock()==oldState.getBlock()) return;

		// Notify block above and below to update their tower states
		if(level.getBlockEntity(pos.above()) instanceof DistillationTankBlockEntity above){
			above.updateTowerState(false);
		}
		if(level.getBlockEntity(pos.below()) instanceof DistillationTankBlockEntity below){
			below.updateTowerState(false);
		}

		forceConnectivityUpdateInArea(level,pos);
	}
	@Override
	public void onRemove(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,
	                     @NotNull BlockState newState,boolean isMoving){
		super.onRemove(state,level,pos,newState,isMoving);
		if(level.isClientSide) return;
		if(state.getBlock()==newState.getBlock()) return;

		// Notify block above and below to update their tower states
		if(level.getBlockEntity(pos.above()) instanceof DistillationTankBlockEntity above){
			above.updateTowerState(false);
		}
		if(level.getBlockEntity(pos.below()) instanceof DistillationTankBlockEntity below){
			below.updateTowerState(false);
		}

		forceConnectivityUpdateInArea(level,pos);
	}
	private void forceConnectivityUpdateInArea(Level level,BlockPos pos){
		if(level instanceof net.minecraft.server.level.ServerLevel serverLevel){
			serverLevel.getServer().tell(new net.minecraft.server.TickTask(
					serverLevel.getServer().getTickCount()+1,
					()->rebuildArea(serverLevel,pos)
			));
		}
	}
	private void rebuildArea(Level level,BlockPos pos){
		java.util.List<DistillationTankBlockEntity> tanks=new java.util.ArrayList<>();
		for(int x=-2;x<=2;x++){
			for(int z=-2;z<=2;z++){
				BlockEntity be=level.getBlockEntity(pos.offset(x,0,z));
				if(be instanceof DistillationTankBlockEntity tank){
					tanks.add(tank);
				}
			}
		}
		// First, split all existing multiblocks in the area to start fresh
		for(DistillationTankBlockEntity tank: tanks){
			com.simibubi.create.api.connectivity.ConnectivityHandler.splitMulti(tank);
		}
		// Re-collect tanks after split to ensure we have the fresh 1x1 states
		tanks.clear();
		for(int x=-2;x<=2;x++){
			for(int z=-2;z<=2;z++){
				BlockEntity be=level.getBlockEntity(pos.offset(x,0,z));
				if(be instanceof DistillationTankBlockEntity tank){
					tanks.add(tank);
				}
			}
		}
		// Sort tanks Northwest-to-Southeast (smallest X and Z first)
		tanks.sort((a,b)->{
			int cmpX=Integer.compare(a.getBlockPos().getX(),b.getBlockPos().getX());
			if(cmpX!=0) return cmpX;
			return java.lang.Integer.compare(a.getBlockPos().getZ(),b.getBlockPos().getZ());
		});
		// Form new multiblocks starting from the NW-most block of each group
		for(DistillationTankBlockEntity tank: tanks){
			if(tank.isController()){
				com.simibubi.create.api.connectivity.ConnectivityHandler.formMulti(tank);
			}
		}
		// Force vertical visual update on all tanks in the area and sync data to clients
		for(DistillationTankBlockEntity tank: tanks){
			tank.updateTowerState(true);
			tank.sendData();
		}
	}
}