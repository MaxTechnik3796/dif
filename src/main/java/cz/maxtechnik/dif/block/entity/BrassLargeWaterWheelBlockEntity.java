package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
public class BrassLargeWaterWheelBlockEntity extends LargeWaterWheelBlockEntity{
	public BrassLargeWaterWheelBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.BRASS_LARGE_WATER_WHEEL.get(),pos,state);
	}
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours){
		super.addBehaviours(behaviours);
		initializeMaterial();
	}
	@Override
	public void onLoad(){
		super.onLoad();
		initializeMaterial();
	}
	private void initializeMaterial(){
		if(this.material==null||this.material.isAir()) this.material=Blocks.DARK_OAK_PLANKS.defaultBlockState();
	}
	@Override
	public void write(CompoundTag compound, HolderLookup.Provider provider, boolean clientPacket){
		super.write(compound, provider, clientPacket);
		if(material!=null) compound.put("Material",NbtUtils.writeBlockState(material));
	}
	@Override
	public void read(CompoundTag compound, HolderLookup.Provider provider, boolean clientPacket){
		super.read(compound, provider, clientPacket);
		if(compound.contains("Material"))
			this.material=NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),compound.getCompound("Material"));
		// Zajistíme valid material po načtení
		initializeMaterial();
	}
	@Override
	public float calculateStressApplied(){
		return 0F;
	}
	@Override
	public float calculateAddedStressCapacity(){
		return 512F;
	}
}