package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class WitherTitanRenderer extends WitherBossRenderer {
    public WitherTitanRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 5.0F;
    }

    @Override
    protected void scale(WitherBoss entity, PoseStack poseStack, float partialTickTime) {
        if (entity.tickCount > 0) {
            float scale = 100.0F; // 10x větší
            poseStack.scale(scale, scale, scale);
        } else {
            poseStack.scale(1.0F, 1.0F, 1.0F);
        }
    }
}