package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
public class BanHammer extends Item{
	private static final ResourceLocation ATTACK_DAMAGE_MODIFIER=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"ban_hammer_damage");
	private static final ResourceLocation ATTACK_SPEED_MODIFIER=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"ban_hammer_speed");
	public BanHammer(){
		super(new Item.Properties().stacksTo(1).fireResistant()
				.attributes(ItemAttributeModifiers.builder()
						.add(Attributes.ATTACK_DAMAGE,new AttributeModifier(ATTACK_DAMAGE_MODIFIER,999999.0,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
						.add(Attributes.ATTACK_SPEED,new AttributeModifier(ATTACK_SPEED_MODIFIER,-2.4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
						.build()));
	}
	@Override
	public boolean onLeftClickEntity(@NotNull ItemStack stack,Player attacker,@NotNull Entity entity){
		if(!attacker.level().isClientSide&&entity instanceof LivingEntity target){
			DamageSource divineSource=attacker.level().damageSources().source(DamageTypes.FELL_OUT_OF_WORLD,attacker);
			if(target instanceof Player targetPlayer){
				ItemStack main=targetPlayer.getMainHandItem();
				ItemStack off=targetPlayer.getOffhandItem();
				if(main.getItem()==DifModItems.GOD_TOTEM.get()||off.getItem()==DifModItems.GOD_TOTEM.get()){
					if(main.getItem()==DifModItems.GOD_TOTEM.get()){
						targetPlayer.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
					}else{
						targetPlayer.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
					}
					targetPlayer.level().broadcastEntityEvent(targetPlayer,(byte)35);
					return true;
				}
			}
			if(target instanceof Player targetPlayer){
				MinecraftServer server=target.getServer();
				if(server!=null){
					String name=targetPlayer.getGameProfile().getName();
					server.getCommands().performPrefixedCommand(
							server.createCommandSourceStack(),
							"ban "+name+" Zabanován Božským Kladivem!"
					);
				}
			}
			target.setInvulnerable(false);
			if(target instanceof Player targetPlayer){
				targetPlayer.getAbilities().invulnerable=false;
				targetPlayer.onUpdateAbilities();
			}
			target.hurt(divineSource,Float.MAX_VALUE);
			if(target.isAlive()){
				target.setHealth(0.0f);
				target.die(divineSource);
				if(target.isAlive()){
					target.discard();
				}
			}
			return true;
		}
		return false;
	}
}