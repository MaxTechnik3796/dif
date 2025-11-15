package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.gui.menu.PortableCraftingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Test extends Item {
	public Test(){
		super(new Properties());
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand){
		ItemStack itemstack = player.getItemInHand(hand);

		// Ujistíme se, že se kód provádí pouze na serveru
		if (!world.isClientSide()) {
			// Zde otevřeme standardní Crafting Menu
			player.openMenu(new SimpleMenuProvider((id, inventory, p) -> {
				// Vytvoříme instanci CraftingMenu.
				// Poslední argument ContainerLevelAccess.NULL je klíčový:
				// říká, že k otevření není potřeba žádný blok (pracovní stůl),
				// takže se otevře "přenosná" verze.
				return new PortableCraftingMenu(id, inventory,world);
			},
					// Titulek GUI (může být cokoliv, např. název předmětu)
					Component.translatable("container.crafting")));
		}

		// Vracíme úspěch a značíme, že itemstack byl spotřebován (což není pravda,
		// ale v tomto případě je success nejlepší pro spuštění animace)
		return InteractionResultHolder.success(itemstack);
	}
}