package cz.maxtechnik.dif.init.Contraption;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class BrassBarrelMountedStorageType extends SimpleMountedStorageType<BrassBarrelMountedStorage> {

    public static final BrassBarrelMountedStorageType INSTANCE = new BrassBarrelMountedStorageType();

    // Codec pro NBT
    public static final Codec<BrassBarrelMountedStorage> CODEC = SimpleMountedStorage.codec(BrassBarrelMountedStorage::new);

    private BrassBarrelMountedStorageType() {
        super(CODEC);
    }

    @Override
    protected IItemHandler getHandler(BlockEntity be) {
        // Tady vracíme capability tvého barrelu
        return be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }

    @Override
    protected SimpleMountedStorage createStorage(IItemHandler handler) {
        return new BrassBarrelMountedStorage(handler);
    }
}