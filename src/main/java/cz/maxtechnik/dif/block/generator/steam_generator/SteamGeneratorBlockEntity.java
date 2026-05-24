package cz.maxtechnik.dif.block.generator.steam_generator;

import cz.maxtechnik.dif.block.generator.AbstractFluidGeneratorBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Steam Generator BlockEntity.
 *
 * Veškerá logika (tick, spotřeba kapaliny, kinetika, tooltip) je v
 * {@link AbstractFluidGeneratorBlockEntity}.
 *
 * Pokud chceš přidat extra chování (ohřívání, zvuk atd.), přepiš
 * {@link #tick()} nebo {@link #canRun()} zde.
 */
public class SteamGeneratorBlockEntity extends AbstractFluidGeneratorBlockEntity {

    public SteamGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.STEAM_GENERATOR.get(), pos, state);
    }

    // ── Registrace capability ─────────────────────────────────────────────────

    /**
     * Zaregistruje fluid-handler capability.
     * Přidej volání do {@code DifModEvents} nebo event subscriberu.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        AbstractFluidGeneratorBlockEntity.registerCap(
                event,
                DifModBlockEntities.STEAM_GENERATOR.get(),
                SteamGeneratorDefinition.INSTANCE
        );
    }

    // Příklad přidání extra podmínky:
    // @Override
    // protected boolean canRun() {
    //     return super.canRun() && nejakaExtraPodminka();
    // }
}
