package cz.maxtechnik.dif.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.block.entity.ForgeGlassBlockEntity;
import cz.maxtechnik.dif.util.ForgeMultiblockHelper;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Renders the molten fluid column as seen through a single ForgeGlass block.
 *
 * Because this renderer is attached to ForgeGlassBlockEntity (not the
 * controller), it renders whenever this glass block is in view — the controller
 * does not need to be on-screen.
 *
 * Rendering logic (Tinkers' Construct style):
 *  - Accumulates each fluid's "height in blocks" proportional to its volume.
 *  - Clips the visible slice to [0, 1] in local Y (= this glass block's layer).
 *  - Uses Create/Catnip FLUID_RENDERER for correct textures and colouring.
 */
public class ForgeGlassBlockEntityRenderer implements BlockEntityRenderer<ForgeGlassBlockEntity> {

    private static final float PAD = 0.01f;

    public ForgeGlassBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ForgeGlassBlockEntity be, float partialTick,
                       PoseStack ps, MultiBufferSource buf,
                       int packedLight, int packedOverlay) {

        BlockPos ctrlPos = be.getControllerPos();
        if (ctrlPos == null) return;

        Level level = be.getLevel();
        if (level == null) return;

        if (!(level.getBlockEntity(ctrlPos) instanceof ForgeControllerBlockEntity ctrl)) return;

        BlockState ctrlState = ctrl.getBlockState();
        if (!ctrlState.hasProperty(ForgeFurnaceController.FORMED)
                || !ctrlState.getValue(ForgeFurnaceController.FORMED)) return;

        int glassLayers = ctrl.getGlassLayers();
        if (glassLayers == 0) return;

        int totalCapacity = ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
        if (totalCapacity <= 0) return;

        // Which layer is this glass block? (1 = first layer, directly above controller)
        int myLayer = be.getBlockPos().getY() - ctrlPos.getY();
        if (myLayer < 1 || myLayer > glassLayers) return;

        // Global Y range for this layer (in "block units" above controller):
        float layerFloor = myLayer - 1f; // e.g. layer 1 → [0, 1), layer 2 → [1, 2)
        float layerCeil  = myLayer;

        // ── Compute 3×3 interior bounds in this glass block's local coords ──
        // Controller facing → direction into the structure
        Direction facing    = ctrlState.getValue(ForgeFurnaceController.FACING);
        Direction intoStr   = facing.getOpposite();
        Direction right     = intoStr.getClockWise();

        BlockPos glassPos   = be.getBlockPos();
        int isX = intoStr.getStepX(), isZ = intoStr.getStepZ();
        int rX  = right.getStepX(),   rZ  = right.getStepZ();

        // The 3×3 spans ctrlPos + intoStr*(0..2), ctrlPos + right*(-1..+1)
        // Corner offsets from ctrlPos in world coords:
        int dx_nn = rX*(-1) + isX*0;  int dz_nn = rZ*(-1) + isZ*0;
        int dx_np = rX*(-1) + isX*2;  int dz_np = rZ*(-1) + isZ*2;
        int dx_pn = rX*(1)  + isX*0;  int dz_pn = rZ*(1)  + isZ*0;
        int dx_pp = rX*(1)  + isX*2;  int dz_pp = rZ*(1)  + isZ*2;

        // Convert to this glass block's local coords:
        int dx = ctrlPos.getX() - glassPos.getX();
        int dz = ctrlPos.getZ() - glassPos.getZ();

        float xMin = dx + Math.min(Math.min(dx_nn, dx_np), Math.min(dx_pn, dx_pp)) + PAD;
        float xMax = dx + Math.max(Math.max(dx_nn, dx_np), Math.max(dx_pn, dx_pp)) + 1 - PAD;
        float zMin = dz + Math.min(Math.min(dz_nn, dz_np), Math.min(dz_pn, dz_pp)) + PAD;
        float zMax = dz + Math.max(Math.max(dz_nn, dz_np), Math.max(dz_pn, dz_pp)) + 1 - PAD;

        int light = LevelRenderer.getLightColor(level, glassPos);

        // ── Render each fluid slice that intersects this layer ───────────────
        int[] renderOrder = ctrl.getFluidRenderOrder();
        float globalY = 0f; // cumulative height from bottom (in block units above controller base)

        for (int idx : renderOrder) {
            if (idx < 0 || idx >= ForgeControllerBlockEntity.FLUID_TANK_COUNT) continue;
            FluidTank tank = ctrl.fluidTanks[idx];
            if (tank == null || tank.isEmpty()) continue;

            float fluidHeight = ((float) tank.getFluidAmount() / totalCapacity) * glassLayers;
            if (fluidHeight <= 0) continue;

            float fluidBottom = globalY;
            float fluidTop    = globalY + fluidHeight;

            // Clip to this layer's [layerFloor, layerCeil] window:
            float drawBottom = Math.max(fluidBottom, layerFloor);
            float drawTop    = Math.min(fluidTop,    layerCeil);

            if (drawTop > drawBottom) {
                // Convert global-Y to local glass-block Y (0=bottom of this block, 1=top):
                float localYMin = (drawBottom - layerFloor) + PAD;
                float localYMax = (drawTop    - layerFloor) - PAD;

                if (localYMax > localYMin) {
                    FluidStack stack = tank.getFluid();
                    NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                            stack,
                            xMin, localYMin, zMin,
                            xMax, localYMax, zMax,
                            buf, ps, light, false, true
                    );
                }
            }

            globalY += fluidHeight;
            if (globalY >= glassLayers) break;
        }
    }

    // Each glass block covers only its own 1×1×1 volume — no extra culling needed.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ForgeGlassBlockEntity be) {
        return new net.minecraft.world.phys.AABB(be.getBlockPos());
    }

    @Override public int getViewDistance() { return 256; }
}
