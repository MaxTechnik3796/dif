package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.DistillationTank;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.DistillationRecipe;
import cz.maxtechnik.dif.util.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
public class DistillationControllerBlockEntity extends BlockEntity{
	public static final int CAPACITY=8000;
	/**
	 * Max výška věže tanků nad controllerem (1 controller + max 15 tanků = 16 bloků celkem).
	 */
	public static final int MAX_TANKS=DistillationRecipe.MAX_OUTPUTS; // 15
	public final FluidTank tank=new FluidTank(CAPACITY){
		@Override
		protected void onContentsChanged(){
			setChanged();
			if(level!=null&&!level.isClientSide)
				level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	};
	// per-instance, NE static!
	private int progress=0;
	private int totalTime=0;
	// cache aktivního receptu (re-resolve když se změní vstup)
	@Nullable
	private DistillationRecipe cachedRecipe;
	private FluidStack lastInput=FluidStack.EMPTY;
	public DistillationControllerBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.DISTILLATION_CONTROLLER.get(),pos,state);
	}
	public IFluidHandler getFluidHandler(){
		return tank;
	}
	public static void serverTick(Level level,BlockPos pos,DistillationControllerBlockEntity be){
		HeatLevel heat=HeatLevel.below(level,pos);
		if(!heat.isActive()){
			be.resetProgress();
			return;
		}
		FluidStack input=be.tank.getFluid();
		if(input.isEmpty()){
			be.resetProgress();
			return;
		}
		// resolve receptu pouze když se změní vstupní fluid (cache)
		if(be.cachedRecipe==null||!FluidStack.isSameFluidSameComponents(be.lastInput,input)){
			be.cachedRecipe=findRecipe(level,input).orElse(null);
			be.lastInput=input.copy();
			be.progress=0;
		}
		DistillationRecipe recipe=be.cachedRecipe;
		if(recipe==null||!recipe.matches(input)){
			be.resetProgress();
			return;
		}
		List<FluidStack> outputs=recipe.outputs();
		// kontrola: nad controllerem musí stát N tanků (= recipe.outputs.size(), max 15)
		if(!towerValid(level,pos,outputs.size())){
			return; // chybí věž -> žádný progres
		}
		// kontrola: výstupy se musí vejít do tanků (simulace), jinak "stalling"
		if(!canFitOutputs(level,pos,outputs)){
			return;
		}
		// progres
		be.totalTime=heat.ticksPerOp;
		be.progress++;
		be.setChanged();
		if(be.progress>=be.totalTime){
			be.progress=0;
			be.tank.drain(recipe.input().amount(),IFluidHandler.FluidAction.EXECUTE);
			for(int i=0;i<outputs.size();i++){
				IFluidHandler outTank=level.getCapability(Capabilities.FluidHandler.BLOCK,pos.above(i+1),null);
				if(outTank!=null) outTank.fill(outputs.get(i).copy(),IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}
	private void resetProgress(){
		if(progress!=0){
			progress=0;
			setChanged();
		}
	}
	private static Optional<DistillationRecipe> findRecipe(Level level,FluidStack input){
		for(RecipeHolder<DistillationRecipe> holder: level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())){
			if(holder.value().matches(input)) return Optional.of(holder.value());
		}
		return Optional.empty();
	}
	/**
	 * Kontrola, že nad controllerem je n DistillationTank bloků v řadě. n <= MAX_TANKS.
	 */
	private static boolean towerValid(Level level,BlockPos pos,int n){
		if(n<=0||n>MAX_TANKS) return false;
		for(int i=1;i<=n;i++){
			if(!(level.getBlockState(pos.above(i)).getBlock() instanceof DistillationTank)) return false;
		}
		return true;
	}
	/**
	 * Simulace fillu - vejde se každý výstup do svého tanku?
	 */
	private static boolean canFitOutputs(Level level,BlockPos pos,List<FluidStack> outputs){
		for(int i=0;i<outputs.size();i++){
			IFluidHandler outTank=level.getCapability(Capabilities.FluidHandler.BLOCK,pos.above(i+1),null);
			if(outTank==null) return false;
			int filled=outTank.fill(outputs.get(i).copy(),IFluidHandler.FluidAction.SIMULATE);
			if(filled<outputs.get(i).getAmount()) return false;
		}
		return true;
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("tank",tank.writeToNBT(provider,new CompoundTag()));
		tag.putInt("progress",progress);
		tag.putInt("totalTime",totalTime);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("tank")) tank.readFromNBT(provider,tag.getCompound("tank"));
		progress=tag.getInt("progress");
		totalTime=tag.getInt("totalTime");
	}
	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return saveWithFullMetadata(provider);
	}
}
