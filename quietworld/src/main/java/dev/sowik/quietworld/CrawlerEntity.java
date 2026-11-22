package dev.sowik.quietworld;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

/** A black, fast spider that climbs walls. Hunts in the dark and in the Hollow Copy. */
public class CrawlerEntity extends Spider {
    private int life;

    public CrawlerEntity(EntityType<? extends CrawlerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            Codex.encounter(this, "mob_crawler");
            if (++life > 4800) discard();
        }
    }
}
