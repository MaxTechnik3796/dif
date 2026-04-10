package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.block.SleepingBagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/**
 * Handler pro zachytávání událostí spojených s hráčem.
 * Tento kód běží na serveru i klientovi, ale nastavení spawnu řeší primárně server.
 */
@Mod.EventBusSubscriber
public class PlayerSpawnHandler{
	@SubscribeEvent
	public static void onPlayerSetSpawn(PlayerSetSpawnEvent event){
		// Získáme svět, ve kterém se hráč nachází
		Level level=event.getEntity().level();
		// Získáme pozici, kterou se hra pokouší nastavit jako nový spawn
		BlockPos pos=event.getNewSpawn();
		// Pokud je pozice null, znamená to, že se spawn resetuje (např. příkazem), to neřešíme
		if(pos!=null){
			BlockState state=level.getBlockState(pos);
			// KONTROLA: Pokud blok na dané pozici je náš Spacák
			if(state.getBlock() instanceof SleepingBagBlock){
				// Pokud je událost zrušitelná, zrušíme ji.
				// To zajistí, že hráč sice v posteli usne, ale jeho 'respawnPosition' zůstane nezměněna.
				if(event.isCancelable()){
					event.setCanceled(true);
				}
			}
		}
	}
}