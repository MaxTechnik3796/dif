package cz.maxtechnik.dif.block;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import cz.maxtechnik.dif.init.other.DifModSpriteShifts;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelData.Builder;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public class DistillationTankModel extends CTModel{
	private static final ModelProperty<CullData> CULL_PROPERTY=new ModelProperty<>();
	public static DistillationTankModel standard(BakedModel originalModel){
		return new DistillationTankModel(originalModel,
				DifModSpriteShifts.DISTILLATION_TANK,
				DifModSpriteShifts.DISTILLATION_TANK_TOP,
				DifModSpriteShifts.DISTILLATION_TANK_INNER);
	}
	private DistillationTankModel(BakedModel originalModel,CTSpriteShiftEntry side,
	                              CTSpriteShiftEntry top,CTSpriteShiftEntry inner){
		super(originalModel,new DistillationTankCTBehaviour(side,top,inner));
	}
	@Override
	protected ModelData.Builder gatherModelData(Builder builder,BlockAndTintGetter world,
	                                            BlockPos pos,BlockState state,ModelData blockEntityData){
		super.gatherModelData(builder,world,pos,state,blockEntityData);
		CullData cullData=new CullData();
		for(Direction d: Iterate.horizontalDirections){
			BlockPos neighbor=pos.relative(d);
			boolean sameTank=world.getBlockState(neighbor).getBlock() instanceof DistillationTank;
			cullData.setCulled(d,sameTank&&ConnectivityHandler.isConnected(world,pos,neighbor));
		}
		return builder.with(CULL_PROPERTY,cullData);
	}
	@Override
	public @NotNull List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource rand,
	                                         ModelData extraData,RenderType renderType){
		if(side!=null)
			return Collections.emptyList();
		List<BakedQuad> quads=new java.util.ArrayList<>();
		for(Direction d: Iterate.directions){
			if(extraData.has(CULL_PROPERTY)&&extraData.get(CULL_PROPERTY).isCulled(d))
				continue;
			quads.addAll(super.getQuads(state,d,rand,extraData,renderType));
		}
		quads.addAll(super.getQuads(state,null,rand,extraData,renderType));
		return quads;
	}
	private static class CullData{
		boolean[] culledFaces;
		public CullData(){
			culledFaces=new boolean[4];
			Arrays.fill(culledFaces,false);
		}
		void setCulled(Direction face,boolean cull){
			if(face.getAxis().isVertical())
				return;
			culledFaces[face.get2DDataValue()]=cull;
		}
		boolean isCulled(Direction face){
			if(face.getAxis().isVertical())
				return false;
			return culledFaces[face.get2DDataValue()];
		}
		@Override
		public boolean equals(Object o){
			return o instanceof CullData other&&Arrays.equals(culledFaces,other.culledFaces);
		}
		@Override
		public int hashCode(){
			return Arrays.hashCode(culledFaces);
		}
	}
}