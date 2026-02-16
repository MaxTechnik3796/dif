package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class EndDisabler{
	@SubscribeEvent
	public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event){
		if(event.getEntity().level().isClientSide())return;
		if(event.getTo().equals(Level.END)&&DifModCommonConfig.disableEnd){
			if(event.getEntity()instanceof ServerPlayer player){
				if(player.gameMode.getGameModeForPlayer().equals(GameType.CREATIVE)||player.gameMode.getGameModeForPlayer().equals(GameType.SPECTATOR))return;
				ResourceKey<Level>home=player.getRespawnDimension();
				assert player.getRespawnPosition()!=null;
				ServerLevel nextLevel=player.server.getLevel(home);
				if(nextLevel!=null){
					player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));
					player.teleportTo(nextLevel,player.getRespawnPosition().getX()+0.5,player.getRespawnPosition().getY()+1.5,player.getRespawnPosition().getZ()+0.5,player.getYRot(),player.getXRot());
					player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
					for(MobEffectInstance effectInstance:player.getActiveEffects())
						player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(),effectInstance));
					player.connection.send(new ClientboundLevelEventPacket(1032,BlockPos.ZERO,0,false));
				}
				DifMod.sendMessageToPlayer(player,Component.literal("§cEnd is not enabled!"));
			}
		}
	}
}
