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
		BlockPos origin=context.origin(); // Střed baňky pod zemí
		RandomSource random=context.random();
		// VÁŽENÝ NÁHODNÝ VÝBĚR VELIKOSTI (aby malé byly častější než obří)
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
		// OCHRANA PROTI GENEROVÁNÍ VE VELKÝCH JESKYNÍCH
		// Otestujeme několik náhodných bloků uvnitř prostoru budoucí baňky.
		int airBlocks=0;
		int checks=20;
		for(int i=0;i<checks;i++){
			int rx=random.nextInt(polomerKouleRopy)-(polomerKouleRopy/2);
			int ry=random.nextInt(polomerKouleRopy)-(polomerKouleRopy/2);
			int rz=random.nextInt(polomerKouleRopy)-(polomerKouleRopy/2);
			if(level.isEmptyBlock(origin.offset(rx,ry,rz))) airBlocks++;
		}
		// Pokud je víc než 25 % testovaných bodů čistý vzduch = jsme uvnitř obrovské jeskyně. Tím pádem generování zrušíme.
		if(airBlocks>checks/4) return false;
		BlockState fluid=DifModBlocks.CRUDE_OIL_FLUID.get().defaultBlockState();
		int centerChunkX=origin.getX()>>4;
		int centerChunkZ=origin.getZ()>>4;
		// 1. GENERACE BAŇKY (Čistá koule ropy pod zemí)
		// Žádný kamenný obal už se negeneruje. Pokud je kolem malá jeskyně, ropa tam zkrátka vyteče.
		for(int x=-polomerKouleRopy;x<=polomerKouleRopy;x++){
			for(int y=-polomerKouleRopy;y<=polomerKouleRopy;y++){
				for(int z=-polomerKouleRopy;z<=polomerKouleRopy;z++){
					double distance=Math.sqrt(x*x+y*y+z*z);
					if(distance<polomerKouleRopy){
						BlockPos currentPos=origin.offset(x,y,z);
						// Prevence pádů - kontrolujeme pouze limitní chunky 3x3
						int chunkX=currentPos.getX()>>4;
						int chunkZ=currentPos.getZ()>>4;
						if(Math.abs(chunkX-centerChunkX)<=1&&Math.abs(chunkZ-centerChunkZ)<=1)
							level.setBlock(currentPos,fluid,3); // Flag 3 - nutné pro Block Update (aby se tekutina později rozlila)
					}
				}
			}
		}
		// 2. GENERACE STOŽÁRU (Gejzíru)
		// Vyjdeme cca 2 bloky pod horním okrajem vytvořené koule a stoupáme rovnou čarou nahoru.
		BlockPos pillarStart=origin.above(polomerKouleRopy-2);
		int surfaceY=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,origin.getX(),origin.getZ());
		int maxHeight=surfaceY+vyskaGejziruNadZemi;
		for(int y=pillarStart.getY();y<=maxHeight;y++){
			// Gejzír je vygenerován jako 1x1 tenký pramen.
			BlockPos currentPillarPos=new BlockPos(origin.getX(),y,origin.getZ());
			int chunkX=currentPillarPos.getX()>>4;
			int chunkZ=currentPillarPos.getZ()>>4;
			if(Math.abs(chunkX-centerChunkX)<=1&&Math.abs(chunkZ-centerChunkZ)<=1){
				// Přepsání bloku čistou ropou (nahradí block i vzduch nad urovní země)
				level.setBlock(currentPillarPos,fluid,3);
				// === KLÍČOVÉ PRO ROZLITÍ ===
				// Pomocí scheduleTicking ihned po vygenerování donutíme ropu spustit svoje fyzikální chování (tok dolů po stranách).
				if(!fluid.getFluidState().isEmpty())
					level.scheduleTick(currentPillarPos,fluid.getFluidState().getType(),0);
			}
		}
		return true;
	}
}