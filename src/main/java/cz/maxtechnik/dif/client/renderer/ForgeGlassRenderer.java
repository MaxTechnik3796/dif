package cz.maxtechnik.dif.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.ForgeGlassBlock;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
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
 * Renders molten fluid inside the Forge Furnace glass tower.
 *
 * Like Tinkers' Construct smeltery:
 *  - Each fluid in the 4 controller tanks is rendered on top of each other.
 *  - Their heights are proportional to their fluid amount compared to total capacity.
 *  - Rendering stops at the first glass layer that is missing any block.
 *  - Uses Create's/Catnip's FLUID_RENDERER to correctly render still/flowing fluid textures and colors.
 */
public class ForgeGlassRenderer implements BlockEntityRenderer<ForgeControllerBlockEntity> {

    private static final float PAD = 0.01f;

    public ForgeGlassRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ForgeControllerBlockEntity be, float partialTick,
                       PoseStack ps, MultiBufferSource buf,
                       int packedLight, int packedOverlay) {

        Level level = be.getLevel();
        if (level == null) return;

        BlockState state = be.getBlockState();
        if (!state.hasProperty(ForgeFurnaceController.FORMED)
                || !state.getValue(ForgeFurnaceController.FORMED)) return;

        int glassLayers = be.getGlassLayers();
        if (glassLayers == 0) return;

        int totalCapacity = ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
        if (totalCapacity <= 0) return;

        // ── Direction ────────────────────────────────────────────────────────
        Direction facing       = state.getValue(ForgeFurnaceController.FACING);
        Direction intoStr      = facing.getOpposite();
        Direction right        = intoStr.getClockWise();
        BlockPos  ctrlPos      = be.getBlockPos();

        // ── Find highest contiguous intact layer ─────────────────────────────
        // Rendering stops at the first layer with ANY missing glass block.
        int intactLayers = 0;
        for (int layer = 1; layer <= glassLayers; layer++) {
            if (!isLayerIntact(level, ctrlPos, intoStr, right, layer)) break;
            intactLayers = layer;
        }
        if (intactLayers == 0) return;

        // ── Compute 3×3 bounding box in local (controller-relative) coords ───
        int isX = intoStr.getStepX(), isZ = intoStr.getStepZ();
        int rX  = right .getStepX(), rZ  = right .getStepZ();

        // Origin offset of each corner block:
        int dx_nn = rX*(-1) + isX*0;  int dz_nn = rZ*(-1) + isZ*0;
        int dx_np = rX*(-1) + isX*2;  int dz_np = rZ*(-1) + isZ*2;
        int dx_pn = rX*(1)  + isX*0;  int dz_pn = rZ*(1)  + isZ*0;
        int dx_pp = rX*(1)  + isX*2;  int dz_pp = rZ*(1)  + isZ*2;

        float xMin = Math.min(Math.min(dx_nn, dx_np), Math.min(dx_pn, dx_pp)) + PAD;
        float xMax = Math.max(Math.max(dx_nn, dx_np), Math.max(dx_pn, dx_pp)) + 1 - PAD;
        float zMin = Math.min(Math.min(dz_nn, dz_np), Math.min(dz_pn, dz_pp)) + PAD;
        float zMax = Math.max(Math.max(dz_nn, dz_np), Math.max(dz_pn, dz_pp)) + 1 - PAD;

        // ── Render each fluid in order, stacking them ────────────────────────
        int[] renderOrder = be.getFluidRenderOrder();
        float currentYOffset = 1.0f + PAD; // Starts at bottom of first glass layer (1 block above controller)
        float maxAllowedY = 1.0f + intactLayers - PAD;

        int light = LevelRenderer.getLightColor(level, ctrlPos.above());

        for (int idx : renderOrder) {
            if (idx < 0 || idx >= ForgeControllerBlockEntity.FLUID_TANK_COUNT) continue;
            FluidTank tank = be.fluidTanks[idx];
            if (tank == null || tank.isEmpty() || tank.getFluidAmount() <= 0) continue;

            FluidStack stack = tank.getFluid();
            // Height proportional to total capacity across the total glass layers height
            float fluidHeight = ((float) tank.getFluidAmount() / totalCapacity) * glassLayers;
            if (fluidHeight <= 0) continue;

            float yMin = currentYOffset;
            float yMax = currentYOffset + fluidHeight;

            // Cap the render heights to the intact glass layers
            float drawYMin = Math.min(yMin, maxAllowedY);
            float drawYMax = Math.min(yMax, maxAllowedY);

            if (drawYMax > drawYMin) {
                // Render the fluid box using Create's/Catnip's FluidRenderer service
                NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                        stack,
                        xMin, drawYMin, zMin,
                        xMax, drawYMax, zMax,
                        buf,
                        ps,
                        light,
                        false,
                        true
                );
            }

            // Accumulate height (even if capped, so subsequent fluids stack on top / are out of bounds)
            currentYOffset += fluidHeight;
            if (currentYOffset >= maxAllowedY) {
                break; // Filled/exceeded the intact glass layers
            }
        }
    }

    /** Returns true if every glass block in the given layer is present. */
    private static boolean isLayerIntact(Level level, BlockPos ctrlPos,
                                         Direction intoStr, Direction right, int layer) {
        for (int z = 0; z < 3; z++) {
            for (int x = -1; x <= 1; x++) {
                BlockPos gp = ctrlPos.relative(intoStr, z).relative(right, x).above(layer);
                if (!(level.getBlockState(gp).getBlock() instanceof ForgeGlassBlock)) return false;
            }
        }
        return true;
    }

    @Override public int getViewDistance() { return 256; }
    @Override public boolean shouldRenderOffScreen(ForgeControllerBlockEntity be) { return true; }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ForgeControllerBlockEntity be) {
        net.minecraft.world.phys.AABB base = new net.minecraft.world.phys.AABB(be.getBlockPos());
        int layers = Math.max(1, be.getGlassLayers());
        return base.inflate(3, 0, 3)
                   .expandTowards(0, layers + 1, 0)
                   .expandTowards(0, -1, 0);
    }
}
