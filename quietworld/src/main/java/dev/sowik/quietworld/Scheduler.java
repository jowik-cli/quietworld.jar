package dev.sowik.quietworld;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Tiny delayed-task queue so chat replies can arrive a moment after the question. */
@Mod.EventBusSubscriber(modid = Quietworld.MODID)
public final class Scheduler {
    private Scheduler() {}

    private static final class Task {
        final long due;
        final Runnable run;

        Task(long due, Runnable run) {
            this.due = due;
            this.run = run;
        }
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0L;

    public static long now() {
        return now;
    }

    public static void later(int ticks, Runnable run) {
        TASKS.add(new Task(now + ticks, run));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        now++;
        if (TASKS.isEmpty()) return;

        List<Runnable> ready = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.due <= now) {
                ready.add(t.run);
                it.remove();
            }
        }
        for (Runnable r : ready) {
            try {
                r.run();
            } catch (RuntimeException ex) {
                // a failed chat line must never crash the server tick
            }
        }
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        TASKS.clear();
    }
}
