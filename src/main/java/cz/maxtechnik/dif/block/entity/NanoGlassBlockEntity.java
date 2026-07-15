package cz.maxtechnik.dif.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class NanoGlassBlockEntity extends BlockEntity{
	private long cooldownExpireTick=0L;
	public NanoGlassBlockEntity(BlockEntityType<?> type,BlockPos pos,BlockState blockState){
		super(type,pos,blockState);
	}
	public boolean isOnCooldown(long currentGameTime){
		return currentGameTime<cooldownExpireTick;
	}
	public void setCooldown(long currentGameTime,long cooldownTicks){
		this.cooldownExpireTick=currentGameTime+cooldownTicks;
		setChanged();
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.loadAdditional(tag,registries);
		cooldownExpireTick=tag.getLong("CooldownExpireTick");
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.saveAdditional(tag,registries);
		tag.putLong("CooldownExpireTick",cooldownExpireTick);
	}
}