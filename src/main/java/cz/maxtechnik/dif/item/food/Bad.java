package cz.maxtechnik.dif.item.food;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
@SuppressWarnings("deprecation")
public class Bad extends Item{
	public Bad(){
		super(new Properties().rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.3f)
						.effect(new MobEffectInstance(MobEffects.CONFUSION,1200,0),1F)
						.effect(new MobEffectInstance(MobEffects.BLINDNESS,1200,0),1F)
						.effect(new MobEffectInstance(MobEffects.HUNGER,1200,4),1F)
						.effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,1200,3),1F)
						.effect(new MobEffectInstance(MobEffects.POISON,1200,1),1F)
						.effect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,1200,0),1F)
				.alwaysEat().build()));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			world.playSound(null,BlockPos.containing(entity.getX(),entity.getY(),entity.getZ()),Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft","entity.ghast.hurt"))),SoundSource.PLAYERS,1,1);
		}
		return retval;
	}
}
