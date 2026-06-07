package cz.maxtechnik.dif.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.ForgeGlassBlock;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.util.ForgeMultiblockHelper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.joml.Matrix4f;

/**
 * Renders molten fluid layers inside the Forge Furnace glass tower.
 *
 * For each intact glass layer that contains fluid, a translucent coloured
 * block is drawn inside the full 3×3 column area.  The top layer is
 * partially filled proportionally to how much fluid it actually holds.
 */
public class ForgeGlassRenderer implements BlockEntityRenderer<ForgeControllerBlockEntity> {

    // Inner padding so the fluid quad sits just inside the glass faces
    private static final float PAD = 0.01f;

    public ForgeGlassRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ForgeControllerBlockEntity be, float partialTick,
                       PoseStack ps, MultiBufferSource buf,
                       int packedLight, int packedOverlay) {

        Level level = be.getLevel();
        if (level == null) return;

        BlockState ctrlState = be.getBlockState();
        if (!ctrlState.hasProperty(ForgeFurnaceController.FORMED)
                || !ctrlState.getValue(ForgeFurnaceController.FORMED)) return;

        int glassLayers = be.getGlassLayers();
        if (glassLayers == 0) return;

        // ── Collect fluid info ──────────────────────────────────────────────
        FluidStack dominant = FluidStack.EMPTY;
        int totalAmount = 0;

        for (int i = 0; i < ForgeControllerBlockEntity.FLUID_TANK_COUNT; i++) {
            FluidTank tank = be.fluidTanks[i];
            if (!tank.isEmpty()) {
                totalAmount += tank.getFluidAmount();
                if (dominant.isEmpty()) dominant = tank.getFluid();
            }
        }

        if (totalAmount == 0 || dominant.isEmpty()) return;

        // Total capacity = layers × MB_PER_GLASS_LAYER
        int totalCapacity = ForgeMultiblockHelper.totalFluidCapacity(glassLayers);

        // ── Direction setup ─────────────────────────────────────────────────
        Direction facing = ctrlState.getValue(ForgeFurnaceController.FACING);
        Direction intoStructure = facing.getOpposite();
        Direction right = intoStructure.getClockWise();

        BlockPos ctrlPos = be.getBlockPos();

        // ── Fluid colour + alpha ────────────────────────────────────────────
        int tint = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
                .of(dominant.getFluid()).getTintColor();
        float r = ((tint >> 16) & 0xFF) / 255f;
        float g = ((tint >>  8) & 0xFF) / 255f;
        float b = ((tint      ) & 0xFF) / 255f;
        float a = 0.82f;

        VertexConsumer vc = buf.getBuffer(RenderType.translucent());

        // ── Per-layer rendering ─────────────────────────────────────────────
        int mbPerLayer = ForgeMultiblockHelper.MB_PER_GLASS_LAYER;

        for (int layer = 1; layer <= glassLayers; layer++) {

            // Check every block in this layer: all 9 must be ForgeGlass
            boolean intact = true;
            outer:
            for (int z = 0; z < 3; z++) {
                for (int x = -1; x <= 1; x++) {
                    BlockPos gp = ctrlPos
                            .relative(intoStructure, z)
                            .relative(right, x)
                            .above(layer);
                    if (!(level.getBlockState(gp).getBlock() instanceof ForgeGlassBlock)) {
                        intact = false;
                        break outer;
                    }
                }
            }
            if (!intact) continue;

            // How much fluid is in this layer?
            int below = (layer - 1) * mbPerLayer;   // mB in layers below
            if (totalAmount <= below) continue;       // this layer empty

            int inThisLayer = Math.min(mbPerLayer, totalAmount - below);
            float fill = (float) inThisLayer / mbPerLayer; // 0..1

            // ── Build pose for this layer ─────────────────────────────────
            // The 3×3 glass area:
            //   z offset 0..2 along intoStructure, x offset -1..1 along right
            //   y offset = layer (glass is 'layer' blocks above controller)
            //
            // We render the ENTIRE 3×3 × fill-height column in one go.
            // Relative to the controller block (pose already translated there):
            //   X: 0 to 3 along intoStructure
            //   Z: -1 to 2 along right (width 3)
            //   Y: layer  to layer+fill

            // Convert intoStructure / right to XZ axis contributions
            int isX = intoStructure.getStepX(), isZ = intoStructure.getStepZ();
            int rX  = right.getStepX(),         rZ  = right.getStepZ();

            // World corners of the 3×3 slab, expressed as offsets from ctrlPos
            // into-structure goes 0..2, right goes -1..1
            // Min/max per axis:
            float xMin = Math.min(isX * 0 + rX * (-1), isX * 2 + rX * 1) + PAD;
            float xMax = Math.max(isX * 0 + rX * (-1), isX * 2 + rX * 1) + 1 - PAD;
            float zMin = Math.min(isZ * 0 + rZ * (-1), isZ * 2 + rZ * 1) + PAD;
            float zMax = Math.max(isZ * 0 + rZ * (-1), isZ * 2 + rZ * 1) + 1 - PAD;
            float yMin = layer + PAD;
            float yMax = layer + fill - PAD;

            if (yMax <= yMin) continue;

            // Lighting — sample at the centre of the column
            BlockPos lightPos = ctrlPos.above(layer);
            int light = LevelRenderer.getLightColor(level, lightPos);

            ps.pushPose();
            Matrix4f mat = ps.last().pose();
            renderBox(vc, mat, xMin, yMin, zMin, xMax, yMax, zMax, r, g, b, a, light);
            ps.popPose();
        }
    }

    /** Renders a solid-colour axis-aligned box (6 quads, backface per side). */
    private static void renderBox(VertexConsumer vc, Matrix4f mat,
                                  float x0, float y0, float z0,
                                  float x1, float y1, float z1,
                                  float r, float g, float b, float a,
                                  int light) {
        int lu = light & 0xFFFF, lv = (light >> 16) & 0xFFFF;

        // Bottom (-Y)
        vc.addVertex(mat, x0, y0, z0).setColor(r,g,b,a).setUv(0,0).setUv2(lu,lv).setNormal(0,-1,0);
        vc.addVertex(mat, x1, y0, z0).setColor(r,g,b,a).setUv(1,0).setUv2(lu,lv).setNormal(0,-1,0);
        vc.addVertex(mat, x1, y0, z1).setColor(r,g,b,a).setUv(1,1).setUv2(lu,lv).setNormal(0,-1,0);
        vc.addVertex(mat, x0, y0, z1).setColor(r,g,b,a).setUv(0,1).setUv2(lu,lv).setNormal(0,-1,0);

        // Top (+Y)
        vc.addVertex(mat, x0, y1, z1).setColor(r,g,b,a).setUv(0,0).setUv2(lu,lv).setNormal(0,1,0);
        vc.addVertex(mat, x1, y1, z1).setColor(r,g,b,a).setUv(1,0).setUv2(lu,lv).setNormal(0,1,0);
        vc.addVertex(mat, x1, y1, z0).setColor(r,g,b,a).setUv(1,1).setUv2(lu,lv).setNormal(0,1,0);
        vc.addVertex(mat, x0, y1, z0).setColor(r,g,b,a).setUv(0,1).setUv2(lu,lv).setNormal(0,1,0);

        // North (-Z)
        vc.addVertex(mat, x0, y1, z0).setColor(r,g,b,a).setUv(0,0).setUv2(lu,lv).setNormal(0,0,-1);
        vc.addVertex(mat, x1, y1, z0).setColor(r,g,b,a).setUv(1,0).setUv2(lu,lv).setNormal(0,0,-1);
        vc.addVertex(mat, x1, y0, z0).setColor(r,g,b,a).setUv(1,1).setUv2(lu,lv).setNormal(0,0,-1);
        vc.addVertex(mat, x0, y0, z0).setColor(r,g,b,a).setUv(0,1).setUv2(lu,lv).setNormal(0,0,-1);

        // South (+Z)
        vc.addVertex(mat, x0, y0, z1).setColor(r,g,b,a).setUv(0,0).setUv2(lu,lv).setNormal(0,0,1);
        vc.addVertex(mat, x1, y0, z1).setColor(r,g,b,a).setUv(1,0).setUv2(lu,lv).setNormal(0,0,1);
        vc.addVertex(mat, x1, y1, z1).setColor(r,g,b,a).setUv(1,1).setUv2(lu,lv).setNormal(0,0,1);
        vc.addVertex(mat, x0, y1, z1).setColor(r,g,b,a).setUv(0,1).setUv2(lu,lv).setNormal(0,0,1);

        // West (-X)
        vc.addVertex(mat, x0, y0, z0).setColor(r,g,b,a).setUv(0,0).setUv2(lu,lv).setNormal(-1,0,0);
        vc.addVertex(mat, x0, y0, z1).setColor(r,g,b,a).setUv(1,0).setUv2(lu,lv).setNormal(-1,0,0);
        vc.addVertex(mat, x0, y1, z1).setColor(r,g,b,a).setUv(1,1).setUv2(lu,lv).setNormal(-1,0,0);
        vc.addVertex(mat, x0, y1, z0).setColor(r,g,b,a).setUv(0,1).setUv2(lu,lv).setNormal(-1,0,0);

        // East (+X)
        vc.addVertex(mat, x1, y0, z1).setColor(r,g,b,a).setUv(0,0).setUv2(lu,lv).setNormal(1,0,0);
        vc.addVertex(mat, x1, y0, z0).setColor(r,g,b,a).setUv(1,0).setUv2(lu,lv).setNormal(1,0,0);
        vc.addVertex(mat, x1, y1, z0).setColor(r,g,b,a).setUv(1,1).setUv2(lu,lv).setNormal(1,0,0);
        vc.addVertex(mat, x1, y1, z1).setColor(r,g,b,a).setUv(0,1).setUv2(lu,lv).setNormal(1,0,0);
    }

    /** BERs may render as far above the block as the glass tower goes. */
    @Override
    public int getViewDistance() { return 256; }

    @Override
    public boolean shouldRenderOffScreen(ForgeControllerBlockEntity be) { return true; }
}
