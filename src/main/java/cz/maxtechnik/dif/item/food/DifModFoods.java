package cz.maxtechnik.dif.item.food;

import cz.maxtechnik.dif.init.other.DifModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
@SuppressWarnings("deprecation")
public class DifModFoods{
	public static final FoodProperties MATA=new FoodProperties.Builder().nutrition(1).saturationMod(0.2F).alwaysEat().build();
	public static final FoodProperties SUGAR_MUSHROOM=new FoodProperties.Builder().nutrition(3).saturationMod(0.5F).alwaysEat().build();
	public static final FoodProperties CHERRY=new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).alwaysEat().build();
	public static final FoodProperties FRIES=new FoodProperties.Builder().nutrition(3).saturationMod(0.45F).alwaysEat().build();
	public static final FoodProperties RIZEK=new FoodProperties.Builder().nutrition(5).saturationMod(0.4F).alwaysEat().meat().build();
	public static final FoodProperties HORSE_MEAT=new FoodProperties.Builder().nutrition(2).saturationMod(0.1F).alwaysEat().meat().build();
	public static final FoodProperties COOKED_HORSE_MEAT=new FoodProperties.Builder().nutrition(6).saturationMod(0.8F).alwaysEat().meat().build();
	public static final FoodProperties BURNED_TOAST=new FoodProperties.Builder().nutrition(3).saturationMod(0.8F).alwaysEat().build();
	public static final FoodProperties FERNET=new FoodProperties.Builder().nutrition(5).saturationMod(1.3F).effect(new MobEffectInstance(MobEffects.CONFUSION,200,0),1F).effect(new MobEffectInstance(MobEffects.REGENERATION,200,0),1F).alwaysEat().build();
	public static final FoodProperties BUCKET_OF_CHICKEN=new FoodProperties.Builder().nutrition(9).saturationMod(1.25F).meat().alwaysEat().build();
	public static final FoodProperties WINE=new FoodProperties.Builder().nutrition(5).saturationMod(1.3F).effect(new MobEffectInstance(MobEffects.REGENERATION,100,0),0.75F).effect(new MobEffectInstance(DifModMobEffects.DRANK.get(),600,0),0.25F).alwaysEat().build();
	public static final FoodProperties CIDER_BOTTLE=new FoodProperties.Builder().nutrition(3).saturationMod(1.1F).effect(new MobEffectInstance(MobEffects.ABSORPTION,1200,0),1F).effect(new MobEffectInstance(MobEffects.CONFUSION,100,0),1F).alwaysEat().build();
	public static final FoodProperties NETHER_WART_BOTTLE=new FoodProperties.Builder().nutrition(5).saturationMod(1.3F).effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,300,0),1F).effect(new MobEffectInstance(MobEffects.INVISIBILITY,600,0),1F).alwaysEat().build();
	public static final FoodProperties MATY_DRINK=new FoodProperties.Builder().nutrition(13).saturationMod(2f).effect(new MobEffectInstance(DifModMobEffects.REDSTONE_IQ.get(),1200,0),1F).effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0),1F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,100,2),1F).alwaysEat().build();
	public static final FoodProperties CHERRY_BOTTLE=new FoodProperties.Builder().nutrition(5).saturationMod(1.3F).effect(new MobEffectInstance(MobEffects.HEAL,1,0),1F).alwaysEat().build();
	public static final FoodProperties CRETE_CAN=new FoodProperties.Builder().nutrition(9).saturationMod(1.2F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,900,0),1F).effect(new MobEffectInstance(MobEffects.NIGHT_VISION,1500,0),1F).effect(new MobEffectInstance(MobEffects.HEALTH_BOOST,1200,0),1F).meat().alwaysEat().build();
	public static final FoodProperties CREATE_BOWL=new FoodProperties.Builder().nutrition(18).saturationMod(1.2F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,1800,1),1F).effect(new MobEffectInstance(MobEffects.NIGHT_VISION,3000,0),1F).effect(new MobEffectInstance(MobEffects.HEALTH_BOOST,2400,0),1F).meat().alwaysEat().build();
	public static final FoodProperties CREATE_SUPER=new FoodProperties.Builder().nutrition(20).saturationMod(2F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,1800,1),1F).effect(new MobEffectInstance(MobEffects.NIGHT_VISION,6000,0),1F).effect(new MobEffectInstance(MobEffects.HEALTH_BOOST,4800,1),1F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,6000,0),1F).meat().alwaysEat().build();
	public static final FoodProperties FLAT_DOUGH=new FoodProperties.Builder().nutrition(2).saturationMod(0.1F).effect(new MobEffectInstance(MobEffects.HUNGER,600,0),0.8F).build();
}