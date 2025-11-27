package cz.maxtechnik.dif.item.food;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class Beer extends BlockItem {
    public Beer(Block block, Properties properties) {
        super(block, properties);
    }
    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack, @NotNull Level world, @NotNull LivingEntity entity) {
        ItemStack retval = new ItemStack(Items.GLASS_BOTTLE);
        super.finishUsingItem(itemstack, world, entity);
        if(!world.isClientSide()){
            if (DifMod.rouletteBoolean(4)) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600, 0));
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            }
        }
        if (itemstack.isEmpty()) {
            return retval;
        } else {
            if (entity instanceof Player player && !player.getAbilities().instabuild) {
                if (!player.getInventory().add(retval)) {
                    player.drop(retval, false);
                }
            }
            return itemstack;
        }
    }
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack) {
        return UseAnim.DRINK;
    }
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(itemstack);
        }
        assert this.getFoodProperties() != null;
        if (player.canEat(this.getFoodProperties().canAlwaysEat())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }
}