package cz.maxtechnik.dif.ponder;

import cz.maxtechnik.dif.block.MegaTorch;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;

import static cz.maxtechnik.dif.ponder.util.PonderScene.*;
public class MegaTorchScenes{
	public static void intro(SceneBuilder scene,SceneBuildingUtil util){
		scene.title("mega_torch_intro","MEGA Torch");
		setupScene(5,scene);
		zoom(scene,0.9F);
		scene.idle(10);
		Selection torchSelection=util.select().fromTo(2,1,2,2,5,2);
		torchSelection.forEach(pos->{
			reveal(scene,util.select().position(pos),Direction.DOWN);
			scene.idle(2);
		});
		scene.idle(3);
		scene.world().modifyBlocks(torchSelection,bs->bs.setValue(MegaTorch.FORMED,true),false);
		scene.idle(10);
		narrate(scene,"The MEGA Torch is a torch that is 5 blocks high.",util.vector().centerOf(2,3,2));
		narrate(scene,"Blocks spawning of hostile mobs in a large area.",util.vector().centerOf(2,3,2));
		scene.idle(10);
		scene.markAsFinished();
	}
}
