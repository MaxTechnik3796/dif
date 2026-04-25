package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.world.entity.boss.wither.WitherBoss;
public class WitherTitanRenderer extends WitherBossRenderer{
	public WitherTitanRenderer(EntityRendererProvider.Context context){
		super(context);
		this.shadowRadius=0.0F;
	}
	@Override
	protected void scale(WitherBoss entity,PoseStack poseStack,float partialTickTime){
		if(entity.tickCount>0){
			poseStack.scale(10.0F,10.0F,10.0F);
			// Žádný translate! Model stojí tam, kde je Collision Box (nohy).
		}
	}
}