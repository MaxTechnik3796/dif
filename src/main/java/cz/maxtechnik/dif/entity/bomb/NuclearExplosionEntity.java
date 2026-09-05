package cz.maxtechnik.dif.entity.bomb;

import cz.maxtechnik.dif.init.events.client.NukeSoundEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class NuclearExplosionEntity extends Entity{
	private static final int LIFETIME_TICKS=750;

	private final NukeCraterHandler craterHandler=new NukeCraterHandler();
	private int age=0;
	private boolean craterFinished=false;

	public NuclearExplosionEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder){
	}

	@Override
	public void tick(){
		super.tick();
		if(level().isClientSide) return;
		if(!(level() instanceof ServerLevel sl)) return;

		double x=getX(), y=getY(), z=getZ();

		// Fáze 0: Spuštění zvuku, klientského záblesku a okamžité radiace s knockbackem
		if(age==0){
			NukeSoundEffect.play(sl,x,y,z);
			NukeRadiationHandler.apply(level(),blockPosition());
		}

		// Výpočet a tvorba kráteru synchronizovaně s čelem rázové vlny
		double groundRadius=NukeShockwaveHandler.getGroundWaveRadius(age);
		if(!craterFinished){
			craterFinished=craterHandler.tick(level(),blockPosition(),groundRadius,random);
		}

		// Dva prstence rázové vlny (horní tenký vzdušný + dolní pozemní)
		NukeShockwaveHandler.tick(sl,x,y,z,age);

		// Vizuální částicový systém (detonace, stoupající koule, noha, hřib)
		NukeParticleHandler.tick(sl,x,y,z,age,random);

		age++;
		if(age>=LIFETIME_TICKS){
			discard();
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		age=tag.getInt("ExplosionAge");
		craterFinished=tag.getBoolean("CraterFinished");
		craterHandler.readNbt(tag);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putInt("ExplosionAge",age);
		tag.putBoolean("CraterFinished",craterFinished);
		craterHandler.writeNbt(tag);
	}

	@Override
	public boolean isAttackable(){
		return false;
	}

}