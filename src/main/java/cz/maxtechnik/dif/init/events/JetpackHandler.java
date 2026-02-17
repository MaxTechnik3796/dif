package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.armor.JetpackItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
@Mod.EventBusSubscriber(modid=DifMod.MODID)
public class JetpackHandler{
	private static Field jumpingField;
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		Player player=event.player;
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(!(chest.getItem() instanceof JetpackItem)) return;
		// --- DETEKCE MEZERNÍKU ---
		boolean isJumping;
		try{
			if(jumpingField==null){
				try{
					jumpingField=LivingEntity.class.getDeclaredField("jumping");
				}catch(NoSuchFieldException e){
					jumpingField=LivingEntity.class.getDeclaredField("f_20899_");
				}
				jumpingField.setAccessible(true);
			}
			isJumping=jumpingField.getBoolean(player);
		}catch(Exception e){
			return;
		}
		int tank=JetpackItem.getTankFuel(chest);
		int main=JetpackItem.getMainFuel(chest);
		boolean isCreative=player.getAbilities().instabuild;
		// --- LOGIKA LETU (Satisfactory styl) ---
		if(isJumping&&!player.onGround()&&!player.getAbilities().flying){
			if(tank>0||isCreative){
				Vec3 v=player.getDeltaMovement();
				player.setDeltaMovement(v.x,0.4,v.z);
				player.fallDistance=0;
				if(!isCreative){
					JetpackItem.setTankFuel(chest,tank-1);
				}
				showOverlay(player,isCreative?200:tank-1,false);
			}
		}
		// --- LOGIKA DOPLŇOVÁNÍ (Tady byla chyba) ---
		else if(player.onGround()){
			if(tank<JetpackItem.MAX_TANK&&(main>0||isCreative)){
				// Určíme, kolik můžeme doplnit (max 2 za tick)
				int toAdd=Math.min(2,JetpackItem.MAX_TANK-tank);
				// Pokud nejsme v creative, nesmíme vzít víc, než kolik máme v hlavní nádrži
				if(!isCreative){
					toAdd=Math.min(toAdd,main);
				}
				if(toAdd>0){
					// Update proběhne primárně na serveru
					if(!player.level().isClientSide()){
						if(!isCreative){
							JetpackItem.setMainFuel(chest,main-toAdd);
						}
						JetpackItem.setTankFuel(chest,tank+toAdd);
						// ZÁSADNÍ: Forge potřebuje vědět, že se item změnil, aby poslal data klientovi
						player.getInventory().setChanged();
					}else{
						// Klient si to zrychlí jen pro plynulost UI
						JetpackItem.setTankFuel(chest,tank+toAdd);
					}
					showOverlay(player,tank+toAdd,true);
				}
			}
		}
	}
	private static void showOverlay(Player player,int tank,boolean recharging){
		if(tank<JetpackItem.MAX_TANK||recharging){
			String icon=recharging?"⚡ ":"🚀 ";
			ChatFormatting color=recharging?ChatFormatting.YELLOW:ChatFormatting.AQUA;
			int percent=(int)((tank/(float)JetpackItem.MAX_TANK)*100);
			int filledBlocks=tank/20;
			String bar="■".repeat(Math.max(0,filledBlocks))+"□".repeat(Math.max(0,10-filledBlocks));
			player.displayClientMessage(
					Component.literal(icon+"THRUST: ["+bar+"] "+percent+"%").withStyle(color),
					true
			);
		}
	}
}