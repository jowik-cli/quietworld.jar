package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Builds the Static Hall: a 5x5 maze of grey rooms under a ceiling of cold lamps, with one door out. */
public final class Halls {
    private Halls() {}

    private static final int ORIGIN = -22;
    private static final int CELL = 9;
    private static final int N = 5;
    private static final int MAX = ORIGIN + CELL * N; // 23

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    public static void build(ServerLevel level) {
        RandomSource rnd = level.getRandom();
        BlockState wall = Blocks.GRAY_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState ceiling = Blocks.SMOOTH_STONE.defaultBlockState();

        for (int x = ORIGIN; x <= MAX; x++) {
            for (int z = ORIGIN; z <= MAX; z++) {
                set(level, new BlockPos(x, 2, z), floor);
                set(level, new BlockPos(x, 8, z), ceiling);
            }
        }
        for (int k = 0; k <= N; k++) {
            int line = ORIGIN + CELL * k;
            for (int t = ORIGIN; t <= MAX; t++) {
                for (int y = 3; y <= 7; y++) {
                    set(level, new BlockPos(line, y, t), wall);
                    set(level, new BlockPos(t, y, line), wall);
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int cx = ORIGIN + CELL * i + 4;
                int cz = ORIGIN + CELL * j + 4;
                set(level, new BlockPos(cx, 8, cz), Blocks.SEA_LANTERN.defaultBlockState());
            }
        }

        boolean[][] seen = new boolean[N][N];
        dfs(level, rnd, 2, 2, seen);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i + 1 < N && rnd.nextInt(5) == 0) carve(level, i, j, i + 1, j);
                if (j + 1 < N && rnd.nextInt(5) == 0) carve(level, i, j, i, j + 1);
            }
        }

        // the way out: a lone door in the far corner room
        int doorX = ORIGIN + CELL * 4 + 4;
        BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        set(level, new BlockPos(doorX, 3, MAX), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, new BlockPos(doorX, 4, MAX), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        SiteData.get(level.getServer()).add(new SiteData.Site(
                Dims.HALL.location().toString(), new BlockPos(doorX, 3, MAX - 1), SiteData.HALL_EXIT));

        // rules, left on a lectern in the starting room
        BlockPos lecternPos = new BlockPos(3, 3, 3);
        BlockState lectern = Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST);
        set(level, lecternPos, lectern);
        LecternBlock.tryPlaceBook(null, level, lecternPos, lectern, Lore.siteBook(Lore.SITE_HALL, "you"));
    }

    private static void dfs(ServerLevel level, RandomSource rnd, int i, int j, boolean[][] seen) {
        seen[i][j] = true;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int a = 3; a > 0; a--) {
            int b = rnd.nextInt(a + 1);
            int[] tmp = dirs[a];
            dirs[a] = dirs[b];
            dirs[b] = tmp;
        }
        for (int[] d : dirs) {
            int ni = i + d[0];
            int nj = j + d[1];
            if (ni < 0 || nj < 0 || ni >= N || nj >= N || seen[ni][nj]) continue;
            carve(level, i, j, ni, nj);
            dfs(level, rnd, ni, nj, seen);
        }
    }

    /** Cuts a 2-wide, 3-tall doorway through the wall between two neighbouring cells. */
    private static void carve(ServerLevel level, int i, int j, int ni, int nj) {
        BlockState air = Blocks.AIR.defaultBlockState();
        if (ni != i) {
            int wallX = ORIGIN + CELL * Math.max(i, ni);
            int zMid = ORIGIN + CELL * j + 4;
            for (int dz = 0; dz <= 1; dz++) {
                for (int y = 3; y <= 5; y++) {
                    set(level, new BlockPos(wallX, y, zMid + dz), air);
                }
            }
        } else {
            int wallZ = ORIGIN + CELL * Math.max(j, nj);
            int xMid = ORIGIN + CELL * i + 4;
            for (int dx = 0; dx <= 1; dx++) {
                for (int y = 3; y <= 5; y++) {
                    set(level, new BlockPos(xMid + dx, y, wallZ), air);
                }
            }
        }
    }

    /** A Hollow touch inside the hall throws the player into a random room. */
    public static void scatter(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        RandomSource rnd = p.getRandom();
        int i = rnd.nextInt(N);
        int j = rnd.nextInt(N);
        double x = ORIGIN + CELL * i + 4 + 0.5D;
        double z = ORIGIN + CELL * j + 4 + 0.5D;
        p.teleportTo(level, x, 3.0D, z, p.getYRot(), 0.0F);
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
        Sfx.at(p, ModSounds.STATIC_BURST, 1.0F, 1.1F);
    }

    public static void spawnHollow(ServerLevel level, ServerPlayer p) {
        RandomSource rnd = p.getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = 6.0D + rnd.nextInt(8);
            int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
            if (x <= ORIGIN || x >= MAX || z <= ORIGIN || z >= MAX) continue;
            BlockPos pos = new BlockPos(x, 3, z);
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                    || !level.getBlockState(pos.above(2)).isAir()) continue;

            HollowEntity hollow = ModEntities.HOLLOW.get().create(level);
            if (hollow == null) return;
            hollow.moveTo(x + 0.5D, 3.0D, z + 0.5D, rnd.nextFloat() * 360.0F, 0.0F);
            hollow.setPersistenceRequired();
            level.addFreshEntity(hollow);
            return;
        }
    }
}
