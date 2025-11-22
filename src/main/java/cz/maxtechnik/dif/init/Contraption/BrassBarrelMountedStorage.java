package cz.maxtechnik.dif.init.Contraption;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;

public class BrassBarrelMountedStorage extends SimpleMountedStorage {

    public BrassBarrelMountedStorage(IItemHandlerModifiable handler) {
        super(AllMountedStorageTypes.BRASS_BARREL.get(), handler);
    }

    // Tady říkáme, že chceme interakci (otevírání GUI)
    @Override
    public boolean canInteract() {
        return true;
    }

    // Zvuk otevření (jako u barrelu/chestu)
    @Override
    protected void playOpeningSound(ServerLevel level, Vec3 pos) {
        level.playSound(null, BlockPos.containing(pos), SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.2F);
    }

    @Override
    protected void playClosingSound(ServerLevel level, Vec3 pos) {
        level.playSound(null, BlockPos.containing(pos), SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.2F);
    }

    // Volitelné – pokud chceš animaci otevírání (jako u barrelu)
    @Override
    public void onInteract(Contraption contraption, BlockPos localPos, BlockState state, BlockEntity be, ServerLevel level) {
        super.onInteract(contraption, localPos, state, be, level);
        // Tady můžeš poslat packet na animaci otevření (pokud máš model s víkem)
        // Např. Create posílá packet pro chesty
    }
}