package cz.maxtechnik.dif.fluid.bucket;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
public class CrudeOilBucket extends BucketItem{
	public CrudeOilBucket(){
		super(DifModFluids.CRUDE_OIL,new Properties().craftRemainder(Items.BUCKET).stacksTo(1));
	}
}
