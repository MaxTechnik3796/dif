package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.block.RemoteMinecartBlock;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
	private double xPush;
	private double zPush;
	public RemoteControlMinecart(EntityType<? extends AbstractMinecart> type,Level level){
		super(type,level);
	}
	public RemoteControlMinecart(Level level,double x,double y,double z){
		super(DifModEntities.REMOTE_MINECART.get(),level,x,y,z);
	}
	public void setRemoteMovement(double push){
		this.push=push;
	}
	@Override
	protected void moveAlongTrack(@NotNull BlockPos pPos,@NotNull BlockState pState){
		super.moveAlongTrack(pPos,pState);
		Vec3 motion=this.getDeltaMovement();
		double horizontalDistSqr=motion.horizontalDistanceSqr();
		double pushSqr=this.xPush*this.xPush+this.zPush*this.zPush;
		if(pushSqr>1.0E-4D&&horizontalDistSqr>0.01D){
			double d4=Math.sqrt(horizontalDistSqr);
			double d5=Math.sqrt(pushSqr);
			this.xPush=motion.x/d4*d5;
			this.zPush=motion.z/d4*d5;
		}
	}
	public void flipDirection(){
		this.xPush*=-1;
		this.zPush*=-1;
		Vec3 motion=this.getDeltaMovement();
		this.setDeltaMovement(motion.x*-0.5,motion.y,motion.z*-0.5);
	}
	@Override
	public void tick(){
		super.tick();
		if(!this.level().isClientSide){
			Vec3 motion=this.getDeltaMovement();
			if(this.push>0.01){
				// Pokud nemáme nastavený směr (stojíme), určíme ho podle rotace a isReversed
				if(xPush*xPush+zPush*zPush<1.0E-4D){
					float f=this.getYRot()*((float)Math.PI/180F);
					this.xPush=-Math.sin(f);
					this.zPush=Math.cos(f);
				}
				// Aplikace síly jako u Furnace Minecartu
				double accel=0.15D; // Síla motoru
				this.setDeltaMovement(motion.x*0.9D+xPush*accel,motion.y,motion.z*0.9D+zPush*accel);
			}else{
				// Přirozené zpomalování
				if(motion.horizontalDistance()>0.001){
					this.setDeltaMovement(motion.multiply(0.95,1,0.95));
				}
				// Vynulujeme push vektory, když se neovládá, aby se příště mohl směr určit znovu
				this.xPush=0;
				this.zPush=0;
			}
			this.push=0;
		}else{
			if(this.getDeltaMovement().horizontalDistance()>0.01D&&this.random.nextInt(4)==0)
				this.level().addParticle(ParticleTypes.LARGE_SMOKE,this.getX(),this.getY()+0.8D,this.getZ(),0D,0D,0D);
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
	@Override
	public float getMaxCartSpeedOnRail(){
		return 0.6F;
	}
	@Override
	public @NotNull Item getDropItem(){
		return DifModItems.REMOTE_MINECART.get();
	}
	@Override
	public @NotNull Type getMinecartType(){
		return Type.RIDEABLE;
	}
	@Override
	public @NotNull ItemStack getPickResult(){
		return new ItemStack(DifModItems.REMOTE_MINECART.get());
	}
	@Override
	public @NotNull BlockState getDefaultDisplayBlockState(){
		return DifModBlocks.REMOTE_MINECART_BLOCK.get().defaultBlockState().setValue(RemoteMinecartBlock.FACING,Direction.NORTH).setValue(RemoteMinecartBlock.WATERLOGGED,false).setValue(RemoteMinecartBlock.LIT,this.getDeltaMovement().horizontalDistance()>0.01D);
	}
}