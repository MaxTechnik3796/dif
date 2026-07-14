package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.EnumSet;
import java.util.Optional;
@EventBusSubscriber
public class ModularSubscriber{
	@SubscribeEvent
	public static void onEntityAttacked(LivingDamageEvent.Pre event){
		if(!(event.getSource().getEntity() instanceof Player attacker)) return;
		LivingEntity target=event.getEntity();
		float dmg=event.getNewDamage();
		ItemStack itemStack=attacker.getMainHandItem();
		if(!(itemStack.getItem() instanceof ModularTool)) return;
		ModularToolProperties props=ModularTool.getProps(itemStack);
		if(props==null) return;
		ModularReforge reforge=ModularTool.getReforge(itemStack);
		int[] chances={0,0,8,5,3};
		int epicChance=chances[2];
		switch(reforge){
			case VAMPIRIC,DRAIN -> {
				if(attacker.getHealth()>=attacker.getMaxHealth()) return;
				if(DifMod.rouletteBoolean(epicChance)) attacker.heal(Math.clamp(dmg/2F,0.5F,target.getHealth()));
			}
			case PHANTOM -> {
				if(DifMod.rouletteBoolean(epicChance)){
					float newDmg=dmg+Math.max(0.5F,dmg/2F);
					event.setNewDamage(newDmg);
				}
			}
			case FROZEN -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,160,0));
			case CURSE -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));
		}
		// Material modifiers: parse materials once for this event
		if(ModularTool.getMaterialModifiers(itemStack).contains(ModularModifier.TOXIC)){
			if(attacker.level().getRandom().nextFloat()<0.3F) target.addEffect(new MobEffectInstance(MobEffects.POISON,60,0));
		}
	}
	@SubscribeEvent
	public static void onLivingExp(LivingExperienceDropEvent event){
		if(event.getAttackingPlayer()!=null){
			ItemStack tool=event.getAttackingPlayer().getMainHandItem();
			if(ModularTool.hasMaterialModifier(tool,ModularModifier.LUCKY_MAT)) event.setDroppedExperience((int)Math.ceil(event.getDroppedExperience()*1.25));
		}
	}
	@SubscribeEvent
	public static void onBlockDrops(BlockDropsEvent event){
		if(!(event.getBreaker() instanceof Player player)) return;
		ItemStack tool=player.getMainHandItem();
		if(!(tool.getItem() instanceof ModularTool)) return;
		ServerLevel level=event.getLevel();
		// Parse materials once for all checks in this event
		EnumSet<ModularModifier> mods=ModularTool.getMaterialModifiers(tool);
		if(mods.contains(ModularModifier.MOMENTUM)){
			if(level.getRandom().nextFloat()<0.1F) player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,100,0));
		}
		if(ModularTool.isModifier(tool,ModularModifier.VOLCANIC)){
			RecipeManager recipeManager=level.getRecipeManager();
			for(net.minecraft.world.entity.item.ItemEntity dropEntity: event.getDrops()){
				ItemStack stack=dropEntity.getItem();
				SingleRecipeInput input=new SingleRecipeInput(stack);
				Optional<RecipeHolder<SmeltingRecipe>> recipeHolder=recipeManager.getRecipeFor(RecipeType.SMELTING,input,level);
				if(recipeHolder.isPresent()){
					ItemStack result=recipeHolder.get().value().getResultItem(level.registryAccess());
					if(!result.isEmpty()){
						ItemStack newStack=result.copy();
						newStack.setCount(stack.getCount()*result.getCount());
						dropEntity.setItem(newStack);
					}
				}
			}
		}
	}
}