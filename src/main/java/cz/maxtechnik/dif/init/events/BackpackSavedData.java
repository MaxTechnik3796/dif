package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class BackpackSavedData extends SavedData {
	private final Map<UUID, NonNullList<ItemStack>> playerBackpacks = new HashMap<>();

	public BackpackSavedData() {}

	// Tato metoda musí být statická pro načítání ze souboru .dat
	public static BackpackSavedData load(CompoundTag nbt) {
		BackpackSavedData data = new BackpackSavedData();
		CompoundTag backpacks = nbt.getCompound("Backpacks");
		for (String key : backpacks.getAllKeys()) {
			UUID uuid = UUID.fromString(key);
			CompoundTag playerTag = backpacks.getCompound(key);
			int size = playerTag.getInt("Size");
			NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
			ContainerHelper.loadAllItems(playerTag, items);
			data.playerBackpacks.put(uuid, items);
		}
		return data;
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		CompoundTag backpacks = new CompoundTag();
		for (Map.Entry<UUID, NonNullList<ItemStack>> entry : playerBackpacks.entrySet()) {
			CompoundTag playerTag = new CompoundTag();
			playerTag.putInt("Size", entry.getValue().size());
			ContainerHelper.saveAllItems(playerTag, entry.getValue());
			backpacks.put(entry.getKey().toString(), playerTag);
		}
		tag.put("Backpacks", backpacks);
		return tag;
	}

	// Místo fixního čísla použij dynamický výpočet
	public NonNullList<ItemStack> getOrCreateInventory(UUID uuid) {
		// 13 * 17 * 16 stránek = 3536
		int totalSize = MegaBackpackMenu.SLOTS_PER_PAGE * 16;
		return playerBackpacks.computeIfAbsent(uuid, k -> NonNullList.withSize(totalSize, ItemStack.EMPTY));
	}

	public static BackpackSavedData get(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.getServer().overworld().getDataStorage()
					.computeIfAbsent(BackpackSavedData::load, BackpackSavedData::new, "dif_mega_backpacks");
		}
		return null;
	}
}