package cz.maxtechnik.dif.entity.vehicle;

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
public class RemoteControlMinecart extends AbstractMinecart{
	private double push;
	private boolean isReversed=false;
	public RemoteControlMinecart(EntityType<? extends AbstractMinecart> type,Level level){
		super(type,level);
	}
	public RemoteControlMinecart(Level level,double x,double y,double z){
		super(DifModEntities.REMOTE_MINECART.get(),level,x,y,z);
	}
	public void setRemoteMovement(double push){
		this.push=push;
	}
	public void flipDirection(){
		this.isReversed=!this.isReversed;
		// Za jízdy otočíme i hybnost pro okamžitou reakci
		Vec3 motion=this.getDeltaMovement();
		this.setDeltaMovement(motion.x*-0.5,motion.y,motion.z*-0.5);
	}
	@Override
	public void tick(){
		super.tick();
		if(!this.level().isClientSide){
			Vec3 motion=this.getDeltaMovement();
			double horizontalSpeed=motion.horizontalDistance();
			// Plyn (push) je nyní vždy kladný (0.0 až 1.0)
			if(this.push>0.01){
				Vec3 railDir;
				// Určení směru, kam zrovna vedou koleje
				if(horizontalSpeed>0.01){
					railDir=motion.normalize();
				}else{
					// Pokud stojíme, určíme směr podle rotace
					float f=this.getYRot()*((float)Math.PI/180F);
					railDir=new Vec3(-Math.sin(f),0,Math.cos(f));
				}
				double accel=0.06;
				double maxSpeed=0.4;
				// Výsledný impuls
				Vec3 thrust=railDir.scale(this.push*accel);
				Vec3 newMotion=motion.add(thrust);
				// Omezení maximální rychlosti
				if(newMotion.horizontalDistance()>maxSpeed) newMotion=newMotion.normalize().scale(maxSpeed);
				this.setDeltaMovement(newMotion.x,motion.y,newMotion.z);
			}else{
				// Pasivní tření
				if(horizontalSpeed>0.001){
					this.setDeltaMovement(motion.multiply(0.9,1.0,0.9));
				}else{
					this.setDeltaMovement(0,motion.y,0);
				}
			}
			this.push=0;
		}
	}
	@Override
	public @NotNull InteractionResult interact(Player player,@NotNull InteractionHand hand){
		ItemStack stack=player.getItemInHand(hand);
		if(stack.is(DifModItems.REMOTE_CONTROLLER.get())){
			if(!player.level().isClientSide){
				CompoundTag nbt=stack.getOrCreateTag();
				if(nbt.hasUUID("LinkedCart")&&nbt.getUUID("LinkedCart").equals(this.getUUID())){
					this.flipDirection(); // PŘEPNUTÍ SMĚRU
					player.displayClientMessage(Component.literal("Direction flipped!"),true);
				}else{
					nbt.putUUID("LinkedCart",this.getUUID());
					player.displayClientMessage(Component.literal("Minecart linked!"),true);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player,hand);
	}
	// NBT Data pro uložení orientace
	@Override
	protected void addAdditionalSaveData(@NotNull CompoundTag tag){
		super.addAdditionalSaveData(tag);
		tag.putBoolean("isReversed",isReversed);
	}
	@Override
	protected void readAdditionalSaveData(@NotNull CompoundTag tag){
		super.readAdditionalSaveData(tag);
		this.isReversed=tag.getBoolean("isReversed");
	}
	@Override
	public @NotNull Item getDropItem(){
		return DifModItems.REMOTE_MINECART_ITEM.get();
	}
	@Override
	public @NotNull Type getMinecartType(){
		return Type.RIDEABLE;
	}
	@Override
	public @NotNull ItemStack getPickResult(){
		return new ItemStack(DifModItems.REMOTE_MINECART_ITEM.get());
	}
	@Override
	public @NotNull BlockState getDefaultDisplayBlockState(){
		return net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING,net.minecraft.core.Direction.NORTH);
	}
}