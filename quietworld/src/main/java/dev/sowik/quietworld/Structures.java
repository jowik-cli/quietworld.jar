package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Picks a spot and builds one of the ten structure types. */
public final class Structures {
    private Structures() {}

    public static final int TYPE_COUNT = 10;
    // 0-2 ritual sites, 3 camp, 4 doorway, 5 house without a door, 6 dormitory, 7 radio mast, 8 graves, 9 tower

    public static BlockPos spawnNear(ServerPlayer p, int minDist, int maxDist, boolean force) {
        return spawnNear(p, minDist, maxDist, force, -1);
    }

    /** Builds a structure min..max blocks from the player, centered in a chunk. forcedType -1 picks at random. */
    public static BlockPos spawnNear(ServerPlayer p, int minDist, int maxDist, boolean force, int forcedType) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return null;

        StoryState state = StoryState.of(p);
        if (!force && state.siteCount() >= Config.MAX_SITES.get()) return null;

        RandomSource rnd = p.getRandom();
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + rnd.nextInt(Math.max(1, maxDist - minDist));
            int cx = Mth.floor(p.getX() + Math.cos(angle) * dist) >> 4;
            int cz = Mth.floor(p.getZ() + Math.sin(angle) * dist) >> 4;
            int x = cx * 16 + 8;
            int z = cz * 16 + 8;

            level.getChunk(cx, cz); // loads or generates the chunk first
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos center = new BlockPos(x, y, z);
            if (!level.getFluidState(center.below()).isEmpty()) continue;
            if (y <= level.getSeaLevel()) continue;

            int type = forcedType >= 0 ? forcedType % TYPE_COUNT : rnd.nextInt(TYPE_COUNT);
            build(level, center, type, rnd, p.getName().getString());
            state.setSiteCount(state.siteCount() + 1);
            return center;
        }
        return null;
    }

    public static void build(ServerLevel level, BlockPos c, int type, RandomSource rnd, String player) {
        switch (type) {
            case 0:
            case 1:
            case 2:
                Rituals.build(level, c, type, rnd);
                break;
            case 3:
                Sites.camp(level, c, rnd, player);
                break;
            case 4:
                Sites.gate(level, c, rnd, SiteData.GATE);
                break;
            case 5:
                Sites.house(level, c, rnd, player);
                break;
            case 6:
                Sites.dormitory(level, c, rnd, player);
                break;
            case 7:
                Sites.mast(level, c, rnd, player);
                break;
            case 8:
                Sites.graves(level, c, rnd, player);
                break;
            default:
                Sites.tower(level, c, rnd, player);
                break;
        }
        if (type != 4 && type != 7) {
            SiteData.get(level.getServer()).add(new SiteData.Site(
                    level.dimension().location().toString(), c, SiteData.STRUCT_BASE + type));
        }
    }
}
