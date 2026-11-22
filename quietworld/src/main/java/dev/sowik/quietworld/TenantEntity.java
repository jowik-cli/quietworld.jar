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

/** A faceless villager that lives in the Hollow Copy. Talk to it. */
public class TenantEntity extends PathfinderMob {
    private static final String[] LINES = {
        "Mm. A guest.",
        "The lease is short. Nobody reads it.",
        "The last tenant was Entity 303. He still owes rent.",
        "I keep the lights on for whoever is still looking.",
        "You came through a door that was never a door.",
        "Go back before the sky finishes counting."
    };

    private int life;
    private int talk;
    private boolean gaveNote;

    public TenantEntity(EntityType<? extends TenantEntity> type, Level level) {
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
            if (!gaveNote) {
                gaveNote = true;
                Dims.give(sp, Lore.siteBook(Lore.SITE_LEASE, sp.getName().getString()));
            }
            Dialogue.sayAs(sp, "Tenant", LINES[talk % LINES.length]);
            talk++;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_tenant");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 32.0D);
        if (nearest != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, nearest.getEyePosition());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        } else if (life > 9000) {
            discard();
        }
    }
}
