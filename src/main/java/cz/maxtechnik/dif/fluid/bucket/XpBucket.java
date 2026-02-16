package cz.maxtechnik.dif.fluid.bucket;

import cz.maxtechnik.dif.fluid.DifModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
public class XpBucket extends BucketItem {
	public XpBucket() {
		super(DifModFluids.XP,new Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.COMMON));
	}
}
