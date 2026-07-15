package cz.maxtechnik.dif.init.other;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
@SuppressWarnings("deprecation")
public class DifModFoods{
	public static final FoodProperties FRIES=new FoodProperties.Builder().nutrition(3).saturationModifier(0.45F).alwaysEdible().build();
	public static final FoodProperties BUCKET_OF_CHICKEN=new FoodProperties.Builder().nutrition(9).saturationModifier(1.25F).alwaysEdible().build();
	public static final FoodProperties RIZEK=new FoodProperties.Builder().nutrition(5).saturationModifier(0.4F).alwaysEdible().build();
	public static final FoodProperties HORSE_MEAT=new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).alwaysEdible().build();
	public static final FoodProperties COOKED_HORSE_MEAT=new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build();
	public static final FoodProperties BURNED_TOAST=new FoodProperties.Builder().nutrition(3).saturationModifier(0.8F).effect(new MobEffectInstance(MobEffects.WITHER,100),1F).alwaysEdible().build();
	public static final FoodProperties BEER=new FoodProperties.Builder().nutrition(4).saturationModifier(1F).effect(new MobEffectInstance(DifModMobEffects.DRANK,600,0),0.25F).effect(new MobEffectInstance(MobEffects.REGENERATION,100,0),0.75F).alwaysEdible().build();
	public static final FoodProperties BERRY_BOTTLE=new FoodProperties.Builder().nutrition(5).saturationModifier(1.3F).effect(new MobEffectInstance(MobEffects.HEAL,1,0),1F).alwaysEdible().build();
	public static final FoodProperties CIDER_BOTTLE=new FoodProperties.Builder().nutrition(3).saturationModifier(1.1F).effect(new MobEffectInstance(MobEffects.ABSORPTION,1200,0),1F).effect(new MobEffectInstance(MobEffects.CONFUSION,100,0),1F).alwaysEdible().build();
	public static final FoodProperties CRETE_CAN=new FoodProperties.Builder().nutrition(9).saturationModifier(1.2F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,900,0),1F).effect(new MobEffectInstance(MobEffects.NIGHT_VISION,1500,0),1F).effect(new MobEffectInstance(MobEffects.HEALTH_BOOST,1200,0),1F).alwaysEdible().build();
	public static final FoodProperties CREATE_BOWL=new FoodProperties.Builder().nutrition(18).saturationModifier(1.2F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,1800,1),1F).effect(new MobEffectInstance(MobEffects.NIGHT_VISION,3000,0),1F).effect(new MobEffectInstance(MobEffects.HEALTH_BOOST,2400,0),1F).alwaysEdible().build();
	public static final FoodProperties CREATE_SUPER=new FoodProperties.Builder().nutrition(20).saturationModifier(2F).effect(new MobEffectInstance(MobEffects.DIG_SPEED,1800,1),1F).effect(new MobEffectInstance(MobEffects.NIGHT_VISION,6000,0),1F).effect(new MobEffectInstance(MobEffects.HEALTH_BOOST,4800,1),1F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,6000,0),1F).alwaysEdible().build();
	public static final FoodProperties BOTTLE_OF_MOLOTOVUV_KOKTEJL=new FoodProperties.Builder().nutrition(4).saturationModifier(3F).alwaysEdible().build();
	public static final FoodProperties BOTTLE_OF_URANOVEJ_KOKTEJL=new FoodProperties.Builder().nutrition(4).saturationModifier(3F).effect(new MobEffectInstance(DifModMobEffects.WTF,1200,0),1F).alwaysEdible().build();
	public static final FoodProperties FLAT_DOUGH=new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).effect(new MobEffectInstance(MobEffects.HUNGER,600,0),0.8F).build();
}