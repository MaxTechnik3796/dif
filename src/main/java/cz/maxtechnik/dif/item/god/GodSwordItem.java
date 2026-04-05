package cz.maxtechnik.dif.item.god;

import cz.maxtechnik.dif.init.events.DivineDamageUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class GodSwordItem extends SwordItem {
    public GodSwordItem(Properties properties) {
        super(Tiers.NETHERITE, 3, -2.4f, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity target) {
            DivineDamageUtils.applyDivineDamage(target, player, false);
            return true;
        }
        return false;
    }
}