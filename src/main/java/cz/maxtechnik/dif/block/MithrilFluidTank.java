package cz.maxtechnik.dif.block;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("unchecked")
public class MithrilFluidTank extends FluidTankBlock{
	public MithrilFluidTank(){
		super(BlockBehaviour.Properties.of()
				.strength(4.0F,5.0F)
				.sound(SoundType.METAL)
				.noOcclusion()
				.isRedstoneConductor((p1,p2,p3)->true)
				.requiresCorrectToolForDrops(),false);
	}
	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType(){
		return DifModBlockEntities.MITHRIL_FLUID_TANK.get();
	}
	public static class Entity extends FluidTankBlockEntity{
		public Entity(BlockEntityType<?> type,BlockPos pos,BlockState state){
			super(type,pos,state);
		}
		public static int getCapacityMultiplier(){
			try{
				return DifModServerConfig.MITHRIL_FLUID_TANK_CAPACITY.get()*1000;
			}catch(Exception e){
				return 32000;
			}
		}
		@Override
		protected SmartFluidTank createInventory(){
			return new SmartFluidTank(getCapacityMultiplier(),this::onFluidStackChanged);
		}
		@Override
		protected void onFluidStackChanged(FluidStack newFluidStack){
			super.onFluidStackChanged(newFluidStack);
			if(level!=null&&!level.isClientSide){
				DisplayLinkBlock.notifyGatherers(level,worldPosition);
			}
		}
		@Override
		public void notifyMultiUpdated(){
			super.notifyMultiUpdated();
			if(level!=null&&!level.isClientSide){
				DisplayLinkBlock.notifyGatherers(level,worldPosition);
			}
		}
		@Override
		public void applyFluidTankSize(int blocks){
			tankInventory.setCapacity(blocks*getCapacityMultiplier());
			int overflow=tankInventory.getFluidAmount()-tankInventory.getCapacity();
			if(overflow>0) tankInventory.drain(overflow,FluidAction.EXECUTE);
			forceFluidLevelUpdate=true;
		}
		@Override
		public int getTankSize(int tank){
			return getCapacityMultiplier();
		}
		@Override
		public float getFillState(){
			FluidTankBlockEntity controller=getControllerBE();
			if(controller!=null&&controller!=this) return controller.getFillState();
			int cap=tankInventory.getCapacity();
			return cap>0?(float)tankInventory.getFluidAmount()/cap:0f;
		}
		@Override
		protected void read(CompoundTag compound,HolderLookup.Provider registries,boolean clientPacket){
			int blocks=compound.getInt("Size")*compound.getInt("Size")*compound.getInt("Height");
			if(blocks>0) tankInventory.setCapacity(blocks*getCapacityMultiplier());
			super.read(compound,registries,clientPacket);
			if(isController()){
				tankInventory.setCapacity(getTotalTankSize()*getCapacityMultiplier());
			}
		}
		public IFluidHandler getFluidCapability(){
			if(fluidCapability!=null) return fluidCapability;
			FluidTankBlockEntity controller=getControllerBE();
			return controller!=null?controller.getTankInventory():tankInventory;
		}
	}
	public static class TankItem extends FluidTankItem{
		public TankItem(Block block,Properties properties){
			super(block,properties);
		}
		@Override
		public @NotNull InteractionResult place(BlockPlaceContext ctx){
			InteractionResult res=super.place(ctx);
			if(res.consumesAction()) tryMultiPlaceMithril(ctx);
			return res;
		}
		@Override
		protected boolean updateCustomBlockEntityTag(BlockPos blockPos,Level level,Player player,ItemStack itemStack,BlockState blockState){
			MinecraftServer server=level.getServer();
			if(server==null) return false;
			CustomData blockEntityData=itemStack.get(DataComponents.BLOCK_ENTITY_DATA);
			if(blockEntityData!=null){
				CompoundTag nbt=blockEntityData.copyTag();
				nbt.remove("Luminosity");
				nbt.remove("Size");
				nbt.remove("Height");
				nbt.remove("Controller");
				nbt.remove("LastKnownPos");
				if(nbt.contains("TankContent")){
					FluidStack fluid=FluidStack.parseOptional(server.registryAccess(),nbt.getCompound("TankContent"));
					if(!fluid.isEmpty()){
						fluid.setAmount(Math.min(Entity.getCapacityMultiplier(),fluid.getAmount()));
						nbt.put("TankContent",fluid.saveOptional(server.registryAccess()));
					}
				}
				BlockEntity.addEntityType(nbt,DifModBlockEntities.MITHRIL_FLUID_TANK.get());
				itemStack.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(nbt));
			}
			return super.updateCustomBlockEntityTag(blockPos,level,player,itemStack,blockState);
		}
		private void tryMultiPlaceMithril(BlockPlaceContext ctx){
			Player player=ctx.getPlayer();
			if(player==null||player.isShiftKeyDown()) return;
			Direction face=ctx.getClickedFace();
			if(!face.getAxis().isVertical()||SymmetryWandItem.presentInHotbar(player)) return;
			Level world=ctx.getLevel();
			BlockPos pos=ctx.getClickedPos();
			BlockPos placedOnPos=pos.relative(face.getOpposite());
			if(!(world.getBlockState(placedOnPos).getBlock() instanceof MithrilFluidTank)) return;
			FluidTankBlockEntity tankAt=ConnectivityHandler.partAt(DifModBlockEntities.MITHRIL_FLUID_TANK.get(),world,placedOnPos);
			if(tankAt==null) return;
			FluidTankBlockEntity controller=tankAt.getControllerBE();
			if(controller==null||controller.getWidth()==1) return;
			int width=controller.getWidth();
			BlockPos startPos=face==Direction.DOWN?controller.getBlockPos().below():controller.getBlockPos().above(controller.getHeight());
			if(startPos.getY()!=pos.getY()) return;
			int needed=0;
			for(int x=0;x<width;x++){
				for(int z=0;z<width;z++){
					BlockPos offsetPos=startPos.offset(x,0,z);
					BlockState state=world.getBlockState(offsetPos);
					if(state.getBlock() instanceof MithrilFluidTank) continue;
					if(!state.canBeReplaced()) return;
					needed++;
				}
			}
			if(!player.isCreative()&&ctx.getItemInHand().getCount()<needed) return;
			for(int x=0;x<width;x++){
				for(int z=0;z<width;z++){
					BlockPos offsetPos=startPos.offset(x,0,z);
					if(world.getBlockState(offsetPos).getBlock() instanceof MithrilFluidTank) continue;
					player.getPersistentData().putBoolean("SilenceTankSound",true);
					super.place(BlockPlaceContext.at(ctx,offsetPos,face));
					player.getPersistentData().remove("SilenceTankSound");
				}
			}
		}
	}
}