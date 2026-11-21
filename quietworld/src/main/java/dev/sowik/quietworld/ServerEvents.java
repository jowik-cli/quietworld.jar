package dev.sowik.quietworld;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Quietworld.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
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

        if (state.introDone() && player.tickCount % 200 == 0 && Config.EVENTS_ENABLED.get()) {
            RandomEvents.roll(player, state, false);
        }
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!StoryState.of(player).started()) return;
        StoryState.deliverNext(player, player.position(), false);
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String text = event.getRawText();
        boolean addressed = text.toLowerCase(java.util.Locale.ROOT).contains("kosiw");
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
            .then(Commands.literal("ritual").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                BlockPos pos = Rituals.spawnNear(p, 30, 45, true);
                ctx.getSource().sendSuccess(() -> Component.literal(pos != null
                        ? "Ritual built at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                        : "No suitable spot found."), false);
                return pos != null ? 1 : 0;
            }))
            .then(Commands.literal("phase").then(Commands.argument("n", IntegerArgumentType.integer(-1, 2)).executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                int n = IntegerArgumentType.getInteger(ctx, "n");
                StoryState.of(p).setPhaseOverride(n);
                ctx.getSource().sendSuccess(() -> Component.literal(n < 0 ? "Phase back to automatic." : "Phase set to " + n + "."), false);
                return 1;
            })))
            .then(Commands.literal("reset").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                StoryState.of(p).reset();
                ctx.getSource().sendSuccess(() -> Component.literal("Story progress reset."), false);
                return 1;
            }))
        );
    }
}
