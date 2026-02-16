package cz.maxtechnik.dif.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber
public class OrbitVoidTeleport{
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		Player entity=event.player;
		if(!(entity instanceof ServerPlayer serverPlayer)) return;
		// OPTIMALIZACE: Kontrola proběhne pouze jednou za 20 ticků (1 vteřina)
		// Používáme ID entity jako offset, aby všichni hráči netikovali ve stejný moment
		if((entity.level().getGameTime()+entity.getId())%20!=0) return;
		ServerLevel world=serverPlayer.serverLevel();
		ResourceLocation currentDim=world.dimension().location();
		// 1. KONTROLA DIMENZE (Pouze v Orbitu)
		if(currentDim.equals(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit"))){
			// 2. KONTROLA VÝŠKY (-90)
			if(serverPlayer.getY()<=-90){
				// 3. PŘÍPRAVA CÍLOVÉHO SVĚTA (Overworld)
				ServerLevel overworld=serverPlayer.server.getLevel(Level.OVERWORLD);
				if(overworld!=null){
					// 4. APLIKACE EFEKTŮ (Tření o atmosféru)
					// Blindness na 5 sekund (100 ticků)
					serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100,0,false,false));
					// Zapálení na 10 sekund (vizuální efekt návratu)
					serverPlayer.setSecondsOnFire(10);
					// Zrušíme aktuální poškození pádem, aby hráč nezemřel hned po portu
					serverPlayer.fallDistance=0;
					// 5. TELEPORTACE
					// Souřadnice zůstávají stejné, výška je 500
					double targetX=serverPlayer.getX();
					double targetZ=serverPlayer.getZ();
					serverPlayer.teleportTo(overworld,targetX,500.0,targetZ,serverPlayer.getYRot(),serverPlayer.getXRot());
					// Zpráva pro hráče
					serverPlayer.displayClientMessage(Component.literal("§6You are falling back to Overworld!"),true);
				}
			}
		}
	}
}