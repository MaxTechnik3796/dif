package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

public class FormulaItem extends Item {
    public FormulaItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if(!level.isClientSide) {
            BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
            Entity e = DifModEntities.FORMULA.get().create(level);
            if (e != null) {
                e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.getPlayer().getYRot(), 0);
                level.addFreshEntity(e);
                if (!ctx.getPlayer().getAbilities().instabuild) {
                    ctx.getItemInHand().shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
