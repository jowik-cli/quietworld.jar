package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;

public final class Spawns {
    private Spawns() {}

    /** Spawns a mob on the surface about `dist` blocks from the player. Returns null if no spot works. */
    public static <T extends Mob> T near(ServerPlayer p, EntityType<T> type, double dist) {
        ServerLevel level = (ServerLevel) p.level();
        RandomSource rnd = p.getRandom();
        for (int attempt = 0; attempt < 5; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
            if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) continue;

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) continue;
            if (Math.abs(y - p.getBlockY()) > 12) continue;

            T mob = type.create(level);
            if (mob == null) return null;
            mob.moveTo(x + 0.5D, y, z + 0.5D, rnd.nextFloat() * 360.0F, 0.0F);
            mob.setPersistenceRequired();
            level.addFreshEntity(mob);
            return mob;
        }
        return null;
    }
}
