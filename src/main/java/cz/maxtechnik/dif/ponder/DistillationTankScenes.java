package cz.maxtechnik.dif.ponder;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.ponder.util.Type;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

import static cz.maxtechnik.dif.ponder.util.PonderScene.*;
public class DistillationTankScenes{
	public static void intro(SceneBuilder sceneBuilder,SceneBuildingUtil util){
		CreateSceneBuilder scene=new CreateSceneBuilder(sceneBuilder);
		scene.title("distillation_tank_intro","The Distillation Tank");
		setupScene(9,scene);
		zoom(scene,0.6F);
		scene.idle(10);
		for(int i=1;i<8;i++){
			reveal(scene,util.select().fromTo(3,i,3,4,i,4),Direction.DOWN);
			scene.idle(3);
		}
		scene.idle(30);
		narrate(scene,"The distillation tank is a multi-block structure that can be built as 1x1, 2x2, or 3x3, its height is determined by the recipe.",util.vector().centerOf(3,4,3));
		scene.idle(10);
		Selection inputSelection=util.select().fromTo(0,1,3,2,2,8);
		reveal(scene,inputSelection,Direction.EAST);
		narrate(scene,"The input tank is located at the very bottom.",util.vector().centerOf(3,1,4));
		showClickWithItemAt(scene,util,new BlockPos(1,1,5),new ItemStack(DifModItems.CRUDE_OIL_BUCKET.get()),Pointing.DOWN,Type.RIGHT,5);
		narrate(scene,"For example, Crude Oil.",util.vector().centerOf(1,2,5));
		fillFluidTank(scene,new BlockPos(1,1,5),new FluidStack(DifModFluids.CRUDE_OIL.get(),16000));
		scene.idle(15);
		applyKineticSpeedAt(scene,util,util.select().position(1,1,4),32);
		applyKineticSpeedAt(scene,util,util.select().fromTo(0,1,4,0,1,8),-32);
		applyKineticSpeedAt(scene,util,util.select().position(1,0,8),16);
		scene.world().propagatePipeChange(new BlockPos(1,1,4));
		scene.idle(20);
		narrate(scene,"Don't forget to turn on the burners!",util.vector().centerOf(3,0,4));
		scene.idle(8);
		showClickWithItemAt(scene,util,new BlockPos(4,0,3),new ItemStack(Items.COAL),Pointing.RIGHT,Type.RIGHT,10);
		scene.idle(8);
		scene.world().modifyBlocks(util.select().fromTo(3,1,3,4,1,4),bs->bs.setValue(BlazeBurnerBlock.HEAT_LEVEL,BlazeBurnerBlock.HeatLevel.KINDLED),false);
		scene.idle(20);
		narrate(scene,"The Distillation Tank will now begin processing fluids.",util.vector().centerOf(3,4,3));
		showItem(scene,util,new BlockPos(4,2,3),new ItemStack(DifModItems.HEAVY_FUEL_OIL_BUCKET.get()),Pointing.RIGHT,20);
		showItem(scene,util,new BlockPos(3,3,4),new ItemStack(DifModItems.LUBRICATING_OIL_BUCKET.get()),Pointing.LEFT,20);
		showItem(scene,util,new BlockPos(4,4,3),new ItemStack(DifModItems.DIESEL_BUCKET.get()),Pointing.RIGHT,20);
		showItem(scene,util,new BlockPos(3,5,4),new ItemStack(DifModItems.GASOLINE_BUCKET.get()),Pointing.LEFT,20);
		showItem(scene,util,new BlockPos(4,6,3),new ItemStack(DifModItems.LPG_BUCKET.get()),Pointing.RIGHT,20);
		scene.idle(30);
		Selection outputSelection=util.select().fromTo(5,1,3,7,7,4);
		applyKineticSpeedAt(scene,util,util.select().position(5,2,3),32);
		applyKineticSpeedAt(scene,util,util.select().position(5,3,3),-32);
		applyKineticSpeedAt(scene,util,util.select().position(5,4,3),32);
		applyKineticSpeedAt(scene,util,util.select().position(5,5,3),-32);
		applyKineticSpeedAt(scene,util,util.select().position(5,6,3),32);
		applyKineticSpeedAt(scene,util,util.select().position(5,7,3),-32);
		applyKineticSpeedAt(scene,util,util.select().position(5,1,4),-16);
		scene.world().propagatePipeChange(new BlockPos(5,3,3));
		scene.world().propagatePipeChange(new BlockPos(5,4,3));
		scene.world().propagatePipeChange(new BlockPos(5,5,3));
		scene.world().propagatePipeChange(new BlockPos(5,6,3));
		scene.world().propagatePipeChange(new BlockPos(5,7,3));
		reveal(scene,outputSelection,Direction.WEST);
		scene.idle(15);
		fillFluidTank(scene,new BlockPos(7,3,3),new FluidStack(DifModFluids.HEAVY_FUEL_OIL.get(),8000));
		fillFluidTank(scene,new BlockPos(6,4,3),new FluidStack(DifModFluids.LUBRICATING_OIL.get(),8000));
		fillFluidTank(scene,new BlockPos(7,5,3),new FluidStack(DifModFluids.DIESEL.get(),8000));
		fillFluidTank(scene,new BlockPos(6,6,3),new FluidStack(DifModFluids.GASOLINE.get(),8000));
		fillFluidTank(scene,new BlockPos(7,7,3),new FluidStack(DifModFluids.LPG.get(),8000));
		scene.idle(10);
		scene.markAsFinished();
	}
}
