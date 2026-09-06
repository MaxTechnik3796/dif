package cz.maxtechnik.dif.block.entity.barrel;

import cz.maxtechnik.dif.block.barrel.AndesiteBarrel;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class AndesiteBarrelBlockEntity extends BaseBarrelBlockEntity{
	public static final int CONTAINER_SIZE=36;
	public AndesiteBarrelBlockEntity(BlockPos position,BlockState blockState){
		super(DifModBlockEntities.ANDESITE_BARREL.get(),position,blockState,CONTAINER_SIZE,AndesiteBarrel.OPEN);
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.andesite_barrel");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inv){
		return new ChestMenu(MenuType.GENERIC_9x4,id,inv,this,4);
	}
}