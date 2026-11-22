package dev.sowik.quietworld;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Per-player checks for doorways, dimension timers and ambient horror in the special places. */
public final class Portals {
    private Portals() {}

    /** Runs every 10 ticks. */
    public static void tick(ServerPlayer p) {
        long now = p.level().getGameTime();
        StoryState s = StoryState.of(p);
        if (now < s.portalCooldown()) return;

        if (p.level().dimension() == Dims.HALL && s.hallSince() > 0L && now - s.hallSince() > 4800L) {
            Sfx.at(p, ModSounds.STATIC_BURST, 1.0F, 0.8F);
            Dims.returnHome(p);
            return;
        }

        String dim = p.level().dimension().location().toString();
        List<SiteData.Site> sites = new ArrayList<>(SiteData.get(p.server).sites());
        for (SiteData.Site site : sites) {
            if (!site.dim().equals(dim)) continue;
            double dx = p.getX() - (site.pos().getX() + 0.5D);
            double dz = p.getZ() - (site.pos().getZ() + 0.5D);
            double dy = p.getY() - site.pos().getY();
            if (dx * dx + dz * dz > 1.44D || Math.abs(dy) > 2.5D) continue;

            switch (site.type()) {
                case SiteData.GATE:
                    Dims.enterCopy(p);
                    return;
                case SiteData.RETURN:
                case SiteData.HALL_EXIT:
                    Dims.returnHome(p);
                    return;
                default:
                    break;
            }
        }
    }

    /** Runs every 5 seconds. */
    public static void ambient(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        RandomSource rnd = p.getRandom();
        ResourceKey<Level> dim = level.dimension();
        SiteData data = SiteData.get(p.server);
        String dimId = dim.location().toString();
        boolean radio = p.getMainHandItem().is(ModItems.STATIC_RADIO.get()) || p.getOffhandItem().is(ModItems.STATIC_RADIO.get());

        for (SiteData.Site s : data.sites()) {
            if (!s.dim().equals(dimId)) continue;
            double d2 = s.pos().distSqr(p.blockPosition());

            // walking up to a structure records it in the archive
            String entry = Codex.idForSite(s.type());
            if (entry != null && d2 < 24.0D * 24.0D) {
                Codex.unlock(p, entry);
            }

            // radio masts hum static
            if (s.type() == SiteData.MAST && d2 < 40.0D * 40.0D && rnd.nextInt(2) == 0) {
                Sfx.play(level, s.pos().getX() + 0.5D, s.pos().getY() + 5.0D, s.pos().getZ() + 0.5D,
                        ModSounds.STATIC_LONG, SoundSource.AMBIENT, 2.0F, 0.9F + rnd.nextFloat() * 0.2F);
            }

            // a held Static Radio crackles near any structure
            if (radio && s.type() != SiteData.HALL_EXIT && d2 < 48.0D * 48.0D && rnd.nextInt(2) == 0) {
                Sfx.at(p, ModSounds.STATIC_LONG, 0.25F, 1.5F);
            }
        }

        // corrupted chunks crackle and sometimes spawn a Glitch
        if (dim == Level.OVERWORLD && data.isCorrupted(p.chunkPosition().toLong())) {
            Codex.unlock(p, "event_corrupt_chunk");
            Sfx.at(p, ModSounds.STATIC_LONG, 0.5F, 1.0F);
            if (rnd.nextInt(12) == 0) {
                Spawns.near(p, ModEntities.GLITCH.get(), 8.0D);
            }
        }

        if (dim == Dims.HALL) {
            Sfx.at(p, ModSounds.DRONE, 0.5F, 0.8F);
            if (rnd.nextInt(4) == 0) Sfx.at(p, ModSounds.STATIC_LONG, 0.35F, 0.9F);
            if (rnd.nextInt(7) == 0
                    && level.getEntitiesOfClass(HollowEntity.class, p.getBoundingBox().inflate(40.0D)).size() < 2) {
                Halls.spawnHollow(level, p);
            }
        }

        if (dim == Dims.COPY) {
            if (rnd.nextInt(5) == 0) Sfx.at(p, ModSounds.DRONE, 0.6F, 0.7F);
            if (rnd.nextInt(10) == 0) Sfx.at(p, ModSounds.WHISPER, 0.6F, 0.9F);
            if (rnd.nextInt(5) == 0
                    && level.getEntitiesOfClass(CrawlerEntity.class, p.getBoundingBox().inflate(48.0D)).size() < 3) {
                Spawns.near(p, ModEntities.CRAWLER.get(), 24.0D);
            }
            if (rnd.nextInt(20) == 0
                    && level.getEntitiesOfClass(TenantEntity.class, p.getBoundingBox().inflate(64.0D)).isEmpty()) {
                Spawns.near(p, ModEntities.TENANT.get(), 25.0D);
            }
        }
    }
}
