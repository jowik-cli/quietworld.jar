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

/** The opposite of a Hollow: it only moves while you are looking at it. Contact hurts and darkens the world. */
public class GazerEntity extends PathfinderMob {
    private int life;
    private boolean seen;

    public GazerEntity(EntityType<? extends GazerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
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
        Codex.encounter(this, "mob_gazer");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 64.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 200) discard();
            return;
        }

        if (isObservedBy(player)) {
            if (life % 5 == 0) {
                this.getNavigation().moveTo(player, 1.25D);
            }
            if (!seen && this.distanceToSqr(player) < 40.0D * 40.0D) {
                seen = true;
                Sfx.at(player, ModSounds.HEARTBEAT, 0.8F, 1.4F);
            }
        } else {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }

        if (this.distanceToSqr(player) < 2.5D) {
            player.hurt(this.level().damageSources().mobAttack(this), 4.0F);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
            Sfx.at(player, ModSounds.SCREECH, 0.8F, 1.0F);
            discard();
            return;
        }
        if (life > 3600) discard();
    }
}
