package dev.sowik.quietworld;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RandomEvents {
    private RandomEvents() {}

    public enum Type {
        DISTANT_SOUND(0, 2),
        FAKE_JOIN(0, 2),
        WATCHER(0, 2),
        KOSIW_SKY(0, 1),
        KOSIW_VISIT(0, 1),
        TORCH_OUT(1, 2),
        SIGN(1, 2),
        RITUAL(1, 2),
        DARKEN(2, 2);

        final int minPhase;
        final int maxPhase;

        Type(int minPhase, int maxPhase) {
            this.minPhase = minPhase;
            this.maxPhase = maxPhase;
        }
    }

    private static final SoundEvent[] SOUNDS = {
        SoundEvents.WOODEN_DOOR_OPEN,
        SoundEvents.WOODEN_DOOR_CLOSE,
        SoundEvents.CHEST_OPEN,
        SoundEvents.CHEST_CLOSE,
        SoundEvents.ENDERMAN_STARE
    };

    private static final String[][] SIGN_TEXTS = {
        {"", "don't", "turn around", ""},
        {"it's behind", "the tree", "", ""},
        {"you already", "know this", "place", ""},
        {"stay in", "the light", "", ""},
        {"", "I counted", "your steps", ""}
    };

    /** Rolls for an event. force = true skips the chance and cooldown (used by the command). */
    public static boolean roll(ServerPlayer player, StoryState state, boolean force) {
        if (player.isSleeping() || player.isSpectator()) return false;

        long now = player.level().getGameTime();
        if (!force) {
            long last = state.lastEventTick();
            if (last >= 0 && now - last < Config.EVENT_COOLDOWN_SECONDS.get() * 20L) return false;
            if (player.getRandom().nextDouble() > Config.EVENT_CHANCE.get()) return false;
        }

        int phase = state.phase(player);
        List<Type> pool = new ArrayList<>();
        for (Type t : Type.values()) {
            if (t.minPhase <= phase && phase <= t.maxPhase) pool.add(t);
        }
        Type chosen = pool.get(player.getRandom().nextInt(pool.size()));
        boolean fired = run(chosen, player);
        if (fired) state.setLastEventTick(now);
        return fired;
    }

    public static boolean run(Type type, ServerPlayer player) {
        return switch (type) {
            case DISTANT_SOUND -> distantSound(player);
            case FAKE_JOIN -> fakeJoin(player);
            case WATCHER -> spawnWatcher(player, 30.0D + player.getRandom().nextInt(15));
            case KOSIW_SKY -> Config.KOSIW_ENABLED.get() && KosiwSpawner.spawn(player, KosiwEntity.Mode.SKY);
            case KOSIW_VISIT -> Config.KOSIW_ENABLED.get() && KosiwSpawner.spawn(player, KosiwEntity.Mode.VISIT);
            case RITUAL -> Rituals.spawnNear(player, 60, 110, false) != null;
            case TORCH_OUT -> torchOut(player);
            case SIGN -> placeSign(player);
            case DARKEN -> darken(player);
        };
    }

    private static boolean distantSound(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        double angle = p.getRandom().nextDouble() * Math.PI * 2.0D;
        double dist = 14.0D + p.getRandom().nextInt(12);
        double x = p.getX() + Math.cos(angle) * dist;
        double z = p.getZ() + Math.sin(angle) * dist;
        SoundEvent sound = SOUNDS[p.getRandom().nextInt(SOUNDS.length)];
        level.playSound(null, x, p.getY(), z, sound, SoundSource.AMBIENT, 0.8F, 0.6F + p.getRandom().nextFloat() * 0.25F);
        return true;
    }

    private static boolean fakeJoin(ServerPlayer p) {
        p.sendSystemMessage(Component.translatable("multiplayer.player.joined", p.getDisplayName())
                .withStyle(ChatFormatting.YELLOW));
        return true;
    }

    public static boolean spawnWatcher(ServerPlayer p, double distance) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
        if (!level.getEntitiesOfClass(WatcherEntity.class, p.getBoundingBox().inflate(96.0D)).isEmpty()) return false;

        double angle = p.getRandom().nextDouble() * Math.PI * 2.0D;
        int x = Mth.floor(p.getX() + Math.cos(angle) * distance);
        int z = Mth.floor(p.getZ() + Math.sin(angle) * distance);
        if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) return false;

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        WatcherEntity watcher = ModEntities.WATCHER.get().create(level);
        if (watcher == null) return false;
        watcher.moveTo(x + 0.5D, y, z + 0.5D, p.getRandom().nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(watcher);
        return true;
    }

    private static boolean torchOut(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        BlockPos c = p.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(c.offset(-14, -6, -14), c.offset(14, 6, 14))) {
            BlockState st = level.getBlockState(pos);
            if (st.is(Blocks.TORCH) || st.is(Blocks.WALL_TORCH)) {
                BlockPos found = pos.immutable();
                level.removeBlock(found, false);
                level.playSound(null, found, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private static boolean placeSign(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        Direction facing = p.getDirection();
        BlockPos base = p.blockPosition().relative(facing, 4);
        if (!level.hasChunkAt(base)) return false;

        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (Math.abs(pos.getY() - p.getBlockY()) > 6) return false;

        int rotation = Mth.floor((p.getYRot() + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
        BlockState sign = Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rotation);
        level.setBlock(pos, sign, 3);

        if (level.getBlockEntity(pos) instanceof SignBlockEntity be) {
            String[] lines = SIGN_TEXTS[new Random().nextInt(SIGN_TEXTS.length)];
            Component[] messages = new Component[4];
            for (int i = 0; i < 4; i++) {
                messages[i] = Component.literal(lines[i]);
            }
            be.setText(new SignText(messages, messages, DyeColor.BLACK, false), true);
            be.setChanged();
            level.sendBlockUpdated(pos, sign, sign, 3);
        }
        return true;
    }

    private static boolean darken(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
        return true;
    }
}
