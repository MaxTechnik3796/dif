package cz.maxtechnik.dif.block.entity.barrel;

import cz.maxtechnik.dif.block.barrel.BrassBarrel;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class BrassBarrelBlockEntity extends BaseBarrelBlockEntity{
	public static final int CONTAINER_SIZE=54;
	public BrassBarrelBlockEntity(BlockPos position,BlockState blockState){
		super(DifModBlockEntities.BRASS_BARREL.get(),position,blockState,CONTAINER_SIZE,BrassBarrel.OPEN);
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.brass_barrel");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inv){
		return ChestMenu.sixRows(id,inv,this);
	}
}