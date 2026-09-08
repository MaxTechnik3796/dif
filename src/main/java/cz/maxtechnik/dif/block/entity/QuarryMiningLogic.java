package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
public class QuarryMiningLogic{
	public static float doMiningTick(QuarryBlockEntity be,Level level,float miningProgressAcc,float progressStep){
		if(!(level instanceof ServerLevel sl)) return miningProgressAcc;
		QuarryAreaManager areaManager=be.getAreaManager();
		BlockPos miningPos=areaManager.getMiningPos();
		if(miningPos==null){
			areaManager.resetMiningPos(be.getBlockPos().getY());
			miningPos=areaManager.getMiningPos();
			be.setChanged();
		}
		ItemStack tool=buildSimulatedTool();
		miningProgressAcc+=progressStep;
		int safety=0;
		try{
			while(safety++<1000){
				// Přeskočit prázdné bloky a bloky obsahující kapalinu
				while(level.isEmptyBlock(miningPos)||!level.getBlockState(miningPos).getFluidState().isEmpty()){
					if(areaManager.advanceMiningPos(level)){
						be.finishMining();
						return miningProgressAcc;
					}
					miningPos=areaManager.getMiningPos();
				}
				BlockState target=level.getBlockState(miningPos);
				// Řešení nezničitelných bloků
				float hardness=target.getDestroySpeed(level,miningPos);
				if(hardness<0){
					miningProgressAcc=0f;
					if(areaManager.advanceMiningPos(level)){
						be.finishMining();
						return miningProgressAcc;
					}
					miningPos=areaManager.getMiningPos();
					continue;
				}
				// Vlastní těžení pevného bloku
				float required=Math.max(1f,hardness*10f);
				if(miningProgressAcc<required){
					return miningProgressAcc;
				}
				miningProgressAcc-=required;
				List<ItemStack> drops=Block.getDrops(target,sl,miningPos,sl.getBlockEntity(miningPos),null,tool);
				level.removeBlock(miningPos,false);
				if(!drops.isEmpty()){
					distributeDrops(be,level,drops);
				}
				if(areaManager.advanceMiningPos(level)){
					be.finishMining();
					return miningProgressAcc;
				}
				miningPos=areaManager.getMiningPos();
			}
			return miningProgressAcc;
		}finally{
			be.setChanged();
		}
	}
	private static ItemStack buildSimulatedTool(){
		return new ItemStack(Items.NETHERITE_PICKAXE);
	}
	private static void distributeDrops(QuarryBlockEntity be,Level level,List<ItemStack> drops){
		BlockPos pos=be.getBlockPos();
		List<IItemHandler> handlers=new ArrayList<>(6);
		for(Direction dir: Direction.values()){
			IItemHandler h=level.getCapability(Capabilities.ItemHandler.BLOCK,pos.relative(dir),dir.getOpposite());
			if(h!=null) handlers.add(h);
		}
		for(ItemStack drop: drops){
			if(drop.isEmpty()) continue;
			ItemStack rem=drop;
			for(IItemHandler h: handlers){
				if(rem.isEmpty()) break;
				rem=ItemHandlerHelper.insertItemStacked(h,rem,false);
			}
			if(!rem.isEmpty()){
				Block.popResource(level,pos.above(),rem);
			}
		}
	}
}
