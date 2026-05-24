package cz.maxtechnik.dif.block.generator.steam_generator;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class SteamGeneratorBlock extends KineticBlock implements EntityBlock{
	// Vlastnost pro natočení hřídele (osa X, Y nebo Z)
	public static final EnumProperty<Direction.Axis> AXIS=BlockStateProperties.AXIS;
	public SteamGeneratorBlock(Properties properties){
		super(properties);
		// Výchozí osa bude svislá Y
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS,Direction.Axis.Y));
	}
	// Vytvoří mozek bloku (BlockEntity)
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new SteamGeneratorBlockEntity(pos,state);
	}
	// Řekne Create modu, v jaké ose se blok točí
	@Override
	public Direction.Axis getRotationAxis(BlockState state){
		return state.getValue(AXIS);
	}
	// Povolí připojení hřídelí pouze ze stran, kam směřuje osa
	@Override
	public boolean hasShaftTowards(LevelReader world,BlockPos pos,BlockState state,Direction face){
		return face.getAxis()==getRotationAxis(state);
	}
	// Natočí blok podle toho, na jakou stranu bloku hráč kliknul při položení
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(AXIS,context.getClickedFace().getAxis());
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(AXIS);
	}
}
