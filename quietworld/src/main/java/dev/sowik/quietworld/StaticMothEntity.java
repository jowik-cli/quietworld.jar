package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** A crackling bat-shaped moth that eats torches and hums static. */
public class StaticMothEntity extends Bat {
    private int life;

    public StaticMothEntity(EntityType<? extends Bat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Bat.createAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_static_moth");

        life++;
        if (life % 40 == 0) {
            BlockPos c = this.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-3, -2, -3), c.offset(3, 2, 3))) {
                BlockState st = this.level().getBlockState(pos);
                if (st.is(Blocks.TORCH) || st.is(Blocks.WALL_TORCH)) {
                    BlockPos found = pos.immutable();
                    this.level().removeBlock(found, false);
                    this.level().playSound(null, found, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.2F);
                    break;
                }
            }
        }
        if (life % 100 == 0 && this.level().getNearestPlayer(this, 12.0D) != null) {
            Sfx.play(this.level(), this.getX(), this.getY(), this.getZ(), ModSounds.STATIC_LONG,
                    SoundSource.HOSTILE, 0.3F, 1.6F);
        }
        if (life > 2400) discard();
    }
}
