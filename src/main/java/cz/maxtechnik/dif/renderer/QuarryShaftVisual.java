package cz.maxtechnik.dif.renderer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.core.Direction;

import java.util.function.Consumer;
/**
 * Custom visual pro Quarry - renderuje jen dolní půlku hřídele a čte světlo ze spodního bloku.
 */
public class QuarryShaftVisual extends KineticBlockEntityVisual<QuarryBlockEntity> implements SimpleTickableVisual{
	protected final RotatingInstance rotatingModel;
	public QuarryShaftVisual(VisualizationContext context,QuarryBlockEntity blockEntity,float partialTick){
		super(context,blockEntity,partialTick);
		rotatingModel=instancerProvider()
				.instancer(AllInstanceTypes.ROTATING,Models.partial(AllPartialModels.SHAFT_HALF))
				.createInstance()
				.rotateToFace(Direction.SOUTH,Direction.DOWN)
				.setup(blockEntity)
				.setPosition(getVisualPosition());
		rotatingModel.setChanged();
	}
	public static SimpleBlockEntityVisualizer.Factory<QuarryBlockEntity> factory(){
		return QuarryShaftVisual::new;
	}
	@Override
	public void update(float pt){
		rotatingModel.setup(blockEntity).setChanged();
	}
	@Override
	public void tick(Context context){
		KineticBlockEntityVisual.applyOverstressEffect(blockEntity,rotatingModel);
	}
	@Override
	public void updateLight(float partialTick){
		// Čteme světlo z bloku pod Quarry, protože Quarry samotná je neprůhledná a má světlo 0
		relight(pos.below(),rotatingModel);
	}
	@Override
	protected void _delete(){
		rotatingModel.delete();
	}
	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer){
		consumer.accept(rotatingModel);
	}
}
