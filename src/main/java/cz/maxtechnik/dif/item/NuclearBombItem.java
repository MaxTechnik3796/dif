package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.entity.NuclearCountdownEntity;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class NuclearBombItem extends Item {

    public NuclearBombItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos clicked = context.getClickedPos();
        NuclearCountdownEntity bomb = new NuclearCountdownEntity(DifModEntities.NUCLEAR_COUNTDOWN.get(), level);
        bomb.setPos(clicked.getX() + 0.5, clicked.getY() + 1.0, clicked.getZ() + 0.5);
        bomb.setCountdown(200);
        level.addFreshEntity(bomb);

        Player player = context.getPlayer();
        if (player != null && !player.isCreative()) context.getItemInHand().shrink(1);

        return InteractionResult.CONSUME;
    }
}