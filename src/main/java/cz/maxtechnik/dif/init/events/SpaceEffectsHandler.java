package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class SpaceEffectsHandler{
	private static final Set<ResourceKey<Level>> LOW_GRAVITY_DIMENSIONS=Set.of(
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit")),
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"))
	);
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase!=TickEvent.Phase.END||event.player.level().isClientSide()) return;
		if(event.player.tickCount%20!=0) return;
		ServerPlayer player=(ServerPlayer)event.player;
		if(LOW_GRAVITY_DIMENSIONS.contains(player.level().dimension())) applyEffects(player);
	}
	private static void applyEffects(ServerPlayer player){
		player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0,true,false));
		player.addEffect(new MobEffectInstance(MobEffects.JUMP,40,0,true,false));
	}
}