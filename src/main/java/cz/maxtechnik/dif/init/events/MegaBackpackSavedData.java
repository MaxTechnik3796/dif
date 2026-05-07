package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.core.HolderLookup;
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

public class MegaBackpackSavedData extends SavedData {

	private final Map<UUID, NonNullList<ItemStack>> playerBackpacks = new HashMap<>();

	public MegaBackpackSavedData() {
	}

	// -------------------------------------------------------------------------
	// V NeoForge 1.21.1 load() přijímá i HolderLookup.Provider (pro item registry)
	// -------------------------------------------------------------------------
	public static MegaBackpackSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
		MegaBackpackSavedData data = new MegaBackpackSavedData();
		CompoundTag backpacks = nbt.getCompound("Backpacks");
		for (String key : backpacks.getAllKeys()) {
			UUID uuid = UUID.fromString(key);
			CompoundTag playerTag = backpacks.getCompound(key);
			int totalSize = MegaBackpackMenu.SLOTS_PER_PAGE * 16;
			NonNullList<ItemStack> allItems = NonNullList.withSize(totalSize, ItemStack.EMPTY);
			for (int p = 0; p < 16; p++) {
				if (playerTag.contains("Page" + p)) {
					CompoundTag pageTag = playerTag.getCompound("Page" + p);
					NonNullList<ItemStack> pageItems = NonNullList.withSize(MegaBackpackMenu.SLOTS_PER_PAGE, ItemStack.EMPTY);
					ContainerHelper.loadAllItems(pageTag, pageItems, provider); // provider povinný v 1.21.1
					for (int i = 0; i < MegaBackpackMenu.SLOTS_PER_PAGE; i++) {
						allItems.set(p * MegaBackpackMenu.SLOTS_PER_PAGE + i, pageItems.get(i));
					}
				}
			}
			data.playerBackpacks.put(uuid, allItems);
		}
		return data;
	}

	// -------------------------------------------------------------------------
	// save() přijímá i HolderLookup.Provider v 1.21.1
	// -------------------------------------------------------------------------
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
		CompoundTag backpacks = new CompoundTag();
		for (Map.Entry<UUID, NonNullList<ItemStack>> entry : playerBackpacks.entrySet()) {
			CompoundTag playerTag = new CompoundTag();
			NonNullList<ItemStack> allItems = entry.getValue();
			for (int p = 0; p < 16; p++) {
				CompoundTag pageTag = new CompoundTag();
				int start = p * MegaBackpackMenu.SLOTS_PER_PAGE;
				NonNullList<ItemStack> pageItems = NonNullList.withSize(MegaBackpackMenu.SLOTS_PER_PAGE, ItemStack.EMPTY);
				for (int i = 0; i < MegaBackpackMenu.SLOTS_PER_PAGE; i++) {
					pageItems.set(i, allItems.get(start + i));
				}
				ContainerHelper.saveAllItems(pageTag, pageItems, provider); // provider povinný v 1.21.1
				playerTag.put("Page" + p, pageTag);
			}
			backpacks.put(entry.getKey().toString(), playerTag);
		}
		tag.put("Backpacks", backpacks);
		return tag;
	}

	public NonNullList<ItemStack> getOrCreateInventory(UUID uuid) {
		int totalSize = MegaBackpackMenu.SLOTS_PER_PAGE * 16;
		return playerBackpacks.computeIfAbsent(uuid, k -> NonNullList.withSize(totalSize, ItemStack.EMPTY));
	}

	// -------------------------------------------------------------------------
	// computeIfAbsent v 1.21.1 vyžaduje SavedData.Factory místo dvou lambda
	// Factory obsahuje: constructor, load(tag, provider), fixedName (nebo null)
	// -------------------------------------------------------------------------
	public static MegaBackpackSavedData get(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.getServer().overworld().getDataStorage()
					.computeIfAbsent(
							new SavedData.Factory<>(
									MegaBackpackSavedData::new,
									MegaBackpackSavedData::load,
									null  // DataFixTypes – null pokud nepoužíváš DataFixer
							),
							"dif_mega_backpacks"
					);
		}
		return null;
	}
}