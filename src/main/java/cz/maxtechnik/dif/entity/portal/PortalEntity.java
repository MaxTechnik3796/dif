package cz.maxtechnik.dif.entity.portal;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PortalEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_IS_BLUE = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_LINKED = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_FACING = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_UP_DIR = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);

    public long lastTeleportTime = 0;
    private int linkedCheckTimer = 0;
    private static final int LINKED_CHECK_INTERVAL = 40;
    private static final Map<UUID, Long> waitingPlayers = new HashMap<>();
    private final Map<UUID, Long> entityCooldowns = new HashMap<>();

    public PortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public PortalEntity(Level level, UUID owner, boolean isBlue, Direction facing, Direction upDir, Vec3 position) {
        super(DifModEntities.PORTAL.get(), level);
        this.setOwner(owner);
        this.setIsBlue(isBlue);
        this.setFacing(facing);
        this.setUpDir(upDir);
        this.setPos(position);
        this.setBoundingBox(this.buildPortalAABB());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_IS_BLUE, true);
        builder.define(DATA_IS_LINKED, false);
        builder.define(DATA_FACING, Direction.NORTH.getName());
        builder.define(DATA_UP_DIR, Direction.UP.getName());
    }

    public UUID getOwner() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    public void setOwner(UUID owner) {
        this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
    }

    public boolean isBlue() {
        return this.entityData.get(DATA_IS_BLUE);
    }

    public void setIsBlue(boolean blue) {
        this.entityData.set(DATA_IS_BLUE, blue);
    }

    public boolean isLinked() {
        return this.entityData.get(DATA_IS_LINKED);
    }

    public void setIsLinked(boolean linked) {
        this.entityData.set(DATA_IS_LINKED, linked);
    }

    public Direction getFacing() {
        return Direction.byName(this.entityData.get(DATA_FACING));
    }

    public void setFacing(Direction facing) {
        this.entityData.set(DATA_FACING, facing != null ? facing.getName() : Direction.NORTH.getName());
    }

    public Direction getUpDir() {
        return Direction.byName(this.entityData.get(DATA_UP_DIR));
    }

    public void setUpDir(Direction upDir) {
        this.entityData.set(DATA_UP_DIR, upDir != null ? upDir.getName() : Direction.UP.getName());
    }

    public AABB buildPortalAABB() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        Direction facing = this.getFacing();
        Direction up = this.getUpDir();

        if (facing.getAxis() == Direction.Axis.Y) {
            double minY = y - 0.03125;
            double maxY = y + 0.03125;
            double minX, maxX, minZ, maxZ;
            if (up.getAxis() == Direction.Axis.Z) {
                minX = x - 0.5;
                maxX = x + 0.5;
                minZ = z - 1.0;
                maxZ = z + 1.0;
            } else {
                minX = x - 1.0;
                maxX = x + 1.0;
                minZ = z - 0.5;
                maxZ = z + 0.5;
            }
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        } else {
            double minY = y - 1.0;
            double maxY = y + 1.0;
            double minX, maxX, minZ, maxZ;
            if (facing.getAxis() == Direction.Axis.X) {
                minX = x - 0.03125;
                maxX = x + 0.03125;
                minZ = z - 0.5;
                maxZ = z + 0.5;
            } else {
                minZ = z - 0.03125;
                maxZ = z + 0.03125;
                minX = x - 0.5;
                maxX = x + 0.5;
            }
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return this.buildPortalAABB();
    }

    public static java.util.Set<BlockPos> getPortalSupportBlocks(Vec3 pos, Direction upDir, Direction facing) {
        Vec3 extVec = Vec3.atLowerCornerOf(upDir.getNormal());
        Vec3 rightVec = Vec3.atLowerCornerOf(facing.getNormal().cross(upDir.getNormal()));
        
        java.util.Set<BlockPos> uniquePositions = new java.util.HashSet<>();
        uniquePositions.add(BlockPos.containing(pos.add(extVec.scale(0.5)).add(rightVec.scale(0.25))));
        uniquePositions.add(BlockPos.containing(pos.add(extVec.scale(0.5)).subtract(rightVec.scale(0.25))));
        uniquePositions.add(BlockPos.containing(pos.subtract(extVec.scale(0.5)).add(rightVec.scale(0.25))));
        uniquePositions.add(BlockPos.containing(pos.subtract(extVec.scale(0.5)).subtract(rightVec.scale(0.25))));
        return uniquePositions;
    }

    @Override
    public void tick() {
        super.tick();

        this.setBoundingBox(this.buildPortalAABB());

        if (this.level().isClientSide()) {
            return;
        }

        if (this.getOwner() == null) {
            this.discard();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.tickCount % 5 == 0) {
            Direction facing = this.getFacing();
            Direction up = this.getUpDir();
            
            java.util.Set<BlockPos> uniquePositions = getPortalSupportBlocks(this.position(), up, facing);

            boolean hasSupport = true;
            for (BlockPos p : uniquePositions) {
                BlockPos supPos = p.relative(facing.getOpposite());
                if (!serverLevel.getBlockState(supPos).isFaceSturdy(serverLevel, supPos, facing)) {
                    hasSupport = false;
                    break;
                }
            }
            
            if (!hasSupport) {
                PortalData.get(serverLevel).remove(this.getOwner(), this.isBlue());
                this.discard();
                return;
            }
        }

        if (!this.isLinked()) return;

        AABB box = this.getBoundingBox().inflate(0.1);
        long now = serverLevel.getGameTime();

        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, box);
        for (Player p : players) {
            UUID pid = p.getUUID();
            if (this.entityCooldowns.containsKey(pid) && now - this.entityCooldowns.get(pid) <= 15)
                continue;
            this.tryTeleportPlayer(p, serverLevel, now);
        }

        if (DifModCommonConfig.PORTAL_ALLOW_ENTITIES.get()) {
            List<LivingEntity> mobs = serverLevel.getEntitiesOfClass(LivingEntity.class, box);
            int count = 0;
            for (LivingEntity mob : mobs) {
                if (mob instanceof Player) continue;
                if (count >= DifModCommonConfig.PORTAL_MAX_ENTITIES_PER_TICK.get()) break;
                UUID mid = mob.getUUID();
                if (this.entityCooldowns.containsKey(mid) && now - this.entityCooldowns.get(mid) <= 15)
                    continue;
                this.tryTeleportEntity(mob, serverLevel, now, false);
                count++;
            }

            List<Entity> misc = serverLevel.getEntitiesOfClass(Entity.class, box, e -> !(e instanceof LivingEntity) && !(e instanceof Projectile) && !(e instanceof ItemEntity));
            for (Entity e : misc) {
                if (e instanceof PortalEntity) continue;
                if (count >= DifModCommonConfig.PORTAL_MAX_ENTITIES_PER_TICK.get()) break;
                UUID eid = e.getUUID();
                if (this.entityCooldowns.containsKey(eid) && now - this.entityCooldowns.get(eid) <= 15)
                    continue;
                this.tryTeleportEntity(e, serverLevel, now, false);
                count++;
            }
        }

        if (DifModCommonConfig.PORTAL_ALLOW_ITEMS.get()) {
            List<ItemEntity> items = serverLevel.getEntitiesOfClass(ItemEntity.class, box);
            int count = 0;
            for (ItemEntity e : items) {
                if (count >= DifModCommonConfig.PORTAL_MAX_ENTITIES_PER_TICK.get()) break;
                UUID eid = e.getUUID();
                if (this.entityCooldowns.containsKey(eid) && now - this.entityCooldowns.get(eid) <= 10) continue;
                this.tryTeleportEntity(e, serverLevel, now, true);
                count++;
            }
        }

        this.entityCooldowns.entrySet().removeIf(e -> now - e.getValue() > 200);
    }

    private PortalEntity findLinkedPortal(ServerLevel sl, BlockPos targetPos) {
        List<PortalEntity> portals = sl.getEntitiesOfClass(PortalEntity.class, new AABB(targetPos).inflate(2.0),
                p -> this.getOwner().equals(p.getOwner()) && p.isBlue() == !this.isBlue());
        return portals.isEmpty() ? null : portals.get(0);
    }

    private void tryTeleportPlayer(Player p, ServerLevel sl, long now) {
        UUID pid = p.getUUID();
        BlockPos target = PortalData.get(sl).getPos(this.getOwner(), !this.isBlue());
        if (target == null) {
            p.displayClientMessage(Component.literal("[!] Linked portal not found"), true);
            waitingPlayers.remove(pid);
            return;
        }

        if (waitingPlayers.containsKey(pid)) {
            AABB box = this.getBoundingBox().inflate(1.5);
            if (!box.contains(p.position())) {
                waitingPlayers.remove(pid);
                return;
            }
        }

        if (this.blockPosition().distSqr(target) > (long) DifModCommonConfig.PORTAL_MAX_DISTANCE.get() * DifModCommonConfig.PORTAL_MAX_DISTANCE.get()) {
            p.displayClientMessage(Component.literal("[!] Portal too far away"), true);
            waitingPlayers.remove(pid);
            return;
        }

        if (!sl.isLoaded(target)) {
            long startTick = waitingPlayers.getOrDefault(pid, now);
            if (!waitingPlayers.containsKey(pid)) waitingPlayers.put(pid, now);
            p.displayClientMessage(Component.literal("Please wait..."), true);
            sl.setChunkForced(target.getX() >> 4, target.getZ() >> 4, true);
            if (now - startTick > DifModCommonConfig.PORTAL_CHUNK_LOAD_TIMEOUT.get()) {
                p.displayClientMessage(Component.literal("[!] Portal unreachable"), true);
                sl.setChunkForced(target.getX() >> 4, target.getZ() >> 4, false);
                waitingPlayers.remove(pid);
            }
            return;
        }
        sl.setChunkForced(target.getX() >> 4, target.getZ() >> 4, false);
        waitingPlayers.remove(pid);

        PortalEntity other = findLinkedPortal(sl, target);
        if (other == null) {
            PortalData.get(sl).remove(this.getOwner(), !this.isBlue());
            p.displayClientMessage(Component.literal("[!] Linked portal not found"), true);
            return;
        }

        Vec3 newMotion = transformVelocity(p.getDeltaMovement(), this.getFacing(), other.getFacing());
        Vec3 dest = getDestinationPosition(other, p);
        double tx = dest.x;
        double ty = dest.y;
        double tz = dest.z;

        float newYaw = p.getYRot();
        // Relative rotation for players only on wall-to-wall portal teleportation
        if (this.getFacing().getAxis() != Direction.Axis.Y && other.getFacing().getAxis() != Direction.Axis.Y) {
            float yawDiff = other.getFacing().toYRot() - (this.getFacing().toYRot() + 180.0F);
            newYaw = p.getYRot() + yawDiff;
        }

        p.teleportTo((net.minecraft.server.level.ServerLevel)p.level(), tx, ty, tz, java.util.Set.of(), newYaw, p.getXRot());
        p.setYBodyRot(newYaw);
        p.setYHeadRot(newYaw);
        p.yRotO = newYaw;

        p.setDeltaMovement(newMotion);
        p.hurtMarked = true;
        if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(sp));
        }

        // Horizontal to vertical transition: set to swimming pose
        if (this.getFacing().getAxis() != Direction.Axis.Y && other.getFacing().getAxis() == Direction.Axis.Y) {
            p.setPose(net.minecraft.world.entity.Pose.SWIMMING);
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1F, 1.2F);
        this.level().playSound(null, target, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1F, 1.2F);
        other.lastTeleportTime = this.lastTeleportTime = now;
        
        // Cooldown ONLY on the target portal so the player can get out of it, but can fall/port through others instantly
        other.entityCooldowns.put(pid, now);
    }

    private void tryTeleportEntity(Entity entity, ServerLevel sl, long now, boolean isItem) {
        BlockPos target = PortalData.get(sl).getPos(this.getOwner(), !this.isBlue());
        if (target == null) return;

        if (this.blockPosition().distSqr(target) > (long) DifModCommonConfig.PORTAL_MAX_DISTANCE.get() * DifModCommonConfig.PORTAL_MAX_DISTANCE.get())
            return;

        if (!sl.isLoaded(target)) return;

        PortalEntity other = findLinkedPortal(sl, target);
        if (other == null) {
            PortalData.get(sl).remove(this.getOwner(), !this.isBlue());
            return;
        }

        Vec3 newMotion = transformVelocity(entity.getDeltaMovement(), this.getFacing(), other.getFacing());
        Vec3 dest = getDestinationPosition(other, entity);
        double tx = dest.x;
        double ty = dest.y;
        double tz = dest.z;

        entity.teleportTo(tx, ty, tz);
        entity.setDeltaMovement(newMotion);
        entity.hurtMarked = true;
        
        // Cooldown ONLY on the target portal
        other.entityCooldowns.put(entity.getUUID(), now);
        this.level().playSound(null, target, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.5F, 1.4F);
    }

    private Vec3 getDestinationPosition(PortalEntity other, Entity entity) {
        double tx, ty, tz;
        Direction outFace = other.getFacing();
        if (outFace.getAxis() == Direction.Axis.Y) {
            tx = other.getX();
            tz = other.getZ();
            ty = outFace == Direction.UP ? other.getY() + 0.0625 : other.getY() - entity.getBbHeight() - 0.0625;
        } else {
            ty = other.getY() - 1.0 + 0.01;
            tx = other.getX() + outFace.getStepX() * (entity.getBbWidth() * 0.5 + 0.0625);
            tz = other.getZ() + outFace.getStepZ() * (entity.getBbWidth() * 0.5 + 0.0625);
        }
        return new Vec3(tx, ty, tz);
    }

    private static Vec3 transformVelocity(Vec3 velocity, Direction inFacing, Direction outFacing) {
        double speed = velocity.length();
        if (speed < 0.001) return velocity;
        Vec3 norm = velocity.normalize();
        Vec3 inAxis = dirToVec(inFacing);
        Vec3 outAxis = dirToVec(outFacing.getOpposite());
        Vec3 transformed = rotateVector(norm, inAxis, outAxis);
        return transformed.scale(speed);
    }

    private static Vec3 dirToVec(Direction d) {
        return new Vec3(d.getStepX(), d.getStepY(), d.getStepZ());
    }

    private static Vec3 rotateVector(Vec3 v, Vec3 from, Vec3 to) {
        Vec3 axis = from.cross(to);
        double sinAngle = axis.length();
        double cosAngle = from.dot(to);
        if (sinAngle < 0.001) {
            if (cosAngle > 0) return v;
            return v.scale(-1);
        }
        axis = axis.normalize();
        return v.scale(cosAngle).add(axis.cross(v).scale(sinAngle)).add(axis.scale(axis.dot(v) * (1 - cosAngle)));
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        if (tag.hasUUID("owner")) {
            this.setOwner(tag.getUUID("owner"));
        }
        this.setIsBlue(tag.getBoolean("isBlue"));
        this.setIsLinked(tag.getBoolean("isLinked"));
        if (tag.contains("facing")) {
            this.setFacing(Direction.byName(tag.getString("facing")));
        }
        if (tag.contains("upDir")) {
            this.setUpDir(Direction.byName(tag.getString("upDir")));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        if (this.getOwner() != null) {
            tag.putUUID("owner", this.getOwner());
        }
        tag.putBoolean("isBlue", this.isBlue());
        tag.putBoolean("isLinked", this.isLinked());
        if (this.getFacing() != null) {
            tag.putString("facing", this.getFacing().getName());
        }
        if (this.getUpDir() != null) {
            tag.putString("upDir", this.getUpDir().getName());
        }
    }
    
    public static void removeOldPortal(ServerLevel serverLevel, UUID owner, boolean isBlue) {
        BlockPos targetPos = PortalData.get(serverLevel).getPos(owner, isBlue);
        if (targetPos != null) {
            PortalData.get(serverLevel).remove(owner, isBlue);
            boolean wasLoaded = serverLevel.isLoaded(targetPos);
            if (!wasLoaded) serverLevel.setChunkForced(targetPos.getX() >> 4, targetPos.getZ() >> 4, true);

            List<PortalEntity> portals = serverLevel.getEntitiesOfClass(PortalEntity.class, new AABB(targetPos).inflate(2.0),
                    p -> owner.equals(p.getOwner()) && p.isBlue() == isBlue);
            for (PortalEntity p : portals) {
                p.discard();
            }
            
            // Leave it forced if it was forced, otherwise remove force if we just forced it temporarily
            // Note: If you want to unforce, uncomment the next line:
            // if (!wasLoaded) serverLevel.setChunkForced(targetPos.getX() >> 4, targetPos.getZ() >> 4, false);
        }
    }
    
    
    public static PortalEntity findPortal(ServerLevel sl, UUID owner, boolean isBlue) {
        BlockPos pos = PortalData.get(sl).getPos(owner, isBlue);
        if (pos == null) return null;
        List<PortalEntity> list = sl.getEntitiesOfClass(PortalEntity.class, new AABB(pos).inflate(2.0),
                p -> owner.equals(p.getOwner()) && p.isBlue() == isBlue);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void updateLinks(ServerLevel sl, UUID owner) {
        PortalEntity blue = findPortal(sl, owner, true);
        PortalEntity orange = findPortal(sl, owner, false);
        
        boolean linked = (blue != null && orange != null);
        
        if (blue != null) {
            blue.setIsLinked(linked);
        }
        if (orange != null) {
            orange.setIsLinked(linked);
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide()) {
            ServerLevel sl = (ServerLevel) this.level();
            sl.setChunkForced(this.chunkPosition().x, this.chunkPosition().z, true);
            updateLinks(sl, this.getOwner());
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!this.level().isClientSide()) {
            ServerLevel sl = (ServerLevel) this.level();
            sl.setChunkForced(this.chunkPosition().x, this.chunkPosition().z, false);
            updateLinks(sl, this.getOwner());
        }
    }

    @Override
    public boolean hurt(@NotNull net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            PortalData.get((ServerLevel) this.level()).remove(this.getOwner(), this.isBlue());
            this.discard();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }
}
