package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.SpaceshipBlockEntity;
import cz.maxtechnik.dif.block.space.Spaceship;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
public class SpaceshipControl{
	public static void planet(LevelAccessor world,int x,int y,int z,Player entity,int buttonId){
		if(world.isClientSide()) return;
		ServerLevel serverWorld=(ServerLevel)world; // Zde už máme správný typ
		BlockPos rocketPos=new BlockPos(x,y,z);
		BlockEntity be=world.getBlockEntity(rocketPos);
		if(!(be instanceof SpaceshipBlockEntity)) return;
		// VOLÁNÍ NAD serverWorld:
		IItemHandler inventory=serverWorld.getCapability(Capabilities.ItemHandler.BLOCK,rocketPos,null);
		if(inventory==null) return;
		// Kontrola paliva
		ItemStack fuelStack=inventory.getStackInSlot(0);
		if(fuelStack.getItem()!=DifModItems.ROCKET_FUEL.get()||fuelStack.getCount()<1){
			entity.displayClientMessage(Component.literal("§cOut of fuel!"),true);
			return;
		}
		// 2. LOGIKA DESTINACE
		ResourceKey<Level> destKey;
		String pName;
		int scroll=getNBT(world,x,y,z,"scroll");
		int targetId=scroll+buttonId;
		if(targetId==0){
			destKey=Level.OVERWORLD;
			pName="Overworld";
		}else if(targetId==1){
			destKey=ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit"));
			pName="Orbit";
		}else if(targetId==2){
			destKey=ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"));
			pName="Moon";
		}else return;
		if(serverWorld.dimension().equals(destKey)){
			entity.displayClientMessage(Component.literal("§cYou already here."),true);
			return;
		}
		// 3. KONTROLA VZLETU
		for(int h=y+2;h<world.getMaxBuildHeight();h++){
			if(!world.isEmptyBlock(rocketPos.atY(h))){
				entity.displayClientMessage(Component.literal("§cStart blocked by an obstacle above the spaceship!"),true);
				return;
			}
		}
		ServerLevel destWorld=serverWorld.getServer().getLevel(destKey);
		if(destWorld==null) return;
		// 4. HLEDÁNÍ POVRCHU
		int maxY=destWorld.getMaxBuildHeight();
		int minY=destWorld.getMinBuildHeight();
		int foundY=-999;
		BlockPos.MutableBlockPos scanPos=new BlockPos.MutableBlockPos();
		outerLoop:
		for(int h=maxY-1;h>minY;h--){
			for(int dx=-1;dx<=1;dx++){
				for(int dz=-1;dz<=1;dz++){
					scanPos.set(x+dx,h,z+dz);
					BlockState state=destWorld.getBlockState(scanPos);
					if(!state.isAir()){
						foundY=h;
						break outerLoop;
					}
				}
			}
		}
		int landingY=(foundY==-999)?64:foundY;
		if(landingY>=314){
			entity.displayClientMessage(Component.literal("§cCannot land! Target height too close to limit!"),false);
			return;
		}
		if(!isAreaClear(destWorld,x,landingY+1,z)){
			entity.displayClientMessage(Component.literal("§eLanding site obstructed!"),false);
			return;
		}
		// --- LET ---
		// 6. SPOTŘEBA PALIVA A DATA (NBT vyžaduje registry)
		inventory.extractItem(0,1,false);
		BlockState rocketState=world.getBlockState(rocketPos);
		// saveWithFullMetadata se v 1.21 často nahrazuje saveWithId pro přenos BE
		CompoundTag rocketData=be.saveWithFullMetadata(serverWorld.registryAccess());
		for(int i=0;i<inventory.getSlots();i++){
			inventory.extractItem(i,64,false);
		}
		// 7. PRE-LOADING
		ChunkPos cp=new ChunkPos(x>>4,z>>4);
		destWorld.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT,cp,2,entity.getId());
		// 8. ODSTRANĚNÍ
		world.destroyBlock(rocketPos,false);
		// 9. PLATFORMA
		BlockPos platformPos=new BlockPos(x,landingY,z);
		BlockPos newMasterPos=platformPos.above(2);
		for(int px=-2;px<=2;px++){
			for(int pz=-2;pz<=2;pz++){
				BlockPos pPos=platformPos.offset(px,0,pz);
				if(destWorld.isEmptyBlock(pPos)){
					destWorld.setBlock(pPos,DifModBlocks.SPACE_SCAFFOLDING.get().defaultBlockState(),3);
				}
			}
		}
		// 10. TELEPORT A OBNOVA
		if(entity instanceof ServerPlayer sp){
			sp.teleportTo(destWorld,x+0.5,newMasterPos.getY()+2.0,z+0.5,entity.getYRot(),entity.getXRot());
		}
		destWorld.setBlock(newMasterPos,rocketState,3);
		if(rocketState.getBlock() instanceof Spaceship spaceship){
			for(BlockPos gp: spaceship.getGhostPositions(newMasterPos)){
				destWorld.setBlock(gp,DifModBlocks.SPACESHIP_GHOST_BLOCK.get().defaultBlockState(),3);
			}
		}
		BlockEntity newBe=destWorld.getBlockEntity(newMasterPos);
		if(newBe instanceof SpaceshipBlockEntity){
			// Načtení dat v 1.21 vyžaduje registry
			newBe.loadWithComponents(rocketData,destWorld.registryAccess());
			newBe.setChanged();
		}
		entity.displayClientMessage(Component.literal("§aYou have successfully landed on "+pName),true);
	}
	private static boolean isAreaClear(Level world,int centerX,int startY,int centerZ){
		int radius=1;
		BlockPos.MutableBlockPos checkPos=new BlockPos.MutableBlockPos();
		for(int dx=-radius;dx<=radius;dx++){
			for(int dz=-radius;dz<=radius;dz++){
				for(int dy=0;dy<5;dy++){
					checkPos.set(centerX+dx,startY+dy,centerZ+dz);
					if(!world.isEmptyBlock(checkPos)) return false;
				}
			}
		}
		return true;
	}
	public static void arrow(LevelAccessor world,int x,int y,int z,int buttonId){
		int scroll=getNBT(world,x,y,z,"scroll");
		if(buttonId==4&&scroll>0) setNBT(world,x,y,z,"scroll",scroll-1);
		if(buttonId==5&&scroll<12) setNBT(world,x,y,z,"scroll",scroll+1);
	}
	public static int getNBT(LevelAccessor world,double x,double y,double z,String tag){
		BlockEntity be=world.getBlockEntity(BlockPos.containing(x,y,z));
		if(be!=null) return be.getPersistentData().getInt(tag);
		return 0;
	}
	public static void setNBT(LevelAccessor world,double x,double y,double z,String tag,int value){
		BlockEntity be=world.getBlockEntity(BlockPos.containing(x,y,z));
		if(be!=null){
			be.getPersistentData().putInt(tag,value);
			be.setChanged();
		}
	}
}