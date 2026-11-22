package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Half;

/** The extra structures. Every one fits inside the chunk it is centered in. */
public final class Sites {
    private Sites() {}

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    private static void writeSign(ServerLevel level, BlockPos pos, BlockState state, String... lines) {
        if (level.getBlockEntity(pos) instanceof SignBlockEntity be) {
            Component[] msg = new Component[4];
            for (int i = 0; i < 4; i++) {
                msg[i] = Component.literal(i < lines.length ? lines[i] : "");
            }
            be.setText(new SignText(msg, msg, DyeColor.BLACK, false), true);
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static void wallSign(ServerLevel level, BlockPos pos, Direction facing, String... lines) {
        BlockState st = Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing);
        set(level, pos, st);
        writeSign(level, pos, st, lines);
    }

    static void lectern(ServerLevel level, BlockPos pos, Direction facing, ItemStack book) {
        BlockState st = Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, facing);
        set(level, pos, st);
        LecternBlock.tryPlaceBook(null, level, pos, st, book);
    }

    private static void register(ServerLevel level, BlockPos pos, int type) {
        SiteData.get(level.getServer()).add(new SiteData.Site(level.dimension().location().toString(), pos, type));
    }

    /** K.'s abandoned camp: unlit fire, a grey tent, a chest and a journal on a lectern. */
    public static void camp(ServerLevel level, BlockPos c, RandomSource rnd, String player) {
        Rituals.prepare(level, c, 6.5D, rnd);
        set(level, c, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        set(level, c.north(2), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        set(level, c.west(2), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        set(level, c.east(2), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        set(level, c.west(2).north(), Rituals.skull(false, rnd));

        BlockState wool = Blocks.GRAY_WOOL.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = 2; dz <= 6; dz++) {
                set(level, c.offset(dx, 2, dz), wool);
                boolean side = Math.abs(dx) == 2 || dz == 6;
                if (side) {
                    set(level, c.offset(dx, 0, dz), wool);
                    set(level, c.offset(dx, 1, dz), wool);
                } else if (dz >= 3) {
                    set(level, c.offset(dx, 0, dz), Blocks.WHITE_CARPET.defaultBlockState());
                }
            }
        }

        BlockPos chest = c.offset(-1, 0, 5);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity ch) {
            ch.setItem(0, new ItemStack(Items.COOKED_BEEF, 2));
            ch.setItem(1, Corruption.makeBook(rnd));
            ch.setItem(2, Lore.siteBook(Lore.SITE_LOST, player));
        }
        lectern(level, c.offset(1, 0, 5), Direction.NORTH, Lore.siteBook(Lore.SITE_CAMP, player));
    }

    /** A crying-obsidian doorway. Walking through it leads to the Hollow Copy (or back home from inside it). */
    public static void gate(ServerLevel level, BlockPos c, RandomSource rnd, int siteType) {
        Rituals.prepare(level, c, 4.0D, rnd);
        BlockState frame = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        for (int dx = -1; dx <= 2; dx++) {
            set(level, c.offset(dx, -1, 0), frame);
            set(level, c.offset(dx, 3, 0), frame);
        }
        for (int dy = 0; dy <= 2; dy++) {
            set(level, c.offset(-1, dy, 0), frame);
            set(level, c.offset(2, dy, 0), frame);
        }
        set(level, c.offset(-2, 0, 0), Rituals.skull(false, rnd));
        set(level, c.offset(3, 0, 0), Rituals.skull(false, rnd));

        BlockPos signPos = c.offset(0, 0, 2);
        BlockState sign = Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 0);
        set(level, signPos, sign);
        writeSign(level, signPos, sign, "THE DOOR", "is a picture", "of a door", "do not knock");

        register(level, c, siteType);
    }

    /** A house with no door. A bed, a lectern, a chest. */
    public static void house(ServerLevel level, BlockPos c, RandomSource rnd, String player) {
        Rituals.prepare(level, c, 6.0D, rnd);
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                set(level, c.offset(dx, -1, dz), planks);
                for (int dy = 0; dy <= 2; dy++) {
                    set(level, c.offset(dx, dy, dz), corner ? log : edge ? planks : air);
                }
                set(level, c.offset(dx, 3, dz), Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        set(level, c.offset(3, 1, 0), pane);
        set(level, c.offset(-3, 1, 0), pane);
        set(level, c.offset(0, 1, 3), pane);
        set(level, c.offset(0, 1, -3), pane);

        BlockPos foot = c.offset(-2, 0, -2);
        set(level, foot, Blocks.WHITE_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT));
        set(level, foot.south(), Blocks.WHITE_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD));
        set(level, c.offset(2, 0, 2), Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, c.offset(0, -1, 0), Blocks.RED_CARPET.defaultBlockState());

        BlockPos chest = c.offset(2, 0, -2);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity ch) {
            ch.setItem(0, new ItemStack(Items.COOKED_BEEF, 2));
            ch.setItem(1, new ItemStack(Items.DIAMOND, 1 + rnd.nextInt(4)));
        }
        lectern(level, c.offset(-2, 0, 2), Direction.EAST, Lore.siteBook(Lore.SITE_HOUSE, player));
    }

    /** An underground room of seven made beds, each with a sign carrying the player's name. */
    public static void dormitory(ServerLevel level, BlockPos c, RandomSource rnd, String player) {
        Rituals.prepare(level, c, 6.0D, rnd);
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState floor = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -6; dy <= -2; dy++) {
                    boolean edge = Math.abs(dx) == 5 || Math.abs(dz) == 3 || dy == -2;
                    set(level, c.offset(dx, dy, dz), dy == -6 ? floor : edge ? bricks : air);
                }
            }
        }
        for (int dy = -5; dy <= -2; dy++) {
            set(level, c.offset(0, dy, -2), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        set(level, c.offset(0, -1, -2), Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM));

        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        set(level, c.offset(-3, -3, -1), lantern);
        set(level, c.offset(3, -3, -1), lantern);
        set(level, c.offset(0, -3, 2), lantern);

        for (int i = 0; i < 7; i++) {
            BlockPos foot = c.offset(-3 + i, -5, 0);
            set(level, foot, Blocks.WHITE_BED.defaultBlockState()
                    .setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT));
            set(level, foot.south(), Blocks.WHITE_BED.defaultBlockState()
                    .setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD));
            wallSign(level, c.offset(-3 + i, -4, 2), Direction.NORTH, player, "slept here", "night " + (i + 1));
        }
        lectern(level, c.offset(3, -5, -2), Direction.SOUTH, Lore.siteBook(Lore.SITE_DORM, player));
    }

    /** A tall iron mast next to a small bunker. It hums static for anyone nearby. */
    public static void mast(ServerLevel level, BlockPos c, RandomSource rnd, String player) {
        Rituals.prepare(level, c, 6.5D, rnd);
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, c.offset(dx, -1, dz), bricks);
            }
        }
        for (int y = 0; y <= 14; y++) {
            set(level, c.above(y), Blocks.IRON_BARS.defaultBlockState());
        }
        set(level, c.above(15), Blocks.LIGHTNING_ROD.defaultBlockState());

        for (int dx = 2; dx <= 6; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                set(level, c.offset(dx, -1, dz), bricks);
                for (int dy = 0; dy <= 3; dy++) {
                    boolean wall = dx == 2 || dx == 6 || dz == -2 || dz == 2;
                    set(level, c.offset(dx, dy, dz), (wall || dy == 3) ? bricks : air);
                }
            }
        }
        set(level, c.offset(4, 0, 2), air);
        set(level, c.offset(4, 1, 2), air);
        set(level, c.offset(3, 0, -1), Blocks.NOTE_BLOCK.defaultBlockState());
        lectern(level, c.offset(5, 0, -1), Direction.SOUTH, Lore.siteBook(Lore.SITE_MAST, player));

        register(level, c, SiteData.MAST);
    }

    /** Three rows of graves. One of them has the player's name on it. */
    public static void graves(ServerLevel level, BlockPos c, RandomSource rnd, String player) {
        Rituals.prepare(level, c, 6.5D, rnd);
        String[] names = {"K.", "kosiw", "Guest_0", "unknown", "Herobrine", "Entity 303", "Null", "Player"};
        int special = rnd.nextInt(15);
        int index = 0;
        for (int row = -4; row <= 4; row += 4) {
            for (int dx = -4; dx <= 4; dx += 2) {
                BlockPos stone = c.offset(dx, 0, row);
                set(level, stone, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, stone.above(), Blocks.STONE_BRICKS.defaultBlockState());
                if (index == special) {
                    wallSign(level, stone.south(), Direction.SOUTH, player, "day ?", "(not yet)");
                } else {
                    String name = Corruption.garble(names[rnd.nextInt(names.length)], rnd, 0.12D);
                    wallSign(level, stone.south(), Direction.SOUTH, name, "day " + (1 + rnd.nextInt(60)));
                }
                index++;
            }
        }
    }

    /** A hollow stone tower with a ladder, a railing and a lookout journal at the top. */
    public static void tower(ServerLevel level, BlockPos c, RandomSource rnd, String player) {
        Rituals.prepare(level, c, 5.0D, rnd);
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dy = 0; dy <= 11; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    set(level, c.offset(dx, dy, dz), edge ? (rnd.nextInt(4) == 0 ? mossy : bricks) : air);
                }
            }
        }
        set(level, c.offset(0, 0, 2), air);
        set(level, c.offset(0, 1, 2), air);

        for (int dy = 0; dy <= 12; dy++) {
            set(level, c.offset(0, dy, -1), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == -1) continue;
                set(level, c.offset(dx, 12, dz), bricks);
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    set(level, c.offset(dx, 13, dz), Blocks.STONE_BRICK_WALL.defaultBlockState());
                }
            }
        }
        set(level, c.offset(-1, 13, 1), Blocks.SOUL_CAMPFIRE.defaultBlockState());
        lectern(level, c.offset(1, 13, 1), Direction.WEST, Lore.siteBook(Lore.SITE_TOWER, player));
    }
}
