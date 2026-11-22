package dev.sowik.quietworld;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
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
import java.util.Locale;
import java.util.Random;

public final class RandomEvents {
    private RandomEvents() {}

    public enum Type {
        // early and always
        DISTANT_SOUND(0, 5), FAKE_JOIN(0, 5), WATCHER(0, 5), STATIC_BURST(0, 5), WHISPER(0, 5), STRUCTURE(0, 5),
        DOORS_OPEN(0, 5), FAKE_CREEPER(0, 5), FOOTSTEPS(0, 5), CHAT_GLITCH(0, 5), BELL(0, 5), ASH_FALL(0, 5), LOST_NOTE(0, 5),
        KOSIW_SKY(0, 2), KOSIW_VISIT(0, 2),
        // from the turn
        TORCH_OUT(1, 5), SIGN(1, 5), HEARTBEAT(1, 5), CORRUPT_CHUNK(1, 5), GLITCH(1, 5), ECHO(1, 5), HOLLOW(1, 5),
        CRAWLER(1, 5), MOB_GROAN(1, 5), THUNDER(1, 5), BLINK(1, 5), MANNEQUIN(1, 5), ITEM_GIFT(1, 5),
        NAME_CALL(1, 5), DESKTOP_FILE(1, 5), MOTHS(1, 5), LAMPLIGHTER(1, 5), LOST_CHILD(1, 5),
        // the silence
        TALL_ONE(2, 5), DARKEN(2, 5), HUNGER_PANG(2, 5), STORM(2, 5), NIGHT_FALLS(2, 5), GAZER(2, 5),
        ARCHIVIST(2, 5), FAKE_DEATH(2, 5),
        // the copy
        WRETCH_PACK(3, 5), HOLLOW_PACK(3, 5), PICKPOCKET(3, 5), HEALTH_DRAIN(3, 5), STRUCTURE_CLUSTER(3, 5),
        CORRUPT_STORM(3, 5), SWAP_HOTBAR(3, 5),
        // the breaking
        STATIC_STORM(4, 5), DISPLACEMENT(4, 5), TALL_PAIR(4, 5);

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
        {"", "I counted", "your steps", ""},
        {"HEROBRINE", "WAS HERE", "(brb)", ""},
        {"not", "wonderland", ".jar", "(legal said)"},
        {"NULL", "sends", "regards", ""},
        {"entity 303", "owes rent", "", ""},
        {"it is a", "broken script", "not a broken", "sign"},
        {"cave dweller", "knocked", "twice", ""}
    };

    /** Rolls for an event. force = true skips the chance and cooldown (used by the command). */
    public static boolean roll(ServerPlayer player, StoryState state, boolean force) {
        if (player.isSleeping() || player.isSpectator()) return false;
        if (player.level().dimension() == Dims.HALL) return false; // the hall has its own ambient horror

        long now = player.level().getGameTime();
        int phase = state.phase(player);
        if (!force) {
            long last = state.lastEventTick();
            long cooldown = (long) (Config.EVENT_COOLDOWN_SECONDS.get() * 20L * Phases.cooldownMultiplier(phase));
            if (last >= 0 && now - last < cooldown) return false;
            double chance = Math.min(0.95D, Config.EVENT_CHANCE.get() * Phases.chanceMultiplier(phase));
            if (player.getRandom().nextDouble() > chance) return false;
        }

        List<Type> pool = new ArrayList<>();
        for (Type t : Type.values()) {
            if (t.minPhase <= phase && phase <= t.maxPhase) pool.add(t);
        }
        Type chosen = pool.get(player.getRandom().nextInt(pool.size()));
        boolean fired = run(chosen, player);
        if (fired) {
            state.setLastEventTick(now);
            Codex.unlock(player, "event_" + chosen.name().toLowerCase(Locale.ROOT));
        }
        return fired;
    }

    public static boolean run(Type type, ServerPlayer player) {
        return switch (type) {
            case DISTANT_SOUND -> distantSound(player);
            case FAKE_JOIN -> fakeJoin(player);
            case WATCHER -> watcherEvent(player);
            case STATIC_BURST -> staticBurst(player);
            case WHISPER -> whisper(player);
            case STRUCTURE -> structure(player);
            case KOSIW_SKY -> Config.KOSIW_ENABLED.get() && KosiwSpawner.spawn(player, KosiwEntity.Mode.SKY);
            case KOSIW_VISIT -> Config.KOSIW_ENABLED.get() && KosiwSpawner.spawn(player, KosiwEntity.Mode.VISIT);
            case TORCH_OUT -> torchOut(player);
            case SIGN -> placeSign(player);
            case HEARTBEAT -> heartbeat(player);
            case CORRUPT_CHUNK -> corruptChunk(player);
            case GLITCH -> Spawns.near(player, ModEntities.GLITCH.get(), 7.0D) != null;
            case ECHO -> spawnEcho(player);
            case HOLLOW -> hollow(player);
            case CRAWLER -> crawler(player);
            case TALL_ONE -> tallOne(player);
            case DARKEN -> darken(player);
            case DOORS_OPEN -> doorsOpen(player);
            case FAKE_CREEPER -> fakeCreeper(player);
            case FOOTSTEPS -> footsteps(player);
            case CHAT_GLITCH -> chatGlitch(player);
            case BELL -> bell(player);
            case ASH_FALL -> ashFall(player);
            case LOST_NOTE -> lostNote(player);
            case MOB_GROAN -> mobGroan(player);
            case THUNDER -> thunder(player);
            case BLINK -> blink(player);
            case MANNEQUIN -> mannequin(player);
            case ITEM_GIFT -> itemGift(player);
            case NAME_CALL -> nameCall(player);
            case DESKTOP_FILE -> desktopFile(player);
            case MOTHS -> moths(player);
            case LAMPLIGHTER -> lamplighter(player);
            case LOST_CHILD -> Spawns.near(player, ModEntities.LOST_CHILD.get(), 16.0D) != null;
            case HUNGER_PANG -> hungerPang(player);
            case STORM -> storm(player);
            case NIGHT_FALLS -> nightFalls(player);
            case GAZER -> Spawns.near(player, ModEntities.GAZER.get(), 24.0D) != null;
            case ARCHIVIST -> Spawns.near(player, ModEntities.ARCHIVIST.get(), 9.0D) != null;
            case FAKE_DEATH -> fakeDeath(player);
            case WRETCH_PACK -> wretchPack(player);
            case HOLLOW_PACK -> hollowPack(player);
            case PICKPOCKET -> pickpocket(player);
            case HEALTH_DRAIN -> healthDrain(player);
            case STRUCTURE_CLUSTER -> structureCluster(player);
            case CORRUPT_STORM -> corruptStorm(player);
            case SWAP_HOTBAR -> swapHotbar(player);
            case STATIC_STORM -> staticStorm(player);
            case DISPLACEMENT -> displacement(player);
            case TALL_PAIR -> tallPair(player);
        };
    }

    /** Used by the /quiet mob test command. */
    public static boolean spawnMob(ServerPlayer p, String kind) {
        switch (kind) {
            case "hollow": return hollow(p);
            case "glitch": return Spawns.near(p, ModEntities.GLITCH.get(), 7.0D) != null;
            case "echo": return spawnEcho(p);
            case "crawler": return Spawns.near(p, ModEntities.CRAWLER.get(), 14.0D) != null;
            case "tall": return tallOne(p);
            case "tenant": return Spawns.near(p, ModEntities.TENANT.get(), 8.0D) != null;
            case "gazer": return Spawns.near(p, ModEntities.GAZER.get(), 20.0D) != null;
            case "moth": return moths(p);
            case "lamplighter": return lamplighter(p);
            case "archivist": return Spawns.near(p, ModEntities.ARCHIVIST.get(), 7.0D) != null;
            case "wretch": return wretchPack(p);
            case "child": return Spawns.near(p, ModEntities.LOST_CHILD.get(), 12.0D) != null;
            default: return false;
        }
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

    private static final String[] FAKE_NAMES = {"Herobrine", "Entity303", "null", "wonderland.jar", "CaveDweller"};

    private static boolean fakeJoin(ServerPlayer p) {
        // one time in four, somebody famous drops by and leaves again a few seconds later
        if (p.getRandom().nextInt(4) == 0) {
            Component name = Component.literal(FAKE_NAMES[p.getRandom().nextInt(FAKE_NAMES.length)]);
            p.sendSystemMessage(Component.translatable("multiplayer.player.joined", name).withStyle(ChatFormatting.YELLOW));
            Scheduler.later(60, () -> p.sendSystemMessage(
                    Component.translatable("multiplayer.player.left", name).withStyle(ChatFormatting.YELLOW)));
            return true;
        }
        p.sendSystemMessage(Component.translatable("multiplayer.player.joined", p.getDisplayName())
                .withStyle(ChatFormatting.YELLOW));
        return true;
    }

    private static boolean watcherEvent(ServerPlayer p) {
        boolean ok = spawnWatcher(p, 30.0D + p.getRandom().nextInt(15));
        if (ok) Sfx.at(p, ModSounds.DRONE, 0.5F, 0.8F);
        return ok;
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

    private static boolean staticBurst(ServerPlayer p) {
        Sfx.at(p, ModSounds.STATIC_BURST, 1.0F, 0.9F + p.getRandom().nextFloat() * 0.2F);
        return true;
    }

    private static boolean whisper(ServerPlayer p) {
        Sfx.at(p, ModSounds.WHISPER, 0.9F, 0.9F);
        return true;
    }

    private static boolean heartbeat(ServerPlayer p) {
        Sfx.at(p, ModSounds.HEARTBEAT, 0.9F, 1.0F);
        Scheduler.later(70, () -> Sfx.at(p, ModSounds.HEARTBEAT, 0.9F, 1.1F));
        return true;
    }

    private static boolean structure(ServerPlayer p) {
        BlockPos pos = Structures.spawnNear(p, 60, 120, false);
        if (pos == null) return false;
        Sfx.at(p, ModSounds.WHISPER, 0.6F, 0.8F);
        return true;
    }

    private static boolean corruptChunk(ServerPlayer p) {
        if (!Corrupt.chunkNear(p, 40, 90)) return false;
        Sfx.at(p, ModSounds.GLITCH, 0.9F, 0.8F);
        return true;
    }

    public static boolean spawnEcho(ServerPlayer p) {
        EchoEntity echo = Spawns.near(p, ModEntities.ECHO.get(), 35.0D);
        if (echo == null) return false;
        Sfx.at(p, ModSounds.WHISPER, 0.7F, 0.9F);
        return true;
    }

    private static boolean hollow(ServerPlayer p) {
        HollowEntity hollow = Spawns.near(p, ModEntities.HOLLOW.get(), 28.0D);
        if (hollow == null) return false;
        Sfx.at(p, ModSounds.HEARTBEAT, 0.7F, 0.9F);
        return true;
    }

    private static boolean crawler(ServerPlayer p) {
        if (!p.level().isNight()) return false;
        return Spawns.near(p, ModEntities.CRAWLER.get(), 18.0D) != null;
    }

    private static boolean tallOne(ServerPlayer p) {
        TallOneEntity tall = Spawns.near(p, ModEntities.TALL_ONE.get(), 24.0D);
        if (tall == null) return false;
        Sfx.at(p, ModSounds.DRONE, 0.8F, 0.6F);
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
                Sfx.at(p, ModSounds.TV_OFF, 0.7F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private static boolean placeSign(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
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
        Sfx.at(p, ModSounds.GLITCH, 0.5F, 1.0F);
        return true;
    }

    private static final String[] CHAT_LINES = {
        "<K.> are you still there",
        "[Server] please do not leave",
        "<kosiw> behind you",
        "<Herobrine> brb",
        "<Entity303> who touched my rent",
        "[Server] \u00a7kxxxxxxxx\u00a7r has joined the quiet",
        "<null> ",
        "<K.> do not trust the second list of rules"
    };

    private static Vec3 behind(ServerPlayer p, double dist) {
        Vec3 look = p.getLookAngle();
        return new Vec3(p.getX() - look.x * dist, p.getY(), p.getZ() - look.z * dist);
    }

    private static boolean doorsOpen(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        BlockPos c = p.blockPosition();
        boolean any = false;
        for (BlockPos pos : BlockPos.betweenClosed(c.offset(-12, -3, -12), c.offset(12, 3, 12))) {
            BlockState st = level.getBlockState(pos);
            if (st.is(BlockTags.WOODEN_DOORS) && st.hasProperty(DoorBlock.OPEN) && !st.getValue(DoorBlock.OPEN)) {
                level.setBlock(pos.immutable(), st.setValue(DoorBlock.OPEN, true), 10);
                any = true;
            }
        }
        if (any) {
            level.playSound(null, c, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.7F);
        }
        return any;
    }

    private static boolean fakeCreeper(ServerPlayer p) {
        Vec3 at = behind(p, 4.0D);
        ((ServerLevel) p.level()).playSound(null, at.x, at.y, at.z, SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0F, 1.0F);
        return true;
    }

    private static boolean footsteps(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        Vec3 look = p.getLookAngle();
        double bx = p.getX();
        double by = p.getY();
        double bz = p.getZ();
        for (int i = 0; i < 6; i++) {
            double d = 10.0D - i * 1.6D;
            double x = bx - look.x * d;
            double z = bz - look.z * d;
            Scheduler.later(i * 14, () -> level.playSound(null, x, by, z, SoundEvents.GRASS_STEP, SoundSource.HOSTILE, 0.9F, 0.9F));
        }
        return true;
    }

    private static boolean chatGlitch(ServerPlayer p) {
        p.sendSystemMessage(Component.literal(CHAT_LINES[p.getRandom().nextInt(CHAT_LINES.length)]));
        return true;
    }

    private static boolean bell(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        double angle = p.getRandom().nextDouble() * Math.PI * 2.0D;
        level.playSound(null, p.getX() + Math.cos(angle) * 40.0D, p.getY(), p.getZ() + Math.sin(angle) * 40.0D,
                SoundEvents.BELL_BLOCK, SoundSource.AMBIENT, 2.0F, 0.5F);
        return true;
    }

    private static boolean ashFall(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        for (int i = 0; i < 30; i++) {
            Scheduler.later(i * 4, () -> level.sendParticles(ParticleTypes.WHITE_ASH,
                    p.getX(), p.getY() + 5.0D, p.getZ(), 12, 5.0D, 2.0D, 5.0D, 0.02D));
        }
        return true;
    }

    private static boolean mobGroan(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        SoundEvent[] groans = {SoundEvents.ZOMBIE_AMBIENT, SoundEvents.SKELETON_AMBIENT, SoundEvents.ZOMBIE_VILLAGER_AMBIENT};
        double angle = p.getRandom().nextDouble() * Math.PI * 2.0D;
        double x = p.getX() + Math.cos(angle) * 13.0D;
        double z = p.getZ() + Math.sin(angle) * 13.0D;
        for (int i = 0; i < 3; i++) {
            SoundEvent sound = groans[p.getRandom().nextInt(groans.length)];
            Scheduler.later(i * 30, () -> level.playSound(null, x, p.getY(), z, sound, SoundSource.HOSTILE, 1.0F, 0.7F));
        }
        return true;
    }

    private static boolean thunder(ServerPlayer p) {
        if (!Config.FLASHES.get()) return false;
        ServerLevel level = (ServerLevel) p.level();
        double angle = p.getRandom().nextDouble() * Math.PI * 2.0D;
        int x = Mth.floor(p.getX() + Math.cos(angle) * (30 + p.getRandom().nextInt(15)));
        int z = Mth.floor(p.getZ() + Math.sin(angle) * (30 + p.getRandom().nextInt(15)));
        if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) return false;

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return false;
        bolt.setVisualOnly(true);
        bolt.moveTo(x + 0.5D, y, z + 0.5D);
        level.addFreshEntity(bolt);
        return true;
    }

    private static boolean blink(ServerPlayer p) {
        if (!Config.FLASHES.get()) return false;
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, false));
        Sfx.at(p, ModSounds.STATIC_BURST, 0.6F, 1.2F);
        return true;
    }

    private static boolean lostNote(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
        RandomSource rnd = p.getRandom();
        double angle = rnd.nextDouble() * Math.PI * 2.0D;
        double dist = 10.0D + rnd.nextInt(7);
        int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
        int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
        if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) return false;

        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, p.getBlockY(), z));
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (Math.abs(pos.getY() - p.getBlockY()) > 6) return false;

        ItemStack book = rnd.nextBoolean()
                ? Corruption.makeBook(rnd)
                : Lore.siteBook(rnd.nextInt(Lore.SITE_NOTES.size()), p.getName().getString());
        Sites.lectern(level, pos, Direction.from2DDataValue(rnd.nextInt(4)), book);
        return true;
    }

    private static boolean mannequin(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
        RandomSource rnd = p.getRandom();
        double angle = rnd.nextDouble() * Math.PI * 2.0D;
        double dist = 18.0D + rnd.nextInt(9);
        int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
        int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
        if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) return false;

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) return false;

        ArmorStand stand = new ArmorStand(level, x + 0.5D, y, z + 0.5D);
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
        float yaw = (float) (Math.toDegrees(Math.atan2(p.getZ() - (z + 0.5D), p.getX() - (x + 0.5D))) - 90.0D);
        stand.moveTo(x + 0.5D, y, z + 0.5D, yaw, 0.0F);
        level.addFreshEntity(stand);
        Scheduler.later(6000, () -> {
            if (stand.isAlive()) stand.discard();
        });
        return true;
    }

    private static boolean itemGift(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        RandomSource rnd = p.getRandom();
        ItemEntity item = new ItemEntity(level, p.getX() + (rnd.nextDouble() - 0.5D) * 2.0D, p.getY() + 0.6D,
                p.getZ() + (rnd.nextDouble() - 0.5D) * 2.0D, new ItemStack(ModItems.SHARD.get()));
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);
        Sfx.at(p, ModSounds.GLITCH, 0.4F, 1.4F);
        return true;
    }

    private static boolean nameCall(ServerPlayer p) {
        p.sendSystemMessage(Component.literal("<K.> " + p.getName().getString()));
        Sfx.at(p, ModSounds.WHISPER, 0.8F, 0.8F);
        return true;
    }

    private static boolean desktopFile(ServerPlayer p) {
        if (!Config.DESKTOP_FILES.get()) return false;
        String[] pool = {"extra_do_not_open", "extra_kosiw", "extra_resignation", "extra_rent", "extra_lost", "extra_static"};
        Net.desktop(p, pool[p.getRandom().nextInt(pool.length)]);
        return true;
    }

    private static boolean moths(ServerPlayer p) {
        boolean any = false;
        for (int i = 0; i < 4; i++) {
            if (Spawns.near(p, ModEntities.STATIC_MOTH.get(), 10.0D + i * 2.0D) != null) any = true;
        }
        return any;
    }

    private static boolean lamplighter(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        if (LamplighterEntity.nearestSite(level, p.position()) == null) return false;
        return Spawns.near(p, ModEntities.LAMPLIGHTER.get(), 14.0D) != null;
    }

    private static boolean hungerPang(ServerPlayer p) {
        net.minecraft.world.food.FoodData food = p.getFoodData();
        if (food.getFoodLevel() <= 6) return false;
        food.setFoodLevel(food.getFoodLevel() - 6);
        p.displayClientMessage(Component.literal("you feel hollow."), true);
        Sfx.at(p, ModSounds.HEARTBEAT, 0.5F, 0.8F);
        return true;
    }

    private static boolean storm(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD || level.isThundering()) return false;
        level.setWeatherParameters(0, 3600, true, true);
        Sfx.at(p, ModSounds.DRONE, 0.7F, 0.7F);
        return true;
    }

    private static boolean nightFalls(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
        long t = level.getDayTime();
        long inDay = t % 24000L;
        if (inDay >= 12500L) return false;
        level.setDayTime(t - inDay + 13200L);
        p.displayClientMessage(Component.literal("the sun was never there."), true);
        Sfx.at(p, ModSounds.DRONE, 0.9F, 0.6F);
        return true;
    }

    private static boolean fakeDeath(ServerPlayer p) {
        String[] causes = {
            "was slain by ?????",
            "fell out of the world",
            "was killed by [Intentional Game Design]",
            "suffocated in a wall that was not there",
            "was pricked to death by a cactus that was not a cactus"
        };
        p.sendSystemMessage(Component.literal(p.getName().getString() + " " + causes[p.getRandom().nextInt(causes.length)]));
        Sfx.at(p, ModSounds.HEARTBEAT, 0.8F, 1.2F);
        return true;
    }

    private static boolean wretchPack(ServerPlayer p) {
        int n = 2 + p.getRandom().nextInt(2);
        boolean any = false;
        for (int i = 0; i < n; i++) {
            if (Spawns.near(p, ModEntities.WRETCH.get(), 18.0D + i * 3.0D) != null) any = true;
        }
        if (any) Sfx.at(p, ModSounds.DRONE, 0.8F, 0.6F);
        return any;
    }

    private static boolean hollowPack(ServerPlayer p) {
        boolean a = Spawns.near(p, ModEntities.HOLLOW.get(), 24.0D) != null;
        boolean b = Spawns.near(p, ModEntities.HOLLOW.get(), 31.0D) != null;
        if (a || b) Sfx.at(p, ModSounds.HEARTBEAT, 0.9F, 0.9F);
        return a || b;
    }

    private static boolean pickpocket(ServerPlayer p) {
        net.minecraft.world.entity.player.Inventory inv = p.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty()) continue;
            if (st.is(ModItems.CODEX.get()) || st.is(ModItems.JOURNAL.get())) continue;
            slots.add(i);
        }
        if (slots.isEmpty()) return false;

        ServerLevel level = (ServerLevel) p.level();
        RandomSource rnd = p.getRandom();
        int slot = slots.get(rnd.nextInt(slots.size()));
        ItemStack stack = inv.getItem(slot).copy();
        inv.setItem(slot, ItemStack.EMPTY);

        double angle = rnd.nextDouble() * Math.PI * 2.0D;
        double dist = 8.0D + rnd.nextInt(5);
        int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
        int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        ItemEntity item = new ItemEntity(level, x + 0.5D, y + 0.5D, z + 0.5D, stack);
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);

        p.sendSystemMessage(Component.literal("<???> found this."));
        Sfx.at(p, ModSounds.WHISPER, 0.7F, 1.0F);
        return true;
    }

    private static boolean healthDrain(ServerPlayer p) {
        if (p.getHealth() <= 8.0F) return false;
        p.hurt(p.level().damageSources().magic(), 3.0F);
        Sfx.at(p, ModSounds.HEARTBEAT, 0.8F, 0.7F);
        return true;
    }

    private static boolean structureCluster(ServerPlayer p) {
        int built = 0;
        for (int i = 0; i < 3; i++) {
            if (Structures.spawnNear(p, 40 + i * 12, 70 + i * 12, false) != null) built++;
        }
        if (built > 0) Sfx.at(p, ModSounds.WHISPER, 0.7F, 0.7F);
        return built > 0;
    }

    private static boolean corruptStorm(ServerPlayer p) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (Corrupt.chunkNear(p, 30 + i * 10, 80 + i * 10)) n++;
        }
        if (n > 0) Sfx.at(p, ModSounds.GLITCH, 1.0F, 0.7F);
        return n > 0;
    }

    private static boolean swapHotbar(ServerPlayer p) {
        net.minecraft.world.entity.player.Inventory inv = p.getInventory();
        int a = p.getRandom().nextInt(9);
        int b = (a + 1 + p.getRandom().nextInt(8)) % 9;
        ItemStack sa = inv.getItem(a);
        ItemStack sb = inv.getItem(b);
        if (sa.isEmpty() && sb.isEmpty()) return false;
        inv.setItem(a, sb);
        inv.setItem(b, sa);
        p.displayClientMessage(Component.literal("your hands remember differently."), true);
        Sfx.at(p, ModSounds.TV_OFF, 0.6F, 1.0F);
        return true;
    }

    private static boolean staticStorm(ServerPlayer p) {
        for (int i = 0; i < 5; i++) {
            int step = i;
            Scheduler.later(i * 60, () -> {
                Sfx.at(p, ModSounds.STATIC_BURST, 0.9F, 0.8F + step * 0.1F);
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
            });
        }
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0, false, false));
        return true;
    }

    private static boolean displacement(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        RandomSource rnd = p.getRandom();
        for (int attempt = 0; attempt < 5; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = 12.0D + rnd.nextInt(5);
            int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
            if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) continue;
            if (Math.abs(y - p.getBlockY()) > 10) continue;
            p.teleportTo(level, x + 0.5D, y, z + 0.5D, p.getYRot(), p.getXRot());
            Sfx.at(p, ModSounds.TV_OFF, 0.8F, 0.9F);
            return true;
        }
        return false;
    }

    private static boolean tallPair(ServerPlayer p) {
        boolean a = Spawns.near(p, ModEntities.TALL_ONE.get(), 26.0D) != null;
        boolean b = Spawns.near(p, ModEntities.TALL_ONE.get(), 33.0D) != null;
        if (a || b) {
            Sfx.at(p, ModSounds.DRONE, 1.0F, 0.6F);
            Sfx.at(p, ModSounds.SCREECH, 0.8F, 0.7F);
        }
        return a || b;
    }

    private static boolean darken(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
        Sfx.at(p, ModSounds.SCREECH, 0.9F, 0.9F);
        Scheduler.later(15, () -> Sfx.at(p, ModSounds.STATIC_BURST, 0.8F, 1.0F));
        return true;
    }
}
