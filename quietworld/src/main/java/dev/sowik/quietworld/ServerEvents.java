package dev.sowik.quietworld;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = Quietworld.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Net.sync(player, "", "");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (player.tickCount % 10 == 0) {
            Portals.tick(player);
        }
        if (player.tickCount % 20 != 0) return;

        StoryState state = StoryState.of(player);
        long now = player.level().getGameTime();

        if (!state.started()) {
            state.start(StoryState.currentDay(player), now);
            return;
        }

        // About 20 seconds in: the first note appears in the inventory and something is already watching.
        if (!state.introDone() && now - state.startTick() >= 400L) {
            StoryState.deliverNext(player, null, true);
            RandomEvents.spawnWatcher(player, 40.0D);
            state.setIntroDone(true);
            return;
        }

        // The Codex and the Journal arrive a moment after the first note (and for worlds started before this update).
        if (state.introDone() && !state.flag("gotBooks")) {
            state.setFlag("gotBooks");
            giveBooks(player);
        }

        if (player.tickCount % 100 == 0) {
            Journal.scan(player);
            Portals.ambient(player);
        }
        if (player.tickCount % 200 == 0) {
            int phase = state.phase(player);
            int announced = state.announcedPhase();
            if (announced < 0) {
                state.setAnnouncedPhase(phase);
            } else if (phase > announced) {
                state.setAnnouncedPhase(phase);
                Phases.onChange(player, phase);
            }
            for (int i = 1; i <= phase; i++) {
                Codex.unlock(player, "story_phase" + i);
            }
            if (state.syncedPhase() != phase) {
                Net.sync(player, "", "");
            }
            if (state.introDone() && Config.EVENTS_ENABLED.get()) {
                RandomEvents.roll(player, state, false);
            }
        }
    }

    private static void giveBooks(ServerPlayer player) {
        Dims.give(player, new ItemStack(ModItems.CODEX.get()));
        Dims.give(player, new ItemStack(ModItems.JOURNAL.get()));
        Codex.unlock(player, "item_codex");
        Codex.unlock(player, "item_journal");
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!StoryState.of(player).started()) return;
        StoryState.deliverNext(player, player.position(), false);
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getStack();
        if (stack.is(ModItems.SHARD.get())) {
            Codex.unlock(player, "item_shard");
        } else if (stack.is(ModItems.STATIC_RADIO.get())) {
            Codex.unlock(player, "item_radio");
        }
        Journal.collectFrom(player, stack);
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getCrafting();
        if (stack.is(ModItems.STATIC_RADIO.get())) {
            Codex.unlock(player, "item_radio");
        } else if (stack.is(ModItems.CODEX.get())) {
            Codex.unlock(player, "item_codex");
        } else if (stack.is(ModItems.JOURNAL.get())) {
            Codex.unlock(player, "item_journal");
        }
    }

    /** Reading a book on a lectern copies it into the Journal. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof LecternBlockEntity lectern) {
            Journal.collectFrom(player, lectern.getBook());
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String text = event.getRawText();

        EchoEntity.record(player, text);
        EchoEntity.hear(player, text);

        boolean addressed = text.toLowerCase(Locale.ROOT).contains("kosiw");
        if (!Dialogue.kosiwNear(player, addressed ? 128.0D : 24.0D)) return;
        Dialogue.reply(player, text);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        // Used by the clickable questions; anyone can run it, and it only works with kosiw close by.
        event.getDispatcher().register(Commands.literal("quietask")
            .then(Commands.argument("topic", StringArgumentType.word()).executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                Dialogue.ask(p, StringArgumentType.getString(ctx, "topic"));
                return 1;
            })));

        event.getDispatcher().register(Commands.literal("quiet")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("note").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                boolean ok = StoryState.deliverNext(p, null, true);
                ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Gave the next note." : "No note available for this phase."), false);
                return ok ? 1 : 0;
            }))
            .then(Commands.literal("event").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                boolean ok = RandomEvents.roll(p, StoryState.of(p), true);
                ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Event fired." : "Event could not fire here."), false);
                return ok ? 1 : 0;
            }))
            .then(Commands.literal("watcher").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                boolean ok = RandomEvents.spawnWatcher(p, 25.0D);
                ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Watcher spawned." : "Could not spawn a Watcher here."), false);
                return ok ? 1 : 0;
            }))
            .then(Commands.literal("kosiw")
                .then(Commands.literal("sky").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    boolean ok = KosiwSpawner.spawn(p, KosiwEntity.Mode.SKY);
                    ctx.getSource().sendSuccess(() -> Component.literal(ok ? "kosiw is in the sky." : "Could not place kosiw here."), false);
                    return ok ? 1 : 0;
                }))
                .then(Commands.literal("visit").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    boolean ok = KosiwSpawner.spawn(p, KosiwEntity.Mode.VISIT);
                    ctx.getSource().sendSuccess(() -> Component.literal(ok ? "kosiw is nearby." : "Could not place kosiw here."), false);
                    return ok ? 1 : 0;
                })))
            .then(Commands.literal("mob")
                .then(Commands.argument("kind", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String kind = StringArgumentType.getString(ctx, "kind");
                    boolean ok = RandomEvents.spawnMob(p, kind);
                    ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Spawned " + kind + "."
                            : "Could not spawn '" + kind + "' (hollow, glitch, echo, crawler, tall, tenant, gazer, moth, lamplighter, archivist, wretch, child)."), false);
                    return ok ? 1 : 0;
                })))
            .then(Commands.literal("structure")
                .executes(ctx -> buildStructure(ctx.getSource().getPlayerOrException(), -1, ctx.getSource()))
                .then(Commands.argument("type", IntegerArgumentType.integer(0, Structures.TYPE_COUNT - 1)).executes(ctx ->
                    buildStructure(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "type"), ctx.getSource()))))
            .then(Commands.literal("corrupt").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                ServerLevel level = (ServerLevel) p.level();
                Corrupt.corruptChunk(level, p.chunkPosition().x, p.chunkPosition().z, p.getRandom());
                SiteData.get(p.server).addCorrupted(p.chunkPosition().toLong());
                ctx.getSource().sendSuccess(() -> Component.literal("This chunk is corrupted."), false);
                return 1;
            }))
            .then(Commands.literal("hall").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                boolean ok = Dims.enterHall(p);
                ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Entering the Static Hall."
                        : "The Static Hall dimension is not loaded in this world."), false);
                return ok ? 1 : 0;
            }))
            .then(Commands.literal("copy").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                boolean ok = Dims.enterCopy(p);
                ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Entering the Hollow Copy."
                        : "The Hollow Copy dimension is not loaded in this world."), false);
                return ok ? 1 : 0;
            }))
            .then(Commands.literal("home").executes(ctx -> {
                Dims.returnHome(ctx.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("items").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                giveBooks(p);
                Dims.give(p, new ItemStack(ModItems.STATIC_RADIO.get()));
                Dims.give(p, new ItemStack(ModItems.SHARD.get(), 4));
                ctx.getSource().sendSuccess(() -> Component.literal("Gave Codex, Journal, Static Radio and 4 shards."), false);
                return 1;
            }))
            .then(Commands.literal("unlock")
                .then(Commands.argument("id", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String id = StringArgumentType.getString(ctx, "id");
                    if (id.equals("all")) {
                        for (Codex.Entry e : Codex.all()) {
                            Codex.unlock(p, e.id());
                        }
                    } else if (Codex.get(id) == null) {
                        ctx.getSource().sendSuccess(() -> Component.literal("Unknown id. Use 'all' or an id like mob_hollow."), false);
                        return 0;
                    } else {
                        Codex.unlock(p, id);
                    }
                    return 1;
                })))
            .then(Commands.literal("sync").executes(ctx -> {
                Net.sync(ctx.getSource().getPlayerOrException(), "", "");
                return 1;
            }))
            .then(Commands.literal("sound")
                .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    RegistryObject<SoundEvent> sound = ModSounds.byName(StringArgumentType.getString(ctx, "name"));
                    if (sound == null) {
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                "Sounds: static_burst static_long drone screech heartbeat whisper glitch tv_off"), false);
                        return 0;
                    }
                    Sfx.at(p, sound, 1.0F, 1.0F);
                    return 1;
                })))
            .then(Commands.literal("phase").then(Commands.argument("n", IntegerArgumentType.integer(-1, 5)).executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                int n = IntegerArgumentType.getInteger(ctx, "n");
                StoryState.of(p).setPhaseOverride(n);
                Net.sync(p, "", "");
                ctx.getSource().sendSuccess(() -> Component.literal(n < 0 ? "Phase back to automatic." : "Phase set to " + n + "."), false);
                return 1;
            })))
            .then(Commands.literal("reset").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                StoryState.of(p).reset();
                Net.sync(p, "", "");
                ctx.getSource().sendSuccess(() -> Component.literal("Story progress reset."), false);
                return 1;
            }))
        );
    }

    private static int buildStructure(ServerPlayer p, int type, CommandSourceStack source) {
        BlockPos pos = Structures.spawnNear(p, 30, 45, true, type);
        source.sendSuccess(() -> Component.literal(pos != null
                ? "Structure built at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                : "No suitable spot found."), false);
        return pos != null ? 1 : 0;
    }
}
