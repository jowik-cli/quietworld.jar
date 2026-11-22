package dev.sowik.quietworld;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Wears your name and repeats what you said in chat, garbled, as if you had said it. */
public class EchoEntity extends PathfinderMob {
    private static final Map<UUID, List<String>> HISTORY = new HashMap<>();

    private int life;
    private int nextSpeak = 500;
    private boolean named;

    public EchoEntity(EntityType<? extends EchoEntity> type, Level level) {
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
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_echo");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 80.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 200) discard();
            return;
        }

        if (!named) {
            named = true;
            this.setCustomName(player.getName());
            this.setCustomNameVisible(true);
        }

        this.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        if (this.distanceToSqr(player) < 64.0D) {
            Sfx.at(player, ModSounds.WHISPER, 0.7F, 0.9F);
            discard();
            return;
        }

        if (life >= nextSpeak) {
            speakFromHistory(player);
            nextSpeak = life + 400 + this.random.nextInt(800);
        }
        if (life > 3000) discard();
    }

    private void speakFromHistory(ServerPlayer player) {
        List<String> said = HISTORY.get(player.getUUID());
        if (said == null || said.isEmpty()) return;
        String line = said.get(this.random.nextInt(said.size()));
        player.sendSystemMessage(Component.literal("<" + player.getName().getString() + "> "
                + Corruption.garble(line, this.random, 0.12D)));
    }

    /** Remembers the last few things a player typed. */
    public static void record(ServerPlayer player, String text) {
        List<String> said = HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        said.add(text);
        while (said.size() > 6) {
            said.remove(0);
        }
    }

    /** If an Echo is nearby, it repeats this message back a little later. */
    public static void hear(ServerPlayer player, String text) {
        List<EchoEntity> echoes = player.level().getEntitiesOfClass(EchoEntity.class, player.getBoundingBox().inflate(64.0D));
        if (echoes.isEmpty()) return;
        EchoEntity echo = echoes.get(0);
        String garbled = Corruption.garble(text, player.getRandom(), 0.1D);
        int delay = 120 + player.getRandom().nextInt(500);
        Scheduler.later(delay, () -> {
            if (echo.isAlive()) {
                player.sendSystemMessage(Component.literal("<" + player.getName().getString() + "> " + garbled));
            }
        });
    }
}
