package dev.sowik.quietworld;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A silent figure that stares at the nearest player and leaves when approached. */
public class WatcherEntity extends PathfinderMob {
    private int life;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Only /kill and the void can remove it by damage.
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        life++;
        Player nearest = this.level().getNearestPlayer(this, 80.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 200) discard();
            return;
        }

        this.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        int phase = StoryState.of(player).phase(player);
        double vanishDistance = phase >= 2 ? 5.0D : 11.0D;
        if (this.distanceToSqr(player) < vanishDistance * vanishDistance) {
            StoryState.deliverNext(player, this.position(), false);
            discard();
            return;
        }

        if (life > 1800) discard(); // 90 seconds
    }
}
