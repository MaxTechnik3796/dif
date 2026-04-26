package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
public class FormulaItem extends Item{
	public FormulaItem(Properties properties){
		super(properties);
	}
	@Override
	public @NotNull InteractionResult useOn(UseOnContext context){
		Level level=context.getLevel();
		if(!level.isClientSide){
			BlockPos pos=context.getClickedPos().relative(context.getClickedFace());
			Entity entity=DifModEntities.FORMULA.get().create(level);
			if(entity!=null){
				assert context.getPlayer()!=null;
				entity.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,context.getPlayer().getYRot(),0);
				level.addFreshEntity(entity);
				if(!context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
