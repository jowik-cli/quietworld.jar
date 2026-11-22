package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Turns terrain into missing-texture checkers, floating towers, and pits. */
public final class Corrupt {
    private Corrupt() {}

    private static final Block[] PALETTE = {
        Blocks.BLACK_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.WHITE_CONCRETE, Blocks.LIME_CONCRETE, Blocks.CYAN_CONCRETE
    };

    /** Corrupts a chunk 40-90 blocks away that has not been corrupted yet. */
    public static boolean chunkNear(ServerPlayer p, int minDist, int maxDist) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
        SiteData data = SiteData.get(p.server);
        if (data.corruptedCount() >= 14) return false;

        RandomSource rnd = p.getRandom();
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + rnd.nextInt(Math.max(1, maxDist - minDist));
            int cx = Mth.floor(p.getX() + Math.cos(angle) * dist) >> 4;
            int cz = Mth.floor(p.getZ() + Math.sin(angle) * dist) >> 4;
            long key = ChunkPos.asLong(cx, cz);
            if (data.isCorrupted(key)) continue;
            corruptChunk(level, cx, cz, rnd);
            data.addCorrupted(key);
            return true;
        }
        return false;
    }

    public static void corruptChunk(ServerLevel level, int cx, int cz, RandomSource rnd) {
        level.getChunk(cx, cz);
        BlockState black = Blocks.BLACK_CONCRETE.defaultBlockState();
        BlockState magenta = Blocks.MAGENTA_CONCRETE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = cx * 16 + lx;
                int z = cz * 16 + lz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos top = new BlockPos(x, y - 1, z);
                if (!level.getBlockState(top).getFluidState().isEmpty()) continue;

                double r = rnd.nextDouble();
                if (r < 0.45D) {
                    level.setBlock(top.above(), air, 2);
                    level.setBlock(top, (((lx >> 1) + (lz >> 1)) & 1) == 0 ? magenta : black, 2);
                } else if (r < 0.52D) {
                    int h = 2 + rnd.nextInt(5);
                    for (int i = 1; i <= h; i++) {
                        level.setBlock(top.above(i), PALETTE[rnd.nextInt(PALETTE.length)].defaultBlockState(), 2);
                    }
                } else if (r < 0.57D) {
                    for (int i = 0; i < 3; i++) {
                        level.setBlock(top.below(i), air, 2);
                    }
                }
            }
        }
    }

    /** Sprinkles a few corrupted blocks on the surface around a point (the Glitch leaves these behind). */
    public static void scatter(ServerLevel level, BlockPos around, int radius, int count, RandomSource rnd) {
        for (int i = 0; i < count; i++) {
            int x = around.getX() + rnd.nextInt(radius * 2 + 1) - radius;
            int z = around.getZ() + rnd.nextInt(radius * 2 + 1) - radius;
            if (!level.hasChunkAt(new BlockPos(x, around.getY(), z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos top = new BlockPos(x, y - 1, z);
            if (!level.getBlockState(top).getFluidState().isEmpty()) continue;
            level.setBlock(top, PALETTE[rnd.nextInt(PALETTE.length)].defaultBlockState(), 2);
        }
    }
}
