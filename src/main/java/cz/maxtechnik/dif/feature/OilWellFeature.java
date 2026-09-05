package cz.maxtechnik.dif.feature;

import com.mojang.serialization.Codec;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
public class OilWellFeature extends Feature<NoneFeatureConfiguration>{
	public enum WellSize{
		SMALL(8,8,50),
		MEDIUM(12,16,30),
		LARGE(16,24,15),
		HUGE(32,48,5);
		public final int radiusSphereOil;
		public final int geyserHighAboveGround;
		public final int weight;
		WellSize(int radiusSphereOil,int geyserHighAboveGround,int weight){
			this.radiusSphereOil=radiusSphereOil;
			this.geyserHighAboveGround=geyserHighAboveGround;
			this.weight=weight;
		}
	}
	public OilWellFeature(Codec<NoneFeatureConfiguration> codec){
		super(codec);
	}
	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
		WorldGenLevel level=context.level();
		BlockPos origin=context.origin();
		RandomSource random=context.random();
		// VÁŽENÝ NÁHODNÝ VÝBĚR VELIKOSTI
		int totalWeight=0;
		for(WellSize s: WellSize.values()) totalWeight+=s.weight;
		int r=random.nextInt(totalWeight);
		WellSize size=WellSize.SMALL;
		int currentWeight=0;
		for(WellSize s: WellSize.values()){
			currentWeight+=s.weight;
			if(r<currentWeight){
				size=s;
				break;
			}
		}
		int polomerKouleRopy=size.radiusSphereOil;
		int vyskaGejziruNadZemi=size.geyserHighAboveGround;
		int airBlocks=0;
		int checks=20;
		for(int i=0;i<checks;i++){
			int rx=random.nextInt(polomerKouleRopy)-(polomerKouleRopy/2);
			int ry=random.nextInt(polomerKouleRopy)-(polomerKouleRopy/2);
			int rz=random.nextInt(polomerKouleRopy)-(polomerKouleRopy/2);
			if(level.isEmptyBlock(origin.offset(rx,ry,rz))) airBlocks++;
		}
		if(airBlocks>checks/4) return false;
		BlockState fluid=DifModBlocks.CRUDE_OIL_FLUID.get().defaultBlockState();
		int centerChunkX=origin.getX()>>4;
		int centerChunkZ=origin.getZ()>>4;
		// GENERACE BAŇKY
		for(int x=-polomerKouleRopy;x<=polomerKouleRopy;x++){
			for(int y=-polomerKouleRopy;y<=polomerKouleRopy;y++){
				for(int z=-polomerKouleRopy;z<=polomerKouleRopy;z++){
					double distance=Math.sqrt(x*x+y*y+z*z);
					if(distance<polomerKouleRopy){
						BlockPos currentPos=origin.offset(x,y,z);
						int chunkX=currentPos.getX()>>4;
						int chunkZ=currentPos.getZ()>>4;
						if(Math.abs(chunkX-centerChunkX)<=1&&Math.abs(chunkZ-centerChunkZ)<=1)
							level.setBlock(currentPos,fluid,3);
					}
				}
			}
		}
		// GENERACE STOŽÁRU
		BlockPos pillarStart=origin.above(polomerKouleRopy-2);
		int surfaceY=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,origin.getX(),origin.getZ());
		int maxHeight=surfaceY+vyskaGejziruNadZemi;
		for(int y=pillarStart.getY();y<=maxHeight;y++){
			// Gejzír
			BlockPos currentPillarPos=new BlockPos(origin.getX(),y,origin.getZ());
			int chunkX=currentPillarPos.getX()>>4;
			int chunkZ=currentPillarPos.getZ()>>4;
			if(Math.abs(chunkX-centerChunkX)<=1&&Math.abs(chunkZ-centerChunkZ)<=1){
				level.setBlock(currentPillarPos,fluid,3);
				// KLÍČOVÉ PRO ROZLITÍ
				if(!fluid.getFluidState().isEmpty())
					level.scheduleTick(currentPillarPos,fluid.getFluidState().getType(),0);
			}
		}
		return true;
	}
}