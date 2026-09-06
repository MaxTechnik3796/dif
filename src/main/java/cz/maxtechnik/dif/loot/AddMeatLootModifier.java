package cz.maxtechnik.dif.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries; // POTŘEBNÝ IMPORT
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments; // POTŘEBNÝ IMPORT
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
public class AddMeatLootModifier extends LootModifier{
	public static final MapCodec<AddMeatLootModifier> CODEC=RecordCodecBuilder.mapCodec(inst->codecStart(inst).and(
			inst.group(
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("rawItem").forGetter(m->m.rawItem),
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("cookedItem").forGetter(m->m.cookedItem)
			)
	).apply(inst,AddMeatLootModifier::new));
	private final Item rawItem;
	private final Item cookedItem;
	public AddMeatLootModifier(LootItemCondition[] conditionsIn,Item rawItem,Item cookedItem){
		super(conditionsIn);
		this.rawItem=rawItem;
		this.cookedItem=cookedItem;
	}
	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot,LootContext context){
		Entity entity=context.getParamOrNull(LootContextParams.THIS_ENTITY);
		if(entity instanceof AgeableMob ageable&&ageable.isBaby()){
			return generatedLoot;
		}
		Item itemToDrop=this.rawItem;
		if(entity!=null&&entity.isOnFire()){
			itemToDrop=this.cookedItem;
		}
		int count=context.getRandom().nextInt(3)+1; // 1-3 ks
		int lootingLevel=0;
		Entity attacker=context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
		if(attacker instanceof LivingEntity livingAttacker){
			var enchantmentRegistry=context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			lootingLevel=livingAttacker.getMainHandItem().getEnchantmentLevel(
					enchantmentRegistry.getOrThrow(Enchantments.LOOTING)
			);
		}
		if(lootingLevel>0){
			count+=context.getRandom().nextInt(lootingLevel+1);
		}
		generatedLoot.add(new ItemStack(itemToDrop,count));
		return generatedLoot;
	}
	@Override
	public @NotNull MapCodec<? extends IGlobalLootModifier> codec(){
		return CODEC;
	}
}