package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.GAME)
public class SpaceSurvivalHandler{
	private static final TagKey<Item> SPACE_SUIT_TAG=TagKey.create(Registries.ITEM,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"space_suit"));
	private static final Set<ResourceKey<Level>> DANGEROUS_DIMENSIONS=Set.of(
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit")),
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"))
	);
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event){
		// V 1.21.1 používáme PlayerTickEvent.Post (odpovídá starému TickEvent.Phase.END)
		if(event.getEntity().level().isClientSide()) return;
		// Kontrola jednou za sekundu (20 ticků)
		if(event.getEntity().tickCount%20!=0) return;
		if(event.getEntity() instanceof ServerPlayer player){
			// Ignorujeme nesmrtelné hráče
			if(player.isCreative()||player.isSpectator()||player.isInvulnerable()) return;
			// Kontrola, zda je hráč v nebezpečné dimenzi
			if(!DANGEROUS_DIMENSIONS.contains(player.level().dimension())) return;
			// Kontrola kompletního setu brnění pomocí Tagu
			boolean isProtected=
					player.getItemBySlot(EquipmentSlot.HEAD).is(SPACE_SUIT_TAG)&&
							player.getItemBySlot(EquipmentSlot.CHEST).is(SPACE_SUIT_TAG)&&
							player.getItemBySlot(EquipmentSlot.LEGS).is(SPACE_SUIT_TAG)&&
							player.getItemBySlot(EquipmentSlot.FEET).is(SPACE_SUIT_TAG);
			if(!isProtected){
				applySpaceDamage(player);
			}
		}
	}
	private static void applySpaceDamage(ServerPlayer player){
		// Okamžitá smrt (lze nahradit např. player.hurt(...) pro postupné ubírání životů)
		player.kill();
	}
}