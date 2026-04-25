package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class SpaceSurvivalHandler{
	private static final TagKey<Item> SPACE_SUIT_TAG=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"space_suit"));
	private static final Set<ResourceKey<Level>> DANGEROUS_DIMENSIONS=Set.of(
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit")),
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"))
	);
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase!=TickEvent.Phase.END||event.player.level().isClientSide()) return;
		if(event.player.tickCount%20!=0) return;
		ServerPlayer player=(ServerPlayer)event.player;
		if(player.isCreative()||player.isSpectator()||player.isInvulnerable()) return;
		if(!DANGEROUS_DIMENSIONS.contains(player.level().dimension())) return;
		boolean isProtected=
				player.getItemBySlot(EquipmentSlot.HEAD).is(SPACE_SUIT_TAG)&&
						player.getItemBySlot(EquipmentSlot.CHEST).is(SPACE_SUIT_TAG)&&
						player.getItemBySlot(EquipmentSlot.LEGS).is(SPACE_SUIT_TAG)&&
						player.getItemBySlot(EquipmentSlot.FEET).is(SPACE_SUIT_TAG);
		if(!isProtected) applySpaceDamage(player);
	}
	private static void applySpaceDamage(ServerPlayer player){
		player.kill();
	}
}