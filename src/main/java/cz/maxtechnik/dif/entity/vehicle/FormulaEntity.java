package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.basic.DifModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
public class FormulaEntity extends BaseCarEntity{
	private static final EntityDataAccessor<Integer> DATA_COLOR=SynchedEntityData.defineId(FormulaEntity.class,EntityDataSerializers.INT);
	// Konstantní hitbox – velikost jako při 45° rotaci (nejhorší případ), nikdy se nemění
	// sqrt(0.6² + 1.8²) ≈ 1.9 jako poloměr
	private static final double HALF_BOX=1.7D;
	private static final float HH=1.4F;
	public FormulaEntity(EntityType<?> t,Level l){
		super(t,l);
	}
	// Konstantní AABB – nemění se s rotací, vždy stejně velký
	private AABB buildAABB(){
		return new AABB(getX()-HALF_BOX,getY(),getZ()-HALF_BOX,getX()+HALF_BOX,getY()+HH,getZ()+HALF_BOX);
	}
	@Override
	public void tick(){
		super.tick();
		setBoundingBox(buildAABB());
	}
	@Override
	protected @NotNull AABB makeBoundingBox(){
		return buildAABB();
	}
	@Override
	protected void positionRider(@NotNull Entity rider,Entity.@NotNull MoveFunction moveFunction){
		if(this.hasPassenger(rider)){
			// offsetZ: Kladné číslo posouvá hráče DOPŘEDU.
			// Pokud je teď moc vzadu, zvyšte toto číslo (např. z -0.2F na 0.3F nebo 0.5F).
			float offsetZ=0.55F;
			float offsetY=-0.525F; // Výška zůstává, aby seděl v díře
			double x=this.getX()+(double)(net.minecraft.util.Mth.sin(-this.getYRot()*((float)Math.PI/180F))*offsetZ);
			double z=this.getZ()+(double)(net.minecraft.util.Mth.cos(this.getYRot()*((float)Math.PI/180F))*offsetZ);
			// In 1.21.1: getPassengersRidingOffset() and getMyRidingOffset() were removed
			// Old value: getPassengersRidingOffset()=0.4D, player getMyRidingOffset()≈-0.35D
			moveFunction.accept(rider,x,this.getY()+0.4D+offsetY,z);
		}
	}
	@Override
	protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger, EntityDimensions dimensions, float partialTick){
		// Default attachment point (used when positionRider calls through)
		return new Vec3(0.0D, 0.4D - 0.525D, 0.55D);
	}
	// === Barva ===
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		super.defineSynchedData(builder);
		builder.define(DATA_COLOR,0xFFFFFF);
	}
	public int getColor(){
		return entityData.get(DATA_COLOR);
	}
	public void setColor(int c){
		entityData.set(DATA_COLOR,c);
	}
	@Override
	protected void readAdditionalSaveData(CompoundTag n){
		super.readAdditionalSaveData(n);
		if(n.contains("Color")) setColor(n.getInt("Color"));
	}
	@Override
	protected void addAdditionalSaveData(CompoundTag n){
		super.addAdditionalSaveData(n);
		n.putInt("Color",getColor());
	}
	@Override
	public @NotNull InteractionResult interact(Player player,@NotNull InteractionHand hand){
		ItemStack s=player.getItemInHand(hand);
		if(s.getItem() instanceof DyeItem dye){
			if(!level().isClientSide){
				// In 1.21.1: getTextureDiffuseColors() (float[]) replaced by getTextureDiffuseColor() (int ARGB)
				int c=dye.getDyeColor().getTextureDiffuseColor();
				setColor(c&0xFFFFFF);
				if(!player.getAbilities().instabuild) s.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player,hand);
	}
	// === Vlastnosti formule ===
	@Override
	public SoundEvent getEngineSound(){
		return DifModSounds.FORMULA_ENGINE.get();
	}
	@Override
	public Item getDropItem(){
		return DifModItems.FORMULA_ITEM.get();
	}
	@Override
	public float getCustomStepHeight(){
		return 0.75F;
	}
	@Override
	public float getMaxSpeedKmh(){
		return 330F;
	}
	@Override
	public float getBaseAcceleration(){
		return 0.042F;
	}
	@Override
	public float[] getGearRatios(){
		return new float[]{3.35F,2.47F,1.97F,1.63F,1.39F,1.23F,1.18F};
	}
	@Override
	public int getShiftCooldownTicks(){
		return 2;
	}
	@Override
	public float getIdleRPM(){
		return 5000F;
	}
	@Override
	public float getMaxRPM(){
		return 18000F;
	}
	@Override
	public float getRedlineRPM(){
		return 17500F;
	}
	@Override
	public float getDownforceCoefficient(){
		return 2F;
	}
	@Override
	public float getAeroDrag(){
		return 0.00018F;
	}
	@Override
	public float getBrakingDeceleration(){
		return 0.1F;
	}
	@Override
	public float getBaseHandling(){
		return 5.5F;
	}
	@Override
	public float getHighSpeedSteerReduction(){
		return 0.3F;
	}
	@Override
	public float getCrashDamageThresholdKmh(){
		return 60F;
	}
	@Override
	public float getCrashDamageMultiplier(){
		return 0.12F;
	}
	@Override
	public Fluid getFuelFluid(){
		return Fluids.LAVA;
	}
	@Override
	public float getMaxFuelMb(){
		return 24000F;
	}
	@Override
	public float getInitialFuelMb(){
		return 0F;
	}
	@Override
	public float getFuelConsumptionLowMbPerTick(){
		return 0.5F;
	}
	@Override
	public float getFuelConsumptionHighMbPerTick(){
		return 1F;
	}
	@Override
	public float getFuelSpeedThresholdKmh(){
		return 150F;
	}
}