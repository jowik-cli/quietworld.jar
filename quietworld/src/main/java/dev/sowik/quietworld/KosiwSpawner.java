package dev.sowik.quietworld;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class KosiwSpawner {
    private KosiwSpawner() {}

    public static boolean spawn(ServerPlayer p, KosiwEntity.Mode mode) {
        ServerLevel level = (ServerLevel) p.level();
        if (level.dimension() != Level.OVERWORLD) return false;
        if (!level.getEntitiesOfClass(KosiwEntity.class, p.getBoundingBox().inflate(128.0D)).isEmpty()) return false;

        // not while the player is deep underground
        int playerGround = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getBlockX(), p.getBlockZ());
        if (playerGround - p.getBlockY() > 6) return false;

        RandomSource rnd = p.getRandom();
        double angle = rnd.nextDouble() * Math.PI * 2.0D;
        double dist = mode == KosiwEntity.Mode.SKY ? 22.0D + rnd.nextInt(14) : 9.0D + rnd.nextInt(5);
        int x = Mth.floor(p.getX() + Math.cos(angle) * dist);
        int z = Mth.floor(p.getZ() + Math.sin(angle) * dist);
        if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) return false;

        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        double y;
        if (mode == KosiwEntity.Mode.SKY) {
            y = Math.max(groundY, p.getBlockY()) + 38.0D;
        } else {
            if (!level.getFluidState(new BlockPos(x, groundY - 1, z)).isEmpty()) return false;
            if (Math.abs(groundY - p.getBlockY()) > 8) return false;
            y = groundY;
        }

        KosiwEntity kosiw = ModEntities.KOSIW.get().create(level);
        if (kosiw == null) return false;
        kosiw.moveTo(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
        kosiw.setMode(mode);
        level.addFreshEntity(kosiw);

        p.sendSystemMessage(Component.translatable("multiplayer.player.joined", Component.literal(KosiwEntity.NAME))
                .withStyle(ChatFormatting.YELLOW));
        Sfx.at(p, ModSounds.STATIC_BURST, 0.4F, 1.3F);
        return true;
    }
}
