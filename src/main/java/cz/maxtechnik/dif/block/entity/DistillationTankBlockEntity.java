package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.DistillationRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
@SuppressWarnings("unsafe")
public class DistillationTankBlockEntity extends FluidTankBlockEntity{
	public static final int MAX_FOOTPRINT=3;
	public static final int MAX_OUTPUTS=15;
	public static final int BASE_TICKS=100;
	private static final int CACHE_REFRESH_RATE=20;
	private int towerOutputCount=0;
	private int cachedHeatPoints=0;
	private float cachedSpeed=0F;
	private int cacheTick=0;
	private int progress=0;
	@Nullable
	private DistillationRecipe cachedRecipe=null;
	private FluidStack lastInput=FluidStack.EMPTY;
	public DistillationTankBlockEntity(BlockEntityType<?> type,BlockPos pos,BlockState state){
		super(type,pos,state);
		this.window = false;
	}
	@Override
	public int getMaxWidth(){
		return MAX_FOOTPRINT;
	}
	@Override
	public int getMaxLength(Direction.Axis axis,int width){
		return 1;
	}
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> list){}
	public IFluidHandler getFluidCapability(){
		return fluidCapability;
	}
	public IFluidHandler fluidTank(){
		return getFluidCapability();
	}
	public boolean isTowerMaster(){
		if(!isController()||level==null) return false;
		var below=level.getBlockEntity(worldPosition.below());
		if(!(below instanceof DistillationTankBlockEntity b)) return true;
		if(!b.isController()) return true;
		return b.getWidth()!=this.getWidth();
	}
	@Nullable
	public DistillationTankBlockEntity getTowerMaster(){
		if(level==null) return null;
		DistillationTankBlockEntity layerCtrl;
		if(isController()) layerCtrl=this;
		else{
			BlockPos ctrlPos=getController();
			if(ctrlPos==null) return null;
			if(!(level.getBlockEntity(ctrlPos) instanceof DistillationTankBlockEntity c)) return null;
			layerCtrl=c;
		}
		if(layerCtrl.isTowerMaster()) return layerCtrl;
		BlockPos check=layerCtrl.worldPosition.below();
		for(int i=0;i<MAX_OUTPUTS+2;i++){
			if(!(level.getBlockEntity(check) instanceof DistillationTankBlockEntity be)) return null;
			if(be.isTowerMaster()) return be;
			check=check.below();
		}
		return null;
	}
	private void refreshCache(){
		int w=getWidth();
		towerOutputCount=0;
		for(int i=1;i<=MAX_OUTPUTS;i++){
			assert level!=null;
			if(!(level.getBlockEntity(worldPosition.above(i)) instanceof DistillationTankBlockEntity a)) break;
			if(!a.isController()||a.getWidth()!=w) break;
			towerOutputCount++;
		}
		int points=0;
		HeatLevel best=HeatLevel.NONE;
		for(int x=0;x<w;x++){
			for(int z=0;z<w;z++){
				BlockState burner=level.getBlockState(worldPosition.offset(x,-1,z));
				HeatLevel h=BlazeBurnerBlock.getHeatLevelOf(burner);
				if(h==HeatLevel.KINDLED) points+=1;
				else if(h==HeatLevel.SEETHING) points+=2;
				if(h.ordinal()>best.ordinal()) best=h;
			}
		}
		cachedHeatPoints=points;
		cachedSpeed=Math.min(10,points)*0.5F;
		sendData();
	}
	public static void serverTick(Level level,DistillationTankBlockEntity be){
		if(!be.isTowerMaster()) return;
		if(be.cacheTick--<=0){
			be.refreshCache();
			be.cacheTick=CACHE_REFRESH_RATE;
		}
		if(be.cachedHeatPoints==0){
			be.resetProgress();
			return;
		}
		FluidStack input=be.tankInventory.getFluid();
		if(input.isEmpty()){
			be.resetProgress();
			return;
		}
		if(be.cachedRecipe==null||!FluidStack.isSameFluidSameComponents(be.lastInput,input)){
			be.cachedRecipe=findRecipe(level,input).orElse(null);
			be.lastInput=input.copy();
			be.progress=0;
		}
		if(be.cachedRecipe==null){
			be.resetProgress();
			return;
		}
		List<FluidStack> outputs=be.cachedRecipe.outputs();
		if(outputs.size()>be.towerOutputCount) return;
		if(!canFitOutputs(level,be.worldPosition,outputs)) return;
		be.progress+=(int)(be.cachedSpeed*10f);
		be.setChanged();
		if(be.progress>=BASE_TICKS*10){
			be.progress=0;
			be.tankInventory.drain(be.cachedRecipe.input().amount(),IFluidHandler.FluidAction.EXECUTE);
			for(int i=0;i<outputs.size();i++){
				IFluidHandler out=level.getCapability(Capabilities.FluidHandler.BLOCK,be.worldPosition.above(i+1),null);
				if(out!=null) out.fill(outputs.get(i).copy(),IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}
	private void resetProgress(){
		if(progress!=0){
			progress=0;
			setChanged();
		}
	}
	private static boolean canFitOutputs(Level level,BlockPos masterPos,List<FluidStack> outputs){
		for(int i=0;i<outputs.size();i++){
			IFluidHandler h=level.getCapability(Capabilities.FluidHandler.BLOCK,masterPos.above(i+1),null);
			if(h==null) return false;
			if(h.fill(outputs.get(i).copy(),IFluidHandler.FluidAction.SIMULATE)<outputs.get(i).getAmount()) return false;
		}
		return true;
	}
	private static Optional<DistillationRecipe> findRecipe(Level level,FluidStack input){
		for(RecipeHolder<DistillationRecipe> holder: level.getRecipeManager().getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())) if(holder.value().matches(input)) return Optional.of(holder.value());
		return Optional.empty();
	}
	@Override
	public void notifyMultiUpdated(){
		super.notifyMultiUpdated();
		cacheTick=0;
		progress=0;
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		boolean added=super.addToGoggleTooltip(tooltip,isPlayerSneaking);
		DistillationTankBlockEntity master=getTowerMaster();
		if(master==null) return added;
		tooltip.add(Component.literal(" "));
		if(isTowerMaster()) tooltip.add(Component.literal(" ◆ TOWER MASTER").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
		else tooltip.add(Component.literal(" ◆ TOWER").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
		tooltip.add(Component.literal(" Heat: ").withStyle(ChatFormatting.GRAY).append(Component.literal(master.cachedHeatPoints+" / 10").withStyle(master.cachedHeatPoints>=10?ChatFormatting.GREEN:ChatFormatting.WHITE)));
		if(master.cachedSpeed>0) tooltip.add(Component.literal(" Speed: ").withStyle(ChatFormatting.GRAY).append(Component.literal(master.cachedSpeed+"×").withStyle(master.cachedSpeed>=4.0f?ChatFormatting.GREEN:ChatFormatting.AQUA)));
		else tooltip.add(Component.literal(" No heat source!").withStyle(ChatFormatting.RED));
		return true;
	}
	@Override
	public void write(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.write(tag,registries,clientPacket);
		tag.putInt("dif_progress",progress);
		tag.putInt("dif_heatPoints",cachedHeatPoints);
		tag.putFloat("dif_speed",cachedSpeed);
	}
	@Override
	public void read(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.read(tag,registries,clientPacket);
		this.window = false;
		progress=tag.getInt("dif_progress");
		cachedHeatPoints=tag.getInt("dif_heatPoints");
		cachedSpeed=tag.getFloat("dif_speed");
		cacheTick=0;
		if (clientPacket && level != null) {
			requestModelDataUpdate();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}
	@Override
	public void setWindows(boolean window) {
		super.setWindows(false);
	}
}