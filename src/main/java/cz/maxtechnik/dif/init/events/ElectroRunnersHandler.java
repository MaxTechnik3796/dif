package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.item.armor.ElectroRunnersItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "tvojmodid")
public class ElectroRunnersHandler {

    // Logika skoku - zvýšeno na 2 bloky
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            
            // Kontrola bot a energie (skok stojí 5 EU)
            if (boots.getItem() instanceof ElectroRunnersItem && ElectroRunnersItem.getEnergyStored(boots) >= 5) {
                Vec3 motion = player.getDeltaMovement();
                // Přidání síly k vertikálnímu pohybu pro dosažení 2 bloků
                player.setDeltaMovement(motion.x, motion.y + 0.255, motion.z);
                
                ElectroRunnersItem.extractEnergy(boots, 5);
            }
        }
    }

    // Vybíjení při zásahu (hit)
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.getItem() instanceof ElectroRunnersItem) {
                // Každý zásah (kromě pádu, který teď ignorujeme) odebere 5 EU
                ElectroRunnersItem.extractEnergy(boots, 5);
            }
        }
    }
}