package dev.sowik.quietworld;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Runs when watched, creeps closer when not. Talk to it and it gives you a shard. */
public class LostChildEntity extends PathfinderMob {
    private static final String[] LINES = {
        "play with me?",
        "mommy said not to look",
        "are you the new one",
        "the tall man is not nice",
        "do you have a light"
    };

    private int life;
    private int nextSpeak = 300;
    private boolean gave;

    public LostChildEntity(EntityType<? extends LostChildEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
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
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide && player instanceof ServerPlayer sp) {
            if (!gave) {
                gave = true;
                Dims.give(sp, new ItemStack(ModItems.SHARD.get()));
                Dialogue.sayAs(sp, "Child", "found this. you can have it.");
            } else {
                Dialogue.sayAs(sp, "Child", LINES[this.random.nextInt(LINES.length)]);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_child");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 48.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 200) discard();
            return;
        }

        double d2 = this.distanceToSqr(player);
        boolean watched = isObservedBy(player);
        if (watched && d2 < 81.0D) {
            Vec3 away = this.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(8.0D);
            Vec3 goal = this.position().add(away);
            if (life % 10 == 0) {
                this.getNavigation().moveTo(goal.x, goal.y, goal.z, 1.3D);
            }
        } else if (!watched && d2 > 25.0D) {
            if (life % 10 == 0) {
                this.getNavigation().moveTo(player, 1.0D);
            }
        } else {
            this.getNavigation().stop();
        }

        if (life >= nextSpeak && d2 < 900.0D) {
            Dialogue.sayAs(player, "Child", LINES[this.random.nextInt(LINES.length)]);
            nextSpeak = life + 500 + this.random.nextInt(500);
        }
        if (life > 4800) discard();
    }
}
