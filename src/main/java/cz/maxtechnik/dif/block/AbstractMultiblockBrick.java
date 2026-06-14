package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.AbstractMultiblockBrickBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * Základní blok pro "cihličky" multibloku (např. CokeOven, BlastSmeltery).
 * <p>
 * Při položení bloku vždy maže {@code controllerPos} z NBT — zabraňuje tomu,
 * aby cihlička vzatá middle-clickem nebo kontrapcí si pamatovala starý controller
 * a fungovala jako "bezdrátová pec".
 * Při odebrání upozorní controller, aby převalidoval strukturu.
 */
public abstract class AbstractMultiblockBrick extends Block implements EntityBlock{
	protected AbstractMultiblockBrick(BlockBehaviour.Properties properties){
		super(properties);
	}
	/**
	 * Vrátí block entity na dané pozici, pokud je to cihlička tohoto multibloku.
	 */
	protected abstract @Nullable AbstractMultiblockBrickBlockEntity getBlockEntityFromPos(Level level,BlockPos pos);
	/**
	 * Při položení bloku vždy vymažeme uloženou referenci na controller.
	 * <p>
	 * Cihlička vzatá middle-clickem nebo kontrapcí nese v NBT item stacku starý
	 * {@code controllerPos}. Ten se při {@code newBlockEntity()} zkopíruje do nové
	 * block entity — a cihlička by tak okamžitě "patřila" vzdálenému controlleru.
	 * Smazáním zde zajistíme, že controller si ji znovu nárokuje až při příštím
	 * tick validaci, kdy skutečně ověří, že cihlička je na správném místě.
	 */
	@Override
	public void onPlace(
			@NotNull BlockState blockState,
			@NotNull Level level,
			@NotNull BlockPos pos,
			@NotNull BlockState oldState,
			boolean isMoving
	){
		super.onPlace(blockState,level,pos,oldState,isMoving);
		if(!level.isClientSide){
			AbstractMultiblockBrickBlockEntity brick=getBlockEntityFromPos(level,pos);
			if(brick!=null) brick.setControllerPos(null);
		}
	}
	@Override
	public void onRemove(
			BlockState blockState,
			@NotNull Level level,
			@NotNull BlockPos pos,
			BlockState newState,
			boolean isMoving
	){
		if(!blockState.is(newState.getBlock())){
			AbstractMultiblockBrickBlockEntity brick=getBlockEntityFromPos(level,pos);
			if(brick!=null) brick.notifyControllerRemoved();
		}
		super.onRemove(blockState,level,pos,newState,isMoving);
	}
}