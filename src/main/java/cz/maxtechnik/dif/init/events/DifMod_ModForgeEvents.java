package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.advancements.critereon.*;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class DifMod_ModForgeEvents{
	public static ItemStack emerald(int count){
		return new ItemStack(Items.EMERALD,count);
	}
	@SubscribeEvent
	public static void registerWanderingTrades(WandererTradesEvent event){
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.CREMEKA.get(),1),2,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MATY_CREATE.get(),1),2,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MAYONNAISE.get(),1),2,0,0F));
	}
	@SubscribeEvent
	public static void addComposterItems(FMLCommonSetupEvent event){
		ComposterBlock.COMPOSTABLES.put(Blocks.BAMBOO.asItem(),0.4F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA.get(),0.9F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA_PLANT.get(),0.88F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATY_BLOCK.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_PLANT.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_SEEDS.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CHERRY.get(),0.65F);
	}
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event){
		/*if(event.getType().equals(VillagerProfession.CARTOGRAPHER)){
			//event.getTrades().get(3).add(new TrialsMapTrade(12,12,5));
		}*/
	}
	@SubscribeEvent
	public static void cobbleGeneratorEvent(BlockEvent.FluidPlaceBlockEvent event){
		if(event.getPos().getY()<0){
			if(event.getState().getBlock().equals(Blocks.COBBLESTONE)){
				event.setNewState(Blocks.COBBLED_DEEPSLATE.defaultBlockState());
			}else if(event.getState().getBlock().equals(Blocks.STONE)){
				event.setNewState(Blocks.DEEPSLATE.defaultBlockState());
			}
		}
	}
	@SubscribeEvent
	public static void furnaceFuelBurnTimeEvent(FurnaceFuelBurnTimeEvent event) {
		ItemStack itemstack=event.getItemStack();
		if(itemstack.getItem().equals(Items.PAPER))event.setBurnTime(5);
	}
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
