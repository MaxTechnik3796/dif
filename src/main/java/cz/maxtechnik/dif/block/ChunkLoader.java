package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.ChunkLoaderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class ChunkLoader extends Block implements EntityBlock{
	public static final BooleanProperty LIT=BlockStateProperties.LIT;
	public ChunkLoader(){
		super(BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.strength(3.5F)
				.sound(SoundType.LODESTONE)
				.requiresCorrectToolForDrops()
				.lightLevel(s->s.getValue(LIT)?12:0));
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT,true));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(LIT);
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new ChunkLoaderBlockEntity(pos,blockState);
	}
	@Override
	public void animateTick(BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull RandomSource random){
		if(!state.getValue(LIT)) return;
		double centerX=pos.getX()+0.5;
		double centerY=pos.getY()+0.5;
		double centerZ=pos.getZ()+0.5;
		// Rychlost rotace a poloměr (radius)
		double time=(double)level.getGameTime()*0.2;
		double radius=1.3;
		// Tři orbity (X-Y, X-Z, Y-Z) přesně podle tvého nákresu
		for(int orbit=0;orbit<3;orbit++){
			// orbitOffset zajistí, že částice na každé dráze začínají v jiném bodě, aby se nesrazily
			double orbitOffset=(orbit*Math.PI*2)/3;
			// Zvýšeno na 12 segmentů pro velmi častý a hustý trail
			for(int segment=0;segment<12;segment++){
				// Každý segment je kousek posunutý zpět pro efekt plynulé čáry
				double sTime=time-(segment*0.08);
				double x, y, z;
				double v=Math.cos(sTime+orbitOffset)*radius;
				double v1=Math.sin(sTime+orbitOffset)*radius;
				double x1=centerX+v;
				if(orbit==0){ // DRÁHA 1: Obíhá vertikálně (kolem osy Z)
					x=x1;
					y=centerY+v1;
					z=centerZ;
				}else if(orbit==1){ // DRÁHA 2: Obíhá horizontálně (kolem osy Y)
					x=x1;
					y=centerY;
					z=centerZ+v1;
				}else{ // DRÁHA 3: Obíhá vertikálně (kolem osy X)
					x=centerX;
					y=centerY+v;
					z=centerZ+v1;
				}
				// První částice je jasná, zbytek tvoří trail
				if(segment==0){
					level.addParticle(ParticleTypes.END_ROD,x,y,z,0,0,0);
				}else{
					// Používáme PORTAL nebo SOUL_FIRE_FLAME pro barevný efekt
					level.addParticle(ParticleTypes.PORTAL,x,y,z,0,0,0);
				}
			}
		}
		// Extra glint efekt na povrchu (pokud nechceš Mixiny)
		if(random.nextFloat()<0.3f){
			level.addParticle(ParticleTypes.WITCH,
					pos.getX()+random.nextDouble(),
					pos.getY()+random.nextDouble(),
					pos.getZ()+random.nextDouble(),0,0,0);
		}
	}
	// Ostatní metody zůstávají stejné jako dříve...
	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())){
			if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity loader){
				loader.handleRemoval();
			}
			super.onRemove(state,level,pos,newState,isMoving);
		}
	}
	@Override
	public void setPlacedBy(Level level,@NotNull BlockPos pos,@NotNull BlockState blockState,@Nullable LivingEntity placer,@NotNull ItemStack itemStack){
		if(!level.isClientSide&&placer instanceof Player player){
			if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity be){
				be.setOwner(player.getUUID(),player.getName().getString());
				be.updateStatus(!level.hasNeighborSignal(pos));
			}
		}
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		if(!level.isClientSide){
			boolean shouldBeLit=!level.hasNeighborSignal(pos);
			if(blockState.getValue(LIT)!=shouldBeLit){
				level.setBlock(pos,blockState.setValue(LIT,shouldBeLit),3);
				if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity be){
					be.updateStatus(shouldBeLit);
				}
			}
		}
	}
}