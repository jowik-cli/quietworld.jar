package dev.sowik.quietworld;

import net.minecraft.commands.arguments.EntityAnchorArgument;
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
import net.minecraft.world.level.Level;

/** Writes one of your undiscovered archive entries in for you, then leaves. */
public class ArchivistEntity extends PathfinderMob {
    private int life;
    private int leaveAt = -1;
    private boolean helped;

    public ArchivistEntity(EntityType<? extends ArchivistEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
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

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide && player instanceof ServerPlayer sp) {
            if (!helped) {
                helped = true;
                String title = Codex.unlockRandomLocked(sp);
                if (title != null) {
                    Dialogue.sayAs(sp, "Archivist", "I wrote this down for you: " + title + ".");
                } else {
                    Dialogue.sayAs(sp, "Archivist", "There is nothing left I can add.");
                }
                leaveAt = life + 100;
            } else {
                Dialogue.sayAs(sp, "Archivist", "...go.");
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_archivist");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 32.0D);
        if (nearest != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, nearest.getEyePosition());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
        if (life > 3600 || (leaveAt > 0 && life > leaveAt)) {
            discard();
        }
    }
}
