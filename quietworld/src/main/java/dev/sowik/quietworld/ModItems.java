package dev.sowik.quietworld;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Quietworld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Quietworld.MODID);

    public static final RegistryObject<Item> CODEX = ITEMS.register("codex", () -> new CodexItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JOURNAL = ITEMS.register("journal", () -> new JournalItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STATIC_RADIO = ITEMS.register("static_radio", () -> new RadioItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHARD = ITEMS.register("corrupted_shard", () -> new ShardItem(new Item.Properties().stacksTo(16)));

    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CODEX.get());
            event.accept(JOURNAL.get());
            event.accept(STATIC_RADIO.get());
            event.accept(SHARD.get());
        }
    }

    /** Right-click to open the archive. */
    public static class CodexItem extends Item {
        public CodexItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openCodex());
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
    }

    /** Right-click to read every note you have found. */
    public static class JournalItem extends Item {
        public JournalItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openJournal());
            } else if (player instanceof ServerPlayer sp) {
                Journal.scan(sp);
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
    }

    /** Points toward the nearest structure and crackles when held near one. */
    public static class RadioItem extends Item {
        private static final String[] COMPASS = {"east", "south-east", "south", "south-west", "west", "north-west", "north", "north-east"};

        public RadioItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                Codex.unlock(sp, "item_radio");
                player.getCooldowns().addCooldown(this, 60);

                SiteData.Site best = null;
                double bestDist = 600.0D * 600.0D;
                String dim = level.dimension().location().toString();
                for (SiteData.Site s : SiteData.get(sp.server).sites()) {
                    if (!s.dim().equals(dim) || s.type() == SiteData.HALL_EXIT || s.type() == SiteData.RETURN) continue;
                    double dx = s.pos().getX() - sp.getX();
                    double dz = s.pos().getZ() - sp.getZ();
                    double d2 = dx * dx + dz * dz;
                    if (d2 < bestDist) {
                        bestDist = d2;
                        best = s;
                    }
                }

                if (best == null) {
                    sp.displayClientMessage(Component.literal("only static."), true);
                    Sfx.at(sp, ModSounds.STATIC_LONG, 0.4F, 1.4F);
                } else {
                    double dx = best.pos().getX() - sp.getX();
                    double dz = best.pos().getZ() - sp.getZ();
                    double dist = Math.sqrt(bestDist);
                    int index = (int) Math.floorMod(Math.round(Math.toDegrees(Math.atan2(dz, dx)) / 45.0D), 8);
                    String how = dist < 40.0D ? "very close" : dist < 120.0D ? "close" : dist < 300.0D ? "far" : "faint";
                    sp.displayClientMessage(Component.literal("the static leans " + COMPASS[index] + ". " + how + "."), true);
                    float pitch = dist < 40.0D ? 1.6F : dist < 120.0D ? 1.3F : 1.0F;
                    Sfx.at(sp, ModSounds.STATIC_LONG, 0.5F, pitch);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }

    /** Right-click to tear a hole in the quiet and trigger a random event. */
    public static class ShardItem extends Item {
        public ShardItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                Codex.unlock(sp, "item_shard");
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                Sfx.at(sp, ModSounds.GLITCH, 0.8F, 0.9F);
                sp.displayClientMessage(Component.literal("the quiet tears."), true);
                RandomEvents.roll(sp, StoryState.of(sp), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }
}
