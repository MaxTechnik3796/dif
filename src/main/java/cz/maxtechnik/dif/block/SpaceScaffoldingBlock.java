package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.TicketType;
import org.jetbrains.annotations.NotNull;

public class SpaceScaffoldingBlock extends Block {

	public SpaceScaffoldingBlock(Properties properties) {
		super(properties);
	}

	// Chyba "Method does not override": v novějších verzích se používá onPlace
	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		if (!world.isClientSide() && world instanceof ServerLevel serverLevel) {
			// 1. Udržení chunku (Ticket na 16 sekund)
			serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 2, pos);

			// 2. Naplánování ticku (Opravený název metody pro Forge/Minecraft)
			// Metoda se jmenuje scheduleTick, ale musíme předat "this"
			world.scheduleTick(pos, this, 320);
		}
	}

	// Chyba "Method does not override": Metoda pro tick se v Block třídě
	// jmenuje přesně 'tick', ale musí mít správné parametry
	@Override
	public void tick(BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random) {
		super.tick(state, world, pos, random);

		// 3. Smazání bloku (proměna na vzduch)
		world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

		// 4. Odstranění ticketu pro chunk
		world.getChunkSource().removeRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 2, pos);
	}
}