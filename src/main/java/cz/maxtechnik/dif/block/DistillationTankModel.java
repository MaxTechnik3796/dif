package cz.maxtechnik.dif.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class DistillationTankModel extends CTModel {

	private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	public static DistillationTankModel standard(BakedModel originalModel) {
		return new DistillationTankModel(originalModel,
				DifModSpriteShifts.DISTILLATION_TANK,
				DifModSpriteShifts.DISTILLATION_TANK_TOP,
				DifModSpriteShifts.DISTILLATION_TANK_INNER);
	}

	private DistillationTankModel(BakedModel originalModel, CTSpriteShiftEntry side,
	                              CTSpriteShiftEntry top, CTSpriteShiftEntry inner) {
		super(originalModel, new DistillationTankCTBehaviour(side, top, inner));
	}

	@Override
	protected ModelData.Builder gatherModelData(Builder builder, BlockAndTintGetter world,
	                                            BlockPos pos, BlockState state, ModelData blockEntityData) {
		super.gatherModelData(builder, world, pos, state, blockEntityData);

		CullData cull = new CullData();
		for (Direction d : Iterate.directions) {
			BlockPos neighbor = pos.relative(d);
			boolean sameTank = world.getBlockState(neighbor).getBlock() instanceof DistillationTank;
			if (d.getAxis().isVertical()) {
				// Vršek/spodek schovej jen když je tam další tank (skládání věže)
				cull.set(d, sameTank);
			} else {
				// Horizontálně schovej vnitřní stěnu mezi spojenými bloky
				cull.set(d, sameTank && ConnectivityHandler.isConnected(world, pos, neighbor));
			}
		}
		return builder.with(CULL_PROPERTY, cull);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource rand,
	                                         ModelData extraData,RenderType renderType) {
		CullData cull = extraData.has(CULL_PROPERTY) ? extraData.get(CULL_PROPERTY) : null;

		// Pouze schovaný face vynech — všechno ostatní renderuj normálně
		if (side != null && cull != null && cull.isCulled(side))
			return Collections.emptyList();

		return super.getQuads(state, side, rand, extraData, renderType);
	}

	// Cull data se správným equals/hashCode — kvůli cachování model dat (řeší flicker i mizení)
	private static final class CullData {
		private final boolean[] culled = new boolean[6];

		void set(Direction d, boolean c) {
			culled[d.ordinal()] = c;
		}

		boolean isCulled(Direction d) {
			return culled[d.ordinal()];
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof CullData other && Arrays.equals(culled, other.culled);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(culled);
		}
	}
}