package cz.maxtechnik.dif.item.tool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Example extends Item {
    public Example(){
        super(new Properties());
    }
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,@NotNull Player player,@NotNull InteractionHand hand){
		super.use(world,player,hand);
		if (!world.isClientSide()){
			Entity owner=world.getEntity();
			if(owner==null){
				owner=player;
			}
			Projectile entityToSpawn=new Object(){
				public Projectile getProjectile(Level level) {
					Projectile entityToSpawn = new ThrownEnderpearl(EntityType.ENDER_PEARL, level);
					entityToSpawn.setOwner(owner);
					return entityToSpawn;
				}
			}.getProjectile(world);
			entityToSpawn.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
			entityToSpawn.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, 1, 0);
			world.addFreshEntity(entityToSpawn);
		}
		return InteractionResultHolder.consume(player.getItemInHand(hand));
	}

	@Override
	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack,@NotNull Player player,@NotNull LivingEntity entity,@NotNull InteractionHand hand){
		itemStack.getOrCreateTag().putUUID("uuid",entity.getUUID());
		return InteractionResult.PASS;
	}
}
