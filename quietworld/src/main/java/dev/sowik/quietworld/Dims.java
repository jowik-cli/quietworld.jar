package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Travel between the overworld, the Static Hall and the Hollow Copy. */
public final class Dims {
    private Dims() {}

    public static final ResourceKey<Level> HALL =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(Quietworld.MODID, "static_hall"));
    public static final ResourceKey<Level> COPY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(Quietworld.MODID, "hollow_copy"));

    private static void rememberHome(ServerPlayer p, StoryState s) {
        if (p.level().dimension() == Level.OVERWORLD) {
            s.setHome(p.getX(), p.getY(), p.getZ());
        }
    }

    public static void give(ServerPlayer p, ItemStack stack) {
        if (!p.getInventory().add(stack)) {
            p.drop(stack, false);
        }
    }

    /** Sends the player to the Static Hall. Returns false if the dimension is not loaded in this world. */
    public static boolean enterHall(ServerPlayer p) {
        MinecraftServer server = p.server;
        ServerLevel hall = server.getLevel(HALL);
        if (hall == null) return false;

        StoryState s = StoryState.of(p);
        rememberHome(p, s);

        SiteData data = SiteData.get(server);
        if (!data.hallBuilt()) {
            Halls.build(hall);
            data.setHallBuilt(true);
        }

        long now = p.level().getGameTime();
        s.setPortalCooldown(now + 100L);
        s.setHallSince(now);
        s.raisePhaseFloor(1);
        p.teleportTo(hall, 0.5D, 3.0D, 0.5D, p.getYRot(), 0.0F);
        Sfx.at(p, ModSounds.STATIC_BURST, 1.0F, 0.8F);
        Codex.unlock(p, "dim_hall");

        if (!s.flag("hallNote")) {
            s.setFlag("hallNote");
            Scheduler.later(100, () -> give(p, Lore.siteBook(Lore.SITE_HALL, p.getName().getString())));
        }
        return true;
    }

    /** Sends the player to the Hollow Copy, building a return doorway where they arrive. */
    public static boolean enterCopy(ServerPlayer p) {
        MinecraftServer server = p.server;
        ServerLevel copy = server.getLevel(COPY);
        if (copy == null) return false;

        StoryState s = StoryState.of(p);
        rememberHome(p, s);

        int bx = Mth.floor(p.getX());
        int bz = Mth.floor(p.getZ());
        copy.getChunk(bx >> 4, bz >> 4);
        int gy = copy.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        Sites.gate(copy, new BlockPos(bx, gy, bz), copy.getRandom(), SiteData.RETURN);

        s.setPortalCooldown(p.level().getGameTime() + 100L);
        s.raisePhaseFloor(2);
        p.teleportTo(copy, bx + 0.5D, gy, bz + 3.5D, 180.0F, 0.0F);
        Sfx.at(p, ModSounds.DRONE, 1.0F, 0.7F);
        Codex.unlock(p, "dim_copy");
        return true;
    }

    public static void returnHome(ServerPlayer p) {
        ServerLevel dest = p.server.overworld();
        StoryState s = StoryState.of(p);

        double x;
        double y;
        double z;
        if (s.hasHome()) {
            x = s.homeX();
            y = s.homeY();
            z = s.homeZ();
        } else {
            BlockPos sp = dest.getSharedSpawnPos();
            x = sp.getX() + 0.5D;
            z = sp.getZ() + 0.5D;
            y = dest.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sp.getX(), sp.getZ());
        }
        dest.getChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4);

        s.setPortalCooldown(p.level().getGameTime() + 200L);
        s.setHallSince(0L);
        p.teleportTo(dest, x, y, z, p.getYRot(), 0.0F);
    }
}
