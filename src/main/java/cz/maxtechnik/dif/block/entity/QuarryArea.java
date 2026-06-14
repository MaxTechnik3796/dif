package cz.maxtechnik.dif.block.entity;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Sdílená datová třída reprezentující obdélníkovou oblast quarry na ose X-Z.
 * Tuto třídu používají jak QuarryBlockEntity, tak QuarryLandmarkBlockEntity,
 * takže výpočty rozměrů jsou vždy na jednom místě.
 */
public record QuarryArea(int minX, int maxX, int minZ, int maxZ) {

	/** Šířka oblasti (počet bloků na ose X). */
	public int sizeX() { return maxX - minX + 1; }

	/** Hloubka oblasti (počet bloků na ose Z). */
	public int sizeZ() { return maxZ - minZ + 1; }

	/**
	 * Vrátí hranice těžební oblasti — o 1 blok menší ze každé strany
	 * (kvůli vnějšímu rámu, který quarry tvoří).
	 */
	public QuarryArea miningBounds() {
		return new QuarryArea(minX + 1, maxX - 1, minZ + 1, maxZ - 1);
	}

	// ── NBT ─────────────────────────────────────────────────────────────

	/**
	 * Uloží oblast do NBT tagu pod klíče "AMnX", "AMxX", "AMnZ", "AMxZ".
	 */
	public void save(CompoundTag tag) {
		tag.putInt("AMnX", minX);
		tag.putInt("AMxX", maxX);
		tag.putInt("AMnZ", minZ);
		tag.putInt("AMxZ", maxZ);
	}

	/**
	 * Načte oblast z NBT tagu. Vrátí null, pokud klíče neexistují.
	 */
	@Nullable
	public static QuarryArea load(CompoundTag tag) {
		if (!tag.contains("AMnX")) return null;
		return new QuarryArea(
			tag.getInt("AMnX"), tag.getInt("AMxX"),
			tag.getInt("AMnZ"), tag.getInt("AMxZ")
		);
	}

	/**
	 * Zpětná kompatibilita — načte oblast z původního "halfX + center" formátu.
	 */
	@Nullable
	public static QuarryArea loadLegacyHalf(CompoundTag tag, String halfXKey, String halfZKey, String cxKey, String czKey) {
		if (!tag.contains(halfXKey) || !tag.contains(cxKey)) return null;
		int hx = tag.getInt(halfXKey), hz = tag.getInt(halfZKey);
		int cx = tag.getInt(cxKey),   cz = tag.getInt(czKey);
		return new QuarryArea(cx - hx, cx + hx, cz - hz, cz + hz);
	}
}
