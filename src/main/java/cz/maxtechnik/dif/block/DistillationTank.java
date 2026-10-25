package cz.maxtechnik.dif.block;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.FluidFormatter;
import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class DistillationTank extends FluidTankBlock{
	public DistillationTank(){
		super(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.METAL).requiresCorrectToolForDrops(),false);
		registerDefaultState(defaultBlockState().setValue(TOP,true).setValue(BOTTOM,true).setValue(SHAPE,Shape.PLAIN));
	}
	@Override
	public InteractionResult onWrenched(BlockState state,UseOnContext context){
		return InteractionResult.PASS;
	}
	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType(){
		return DifModBlockEntities.DISTILLATION_TANK.get();
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState blockState,BlockEntityType<T> type){
		BlockEntityTicker<T> createTicker=super.getTicker(level,blockState,type);
		if(level.isClientSide||type!=DifModBlockEntities.DISTILLATION_TANK.get()) return createTicker;
		return (lvl,pos,state,be)->{
			if(createTicker!=null) createTicker.tick(lvl,pos,state,be);
			if(be instanceof DistillationTankBlockEntity dbe) DistillationTankBlockEntity.serverTick(lvl,dbe);
		};
	}
	@Override
	public @NotNull BlockState updateShape(@NotNull BlockState state,@NotNull Direction direction,@NotNull BlockState neighborState,@NotNull LevelAccessor level,@NotNull BlockPos currentPos,@NotNull BlockPos neighborPos){
		if(direction==Direction.DOWN) return state.setValue(BOTTOM,!(neighborState.getBlock() instanceof DistillationTank));
		if(direction==Direction.UP) return state.setValue(TOP,!(neighborState.getBlock() instanceof DistillationTank));
		return super.updateShape(state,direction,neighborState,level,currentPos,neighborPos);
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block neighborBlock,@NotNull BlockPos fromPos,boolean isMoving){
		super.neighborChanged(blockState,level,pos,neighborBlock,fromPos,isMoving);
		if(!fromPos.equals(pos.above())&&!fromPos.equals(pos.below())) return;
		if(level.getBlockEntity(pos) instanceof DistillationTankBlockEntity dbe){
			DistillationTankBlockEntity master=dbe.getTowerMaster();
			if(master!=null) master.notifyMultiUpdated();
		}
	}
	@Override
	public void onPlace(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean isMoving){
		super.onPlace(state,level,pos,oldState,isMoving);
		if(!level.isClientSide&&state.getBlock()!=oldState.getBlock()) scheduleRebuild(level,pos);
	}
	@Override
	public void onRemove(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState newState,boolean isMoving){
		super.onRemove(state,level,pos,newState,isMoving);
		if(!level.isClientSide&&state.getBlock()!=newState.getBlock()) scheduleRebuild(level,pos);
	}
	private void scheduleRebuild(Level level,BlockPos pos){
		if(level instanceof ServerLevel sl){
			sl.getServer().tell(new TickTask(sl.getServer().getTickCount()+1,()->rebuildArea(sl,pos)));
		}
	}
	private void rebuildArea(Level level,BlockPos pos){
		List<DistillationTankBlockEntity> tanks=new ArrayList<>();
		for(int x=-2;x<=2;x++){
			for(int z=-2;z<=2;z++){
				if(level.getBlockEntity(pos.offset(x,0,z)) instanceof DistillationTankBlockEntity tank) tanks.add(tank);
			}
		}
		for(DistillationTankBlockEntity tank: tanks) ConnectivityHandler.splitMulti(tank);
		tanks.sort(Comparator.comparingInt((DistillationTankBlockEntity a)->a.getBlockPos().getX()).thenComparingInt(a->a.getBlockPos().getZ()));
		for(DistillationTankBlockEntity tank: tanks){
			if(tank.isController()) ConnectivityHandler.formMulti(tank);
		}
		for(DistillationTankBlockEntity tank: tanks){
			BlockPos p=tank.getBlockPos();
			BlockState s=level.getBlockState(p);
			if(s.getBlock() instanceof DistillationTank){
				BlockState updated=s.setValue(BOTTOM,!(level.getBlockState(p.below()).getBlock() instanceof DistillationTank))
						.setValue(TOP,!(level.getBlockState(p.above()).getBlock() instanceof DistillationTank));
				if(s!=updated) level.setBlock(p,updated,3);
			}
			tank.sendData();
		}
	}
	public static class TankItem extends BlockItem{
		public TankItem(Block block,Properties properties){
			super(block,properties);
		}
		@Override
		public @NotNull InteractionResult place(@NotNull BlockPlaceContext ctx){
			InteractionResult res=super.place(ctx);
			if(res.consumesAction()) tryMultiPlace(ctx);
			return res;
		}
		private void tryMultiPlace(BlockPlaceContext ctx){
			Player player=ctx.getPlayer();
			if(player==null||player.isShiftKeyDown()) return;
			Direction face=ctx.getClickedFace();
			if(!face.getAxis().isVertical()||SymmetryWandItem.presentInHotbar(player)) return;
			Level world=ctx.getLevel();
			BlockPos pos=ctx.getClickedPos();
			BlockPos placedOnPos=pos.relative(face.getOpposite());
			if(!(world.getBlockState(placedOnPos).getBlock() instanceof DistillationTank)) return;
			DistillationTankBlockEntity tankAt=ConnectivityHandler.partAt(DifModBlockEntities.DISTILLATION_TANK.get(),world,placedOnPos);
			if(tankAt==null) return;
			DistillationTankBlockEntity controller=(DistillationTankBlockEntity)tankAt.getControllerBE();
			if(controller==null||controller.getWidth()==1) return;
			int width=controller.getWidth();
			BlockPos startPos=face==Direction.DOWN?controller.getBlockPos().below():controller.getBlockPos().above(controller.getHeight());
			if(startPos.getY()!=pos.getY()) return;
			int needed=0;
			for(int x=0;x<width;x++){
				for(int z=0;z<width;z++){
					BlockPos offsetPos=startPos.offset(x,0,z);
					BlockState state=world.getBlockState(offsetPos);
					if(state.getBlock() instanceof DistillationTank) continue;
					if(!state.canBeReplaced()||!world.getEntitiesOfClass(LivingEntity.class,new AABB(offsetPos)).isEmpty()) return;
					needed++;
				}
			}
			if(!player.isCreative()&&ctx.getItemInHand().getCount()<needed) return;
			for(int x=0;x<width;x++){
				for(int z=0;z<width;z++){
					BlockPos offsetPos=startPos.offset(x,0,z);
					if(world.getBlockState(offsetPos).getBlock() instanceof DistillationTank) continue;
					player.getPersistentData().putBoolean("SilenceTankSound",true);
					super.place(BlockPlaceContext.at(ctx,offsetPos,face));
					player.getPersistentData().remove("SilenceTankSound");
				}
			}
		}
	}
	public static class TankDisplaySource extends SingleLineDisplaySource{
		@Override
		protected MutableComponent provideLine(DisplayLinkContext context,DisplayTargetStats stats){
			if(!(context.getSourceBlockEntity() instanceof FluidTankBlockEntity tank)) return EMPTY_LINE;
			FluidTankBlockEntity controller=tank.getControllerBE();
			if(controller==null) controller=tank;
			net.neoforged.neoforge.fluids.IFluidTank inv=controller.getTank(0);
			if(inv==null) return EMPTY_LINE;
			FluidStack fluid=inv.getFluid();
			String cap=FluidFormatter.asString(inv.getCapacity(),false);
			if(fluid.isEmpty()){
				return Component.literal("Empty (0mB / "+cap+")");
			}
			return fluid.getHoverName().copy()
					.append(": ")
					.append(FluidFormatter.asString(fluid.getAmount(),false))
					.append(" / ")
					.append(cap);
		}
		@Override
		protected boolean allowsLabeling(DisplayLinkContext context){
			return true;
		}
	}
}