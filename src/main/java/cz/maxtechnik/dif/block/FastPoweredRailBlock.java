package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
public class FastPoweredRailBlock extends PoweredRailBlock {
	public FastPoweredRailBlock(Properties properties) {
		// Druhý parametr 'true' v konstruktoru říká, že jde o napájený (powered) rail
		super(properties, true);
	}

	// V 1.20.1 Forge používá tuto metodu pro určení maximální rychlosti na koleji
	@Override
	public float getRailMaxSpeed(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
		// Výchozí hodnota u běžných kolejí je 0.4f
		// 1.2f je trojnásobná rychlost.
		// POZOR: Pokud dáš víc než 1.5f, vozíky budou v zatáčkách často vypadávat!
		return 10F;
	}
	@Override
	public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
		double multiplier = 0.8;
		if (state.getValue(POWERED)) {
			Vec3 motion = cart.getDeltaMovement();
			double speed = motion.horizontalDistance();

			// Pokud se vozík hýbe, přidáme mu extra "push"
			if (speed > 0.01) {
				// Tady násobíme pohyb.
				// 1.1 = přidá 10% rychlosti při každém ticku na tomto railu.
				// Pozor: pokud je číslo moc vysoké, vozík okamžitě vystřelí a může lagovat.
				cart.setDeltaMovement(motion.add(motion.x / speed * multiplier, 0.0, motion.z / speed * multiplier));
			} else {
				// Pokud vozík stojí, ale je u bloku (třeba u zdi),
				// vanilla kód ho rozjede. Můžeš nechat super.onMinecartPass
				super.onMinecartPass(state, world, pos, cart);
			}
		}
	}
}