package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** A flickering thing that hops around you, leaving corrupted blocks. Killing it can drop a corrupted book. */
public class GlitchEntity extends PathfinderMob {
    private int life;
    private int nextHop = 20;
    private boolean pendingHop;

    public GlitchEntity(EntityType<? extends GlitchEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        pendingHop = true;
        return result;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_glitch");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 48.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 100) discard();
            return;
        }

        if (life >= nextHop || pendingHop) {
            hop(player);
            nextHop = life + 25 + this.random.nextInt(30);
            pendingHop = false;
        }
        if (life > 500) discard();
    }

    private void hop(ServerPlayer player) {
        ServerLevel level = (ServerLevel) this.level();
        RandomSource rnd = this.random;
        double angle = rnd.nextDouble() * Math.PI * 2.0D;
        double dist = 4.0D + rnd.nextInt(6);
        int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
        int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);
        if (!level.hasChunkAt(new BlockPos(x, player.getBlockY(), z))) return;

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - player.getBlockY()) > 8) return;

        this.moveTo(x + 0.5D, y, z + 0.5D, rnd.nextFloat() * 360.0F, 0.0F);
        Sfx.play(level, x + 0.5D, y, z + 0.5D, ModSounds.GLITCH, SoundSource.HOSTILE, 0.8F, 0.8F + rnd.nextFloat() * 0.6F);
        Corrupt.scatter(level, this.blockPosition(), 3, 3 + rnd.nextInt(4), rnd);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.spawnAtLocation(new ItemStack(ModItems.SHARD.get()));
            if (this.random.nextInt(2) == 0) {
                this.spawnAtLocation(Corruption.makeBook(this.random));
            }
        }
    }
}
