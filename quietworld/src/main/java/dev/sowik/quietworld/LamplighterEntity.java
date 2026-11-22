package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Carries a soul lantern toward the nearest structure and waits for you to keep up. */
public class LamplighterEntity extends PathfinderMob {
    private int life;

    public LamplighterEntity(EntityType<? extends LamplighterEntity> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SOUL_LANTERN));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 80.0D);
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

    /** The nearest registered structure, doorway or mast in this dimension within 300 blocks, or null. */
    public static BlockPos nearestSite(ServerLevel level, Vec3 from) {
        String dim = level.dimension().location().toString();
        BlockPos best = null;
        double bestDist = 300.0D * 300.0D;
        for (SiteData.Site s : SiteData.get(level.getServer()).sites()) {
            if (!s.dim().equals(dim)) continue;
            boolean guide = s.type() == SiteData.GATE || s.type() == SiteData.MAST || s.type() >= SiteData.STRUCT_BASE;
            if (!guide) continue;
            double dx = s.pos().getX() + 0.5D - from.x;
            double dz = s.pos().getZ() + 0.5D - from.z;
            double d2 = dx * dx + dz * dz;
            if (d2 < bestDist) {
                bestDist = d2;
                best = s.pos();
            }
        }
        return best;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_lamplighter");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 90.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 200) discard();
            return;
        }

        if (life % 20 == 0) {
            BlockPos site = nearestSite((ServerLevel) this.level(), this.position());
            if (site == null) {
                if (life > 600) vanish(player);
            } else if (site.distSqr(this.blockPosition()) < 36.0D) {
                vanish(player);
            } else if (this.distanceToSqr(player) < 22.0D * 22.0D) {
                this.getNavigation().moveTo(site.getX() + 0.5D, site.getY(), site.getZ() + 0.5D, 1.0D);
            } else {
                this.getNavigation().stop();
            }
        }
        if (life > 7200) discard();
    }

    private void vanish(ServerPlayer player) {
        Sfx.at(player, ModSounds.WHISPER, 0.5F, 1.2F);
        discard();
    }
}
