package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.entity.vehicle.RemoteControlMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class RemoteMinecartItem extends Item{
	public RemoteMinecartItem(Properties props){
		super(props);
	}
	@Override
	public @NotNull InteractionResult useOn(UseOnContext context){
		Level level=context.getLevel();
		BlockPos pos=context.getClickedPos();
		BlockState state=level.getBlockState(pos);
		if(!state.is(net.minecraft.tags.BlockTags.RAILS)){
			return InteractionResult.FAIL;
		}else{
			if(!level.isClientSide){
				RailShape shape = state.is(net.minecraft.tags.BlockTags.RAILS) ? state.getValue(((BaseRailBlock)state.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
				double d0=0.0D;
				if(shape.isAscending()) d0=0.5D;
				RemoteControlMinecart minecart=new RemoteControlMinecart(level,pos.getX()+0.5D,pos.getY()+0.0625D+d0,pos.getZ()+0.5D);
				if(context.getItemInHand().has(DataComponents.CUSTOM_NAME)){
					minecart.setCustomName(context.getItemInHand().getHoverName());
				}
				level.addFreshEntity(minecart);
			}
			context.getItemInHand().shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}
}