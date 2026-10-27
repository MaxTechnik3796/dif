package cz.maxtechnik.dif.init.event.client;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.model.ModelSwapper;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.*;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ConnectedTexturesHandler{
	@SubscribeEvent
	public static void onModelBake(ModelEvent.ModifyBakingResult event){
		register(event);
		ModelSwapper.swapModels(event.getModels(),ModelSwapper.getAllBlockStateModelLocations(DifModBlocks.DISTILLATION_TANK.get()),
				model->new TankModel(model,
						DifModSpriteShifts.DISTILLATION_TANK,
						DifModSpriteShifts.DISTILLATION_TANK_TOP,
						DifModSpriteShifts.DISTILLATION_TANK_INNER
				));
		ModelSwapper.swapModels(event.getModels(),ModelSwapper.getAllBlockStateModelLocations(DifModBlocks.MITHRIL_FLUID_TANK.get()),
				model->new TankModel(model,
						DifModSpriteShifts.MITHRIL_FLUID_TANK,
						DifModSpriteShifts.MITHRIL_FLUID_TANK_TOP,
						DifModSpriteShifts.MITHRIL_FLUID_TANK_INNER
				));
	}
	private static void register(ModelEvent.ModifyBakingResult event){
		ModelSwapper.swapModels(event.getModels(),ModelSwapper.getAllBlockStateModelLocations(DifModBlocks.ZINC_CASING.get()),model->new CTModel(model,new SimpleCTBehaviour(DifModSpriteShifts.ZINC_CASING)));
	}
	private static class TankModel extends CTModel{
		private static final ModelProperty<CullData> CULL_PROPERTY=new ModelProperty<>();
		public TankModel(BakedModel originalModel,CTSpriteShiftEntry side,CTSpriteShiftEntry top,CTSpriteShiftEntry inner){
			super(originalModel,new FluidTankCTBehaviour(side,top,inner));
		}
		@Override
		protected ModelData.@NotNull Builder gatherModelData(ModelData.@NotNull Builder builder,@NotNull BlockAndTintGetter world,@NotNull BlockPos pos,@NotNull BlockState state,@NotNull ModelData blockEntityData){
			super.gatherModelData(builder,world,pos,state,blockEntityData);
			CullData cullData=new CullData();
			for(Direction d: Iterate.horizontalDirections){
				cullData.setCulled(d,ConnectivityHandler.isConnected(world,pos,pos.relative(d)));
			}
			return builder.with(CULL_PROPERTY,cullData);
		}
		@Override
		public @NotNull List<BakedQuad> getQuads(BlockState state,Direction side,@NotNull RandomSource rand,@NotNull ModelData extraData,RenderType renderType){
			if(side!=null) return Collections.emptyList();
			List<BakedQuad> quads=new ArrayList<>();
			for(Direction d: Iterate.directions){
				if(extraData.has(CULL_PROPERTY)&&Objects.requireNonNull(extraData.get(CULL_PROPERTY)).isCulled(d)) continue;
				quads.addAll(super.getQuads(state,d,rand,extraData,renderType));
			}
			quads.addAll(super.getQuads(state,null,rand,extraData,renderType));
			return quads;
		}
		private static class CullData{
			private final boolean[] culledFaces=new boolean[4];
			public CullData(){
				Arrays.fill(culledFaces,false);
			}
			void setCulled(Direction face,boolean cull){
				if(face.getAxis().isVertical()) return;
				culledFaces[face.get2DDataValue()]=cull;
			}
			boolean isCulled(Direction face){
				if(face.getAxis().isVertical()) return false;
				return culledFaces[face.get2DDataValue()];
			}
		}
	}
}