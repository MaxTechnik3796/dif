package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.lang.Math.round;
public class RemoteControlMinecart extends AbstractMinecart{
	private double push;
	public RemoteControlMinecart(EntityType<? extends AbstractMinecart> type,Level level){
		super(type,level);
	}
	public RemoteControlMinecart(Level level,double x,double y,double z){
		super(DifModEntities.REMOTE_MINECART.get(),level,x,y,z);
	}
	@Override
	public @NotNull Item getDropItem(){
		return DifModItems.REMOTE_MINECART_ITEM.get();
	}
	// Definujeme, jaký blok se má zobrazovat uvnitř (např. tvůj Chunk Loader)
	@Override
	public @NotNull BlockState getDefaultDisplayBlockState(){
		// NORTH je v Minecraftu pro Minecarty "předek"
		return net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING,net.minecraft.core.Direction.NORTH);
	}
	@Override
	public @NotNull Type getMinecartType(){
		return Type.RIDEABLE; // Nebo si vytvoř vlastní typ
	}
	// Metoda, kterou budeme volat z dálkového ovladače (přes Packet)
	public void setRemoteMovement(double push){
		this.push=push;
	}
	@Override
	public void tick(){
		super.tick();
		if(!this.level().isClientSide){
			Vec3 currentMotion=this.getDeltaMovement();
			// 1. PASIVNÍ BRŽDĚNÍ (Důležité pro pocit kontroly)
			if(currentMotion.horizontalDistance()>0.001)this.setDeltaMovement(currentMotion.multiply(0.85,1,0.85));
			float yaw=this.getYRot();
			//DifMod.LOGGER.debug(String.valueOf(push));
			switch(round(yaw)){
				case 0->this.setDeltaMovement(-push,currentMotion.y,currentMotion.z);
				case 90->this.setDeltaMovement(currentMotion.x,currentMotion.y,-push);
				case 180->this.setDeltaMovement(push,currentMotion.y,currentMotion.z);
				case -90,270->this.setDeltaMovement(currentMotion.x,currentMotion.y,push);
			}
			push*=0.1;
			// 2. AKTIVNÍ POHON (Plynový pedál)
			// remotePushX bereme jako plyn (1.0 = vpřed, -1.0 = vzad)
			/*if (Math.abs(remotePushX) > 0.01) {
				// Získáme čistý směr, kterým Minecart kouká
				float yaw = this.getYRot();
				Vec3 lookDir = Vec3.directionFromRotation(0, yaw);

				// Cílová rychlost, kterou chceme mít (např. max 0.4 bloků/tick)
				double targetSpeed = remotePushX * 0.4;

				// TADY JE TA ZMĚNA:
				// Místo .add() použijeme postupné přibližování k cílové rychlosti (Lerp)
				double newX = currentMotion.x + (lookDir.x * targetSpeed - currentMotion.x) * 0.3;
				double newZ = currentMotion.z + (lookDir.z * targetSpeed - currentMotion.z) * 0.3;

				this.setDeltaMovement(newX, currentMotion.y, newZ);
			}

			// 3. VYČIŠTĚNÍ IMPULSU
			// Aby se plyn neopakoval, pokud nepřijde další packet
			remotePushX *= 0.1;*/
		}
	}
	@Override
	public @NotNull ItemStack getPickResult(){
		return new ItemStack(DifModItems.REMOTE_MINECART_ITEM.get());
	}
	@Override
	public @NotNull InteractionResult interact(Player player,@NotNull InteractionHand hand){
		ItemStack stack=player.getItemInHand(hand);
		if(stack.is(DifModItems.REMOTE_CONTROLLER.get())){
			if(!player.level().isClientSide){
				// Uložíme UUID Minecartu do NBT ovladače
				CompoundTag nbt=stack.getOrCreateTag();
				nbt.putUUID("LinkedCart",this.getUUID());
				player.displayClientMessage(Component.literal("Minecart linked!"),true);
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player,hand);
	}
}