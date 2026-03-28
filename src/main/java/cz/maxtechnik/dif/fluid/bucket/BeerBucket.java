package cz.maxtechnik.dif.fluid.bucket;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;
public class BeerBucket extends BucketItem{
	public BeerBucket(){
		super(DifModFluids.BEER,new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.COMMON));
	}
}
