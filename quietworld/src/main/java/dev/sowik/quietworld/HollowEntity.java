package dev.sowik.quietworld;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Only moves while you are not looking at it. Reaching you sends you to the Static Hall. */
public class HollowEntity extends PathfinderMob {
    private int life;
    private boolean wasSeen;

    public HollowEntity(EntityType<? extends HollowEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
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

    private boolean isObservedBy(ServerPlayer p) {
        Vec3 view = p.getViewVector(1.0F).normalize();
        Vec3 toMe = new Vec3(this.getX() - p.getX(), this.getEyeY() - p.getEyeY(), this.getZ() - p.getZ());
        if (toMe.lengthSqr() < 0.0001D) return true;
        return view.dot(toMe.normalize()) > 0.8D && p.hasLineOfSight(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_hollow");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 64.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 200) discard();
            return;
        }

        if (isObservedBy(player)) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            if (!wasSeen && this.distanceToSqr(player) < 40.0D * 40.0D) {
                wasSeen = true;
                Sfx.at(player, ModSounds.HEARTBEAT, 0.8F, 1.0F);
            }
        } else if (life % 5 == 0) {
            this.getNavigation().moveTo(player, 1.15D);
        }

        if (this.distanceToSqr(player) < 2.5D) {
            touch(player);
            return;
        }
        if (life > 3600) discard();
    }

    private void touch(ServerPlayer player) {
        if (player.level().dimension() == Dims.HALL) {
            Halls.scatter(player);
        } else if (!Dims.enterHall(player)) {
            // the Static Hall is not loaded in this world: still count it for the story
            StoryState.of(player).raisePhaseFloor(1);
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false));
            Sfx.at(player, ModSounds.STATIC_BURST, 1.0F, 0.8F);
        }
        discard();
    }
}
