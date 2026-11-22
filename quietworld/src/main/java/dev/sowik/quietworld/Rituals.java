package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Builds small wrong-looking ritual sites out in the world. All three fit inside a single chunk. */
public final class Rituals {
    private Rituals() {}

    public static void build(ServerLevel level, BlockPos c, int variant, RandomSource rnd) {
        prepare(level, c, 6.0D, rnd);
        switch (variant) {
            case 0:
                candleRing(level, c, rnd);
                break;
            case 1:
                effigy(level, c, rnd);
                break;
            default:
                sunkenCircle(level, c, rnd);
                break;
        }
    }

    static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    /** Clears the space above and lays a bare, wrong-colored floor. */
    static void prepare(ServerLevel level, BlockPos c, double radius, RandomSource rnd) {
        int r = (int) Math.ceil(radius);
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) > radius) continue;
                for (int dy = 0; dy <= 7; dy++) {
                    set(level, c.offset(dx, dy, dz), air);
                }
                BlockPos ground = c.offset(dx, -1, dz);
                BlockState floor = rnd.nextInt(5) == 0 ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
                set(level, ground, floor);
                for (int dy = 1; dy <= 3; dy++) {
                    BlockPos below = ground.below(dy);
                    if (!level.getBlockState(below).canOcclude()) {
                        set(level, below, Blocks.DIRT.defaultBlockState());
                    }
                }
            }
        }
    }

    static BlockState skull(boolean wither, RandomSource rnd) {
        BlockState base = wither ? Blocks.WITHER_SKELETON_SKULL.defaultBlockState() : Blocks.SKELETON_SKULL.defaultBlockState();
        return base.setValue(SkullBlock.ROTATION, rnd.nextInt(16));
    }

    static BlockState candle(boolean red, RandomSource rnd) {
        BlockState base = red ? Blocks.RED_CANDLE.defaultBlockState() : Blocks.WHITE_CANDLE.defaultBlockState();
        return base.setValue(CandleBlock.CANDLES, 1 + rnd.nextInt(3)).setValue(CandleBlock.LIT, true);
    }

    /** Blackstone ring, skulls and red candles, crying obsidian in the middle, a lectern holding a corrupted book. */
    private static void candleRing(ServerLevel level, BlockPos c, RandomSource rnd) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                BlockPos g = c.offset(dx, -1, dz);
                if (d >= 3.4D && d <= 4.6D) {
                    set(level, g, rnd.nextInt(10) < 7
                            ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                            : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                } else if (d <= 1.2D) {
                    set(level, g, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                }
            }
        }
        for (int k = 0; k < 8; k++) {
            double angle = k * Math.PI / 4.0D;
            int dx = (int) Math.round(Math.cos(angle) * 4.0D);
            int dz = (int) Math.round(Math.sin(angle) * 4.0D);
            BlockPos top = c.offset(dx, 0, dz);
            set(level, top, k % 2 == 0 ? skull(false, rnd) : candle(true, rnd));
        }
        BlockState lectern = Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, Direction.from2DDataValue(rnd.nextInt(4)));
        set(level, c, lectern);
        LecternBlock.tryPlaceBook(null, level, c, lectern, Corruption.makeBook(rnd));
    }

    /** A hay-and-fence scarecrow with a carved head, ringed by candle-topped fence posts. */
    private static void effigy(ServerLevel level, BlockPos c, RandomSource rnd) {
        BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState();

        set(level, c, fence);
        set(level, c.above(1), hay);
        set(level, c.above(2), hay);
        set(level, c.above(2).east(), fence);
        set(level, c.above(2).west(), fence);
        set(level, c.above(3), Blocks.CARVED_PUMPKIN.defaultBlockState()
                .setValue(CarvedPumpkinBlock.FACING, Direction.from2DDataValue(rnd.nextInt(4))));

        for (int k = 0; k < 8; k++) {
            double angle = k * Math.PI / 4.0D;
            int dx = (int) Math.round(Math.cos(angle) * 4.0D);
            int dz = (int) Math.round(Math.sin(angle) * 4.0D);
            BlockPos post = c.offset(dx, 0, dz);
            set(level, post, fence);
            set(level, post.above(), fence);
            set(level, post.above(2), candle(false, rnd));
        }
        set(level, c.north(2), skull(false, rnd));
        set(level, c.south(2), skull(false, rnd));
    }

    /** A circle dug into the ground with a red carpet sigil, bone rim and a skull on a bone pillar. */
    private static void sunkenCircle(ServerLevel level, BlockPos c, RandomSource rnd) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                BlockPos g = c.offset(dx, -1, dz);
                if (d <= 3.2D) {
                    set(level, g, air);
                    set(level, g.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                    BlockPos support = g.below(2);
                    if (!level.getBlockState(support).canOcclude()) {
                        set(level, support, Blocks.DIRT.defaultBlockState());
                    }
                } else if (d >= 3.8D && d <= 4.8D) {
                    set(level, g, Blocks.BONE_BLOCK.defaultBlockState());
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 3.2D) continue;
                boolean sigil = d >= 2.1D || dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz);
                if (sigil) {
                    set(level, c.offset(dx, -1, dz), Blocks.RED_CARPET.defaultBlockState());
                }
            }
        }
        set(level, c.below(), Blocks.BONE_BLOCK.defaultBlockState());
        set(level, c, Blocks.BONE_BLOCK.defaultBlockState());
        set(level, c.above(), skull(true, rnd));
    }
}
