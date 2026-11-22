package dev.sowik.quietworld;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** kosiw's answers. Questions come from clickable topics or free-typed chat. */
public final class Dialogue {
    private Dialogue() {}

    public record Topic(String id, String label, String[] keys, List<String> early, List<String> turned) {}

    private static final List<Topic> TOPICS = List.of(
        new Topic("notes", "Did you write the notes?", new String[]{"note", "notes", "book", "books", "wrote", "write", "page", "pages"},
            List.of("Some of them.", "The handwriting is not mine. The words might be."),
            List.of("Not the last ones.", "Do not trust the last ones.")),
        new Topic("ritual", "What are the circles?", new String[]{"ritual", "circle", "circles", "structure", "structures", "build", "built", "candle", "candles"},
            List.of("Circles. Somebody keeps building circles.", "I have never seen who. I only find the candles."),
            List.of("I do not go near them anymore.", "They are warm, {name}. Nothing out here is warm.")),
        new Topic("watcher", "What is the tall one?", new String[]{"tall", "watcher", "figure", "thing", "stands", "staring", "stare", "watching"},
            List.of("The tall one? It is not mine.", "It only looks. Do not give it a reason to do more."),
            List.of("It stopped leaving. You noticed too.", "It has your walk now.")),
        new Topic("herobrine", "Is Herobrine around?", new String[]{"herobrine", "brine"},
            List.of("Herobrine? He quit.", "Creative differences. He wanted more fog, the Watcher wanted more standing."),
            List.of("He left a sign that said brb.", "That was a long time ago. Do not make it say that again.")),
        new Topic("mods", "Are there other horror mods here?", new String[]{"mod", "mods", "modpack", "jar", "wonderland", "script", "slenderman", "backrooms", "null", "dweller", "entity", "303"},
            List.of("Other horror mods? We are not in competition.", "They have bigger budgets. We have a very tall man."),
            List.of("Do not say their names in here. The Watcher is a stickler about trademarks.", "wonderland.jar sends its regards. It has its own door.")),
        new Topic("hall", "What is the Static Hall?", new String[]{"hall", "static", "backrooms", "rooms"},
            List.of("A room it keeps for people it has finished with.", "The door is always in the farthest corner."),
            List.of("It is not a room. It is the inside of the signal.", "Do not follow the sound.")),
        new Topic("hollow", "What are the hollow ones?", new String[]{"hollow", "hollows", "statue", "statues", "moves", "moving"},
            List.of("They move when you look away.", "So do not look away."),
            List.of("They were people. Watch where they stand when you blink.")),
        new Topic("leave", "How do I leave?", new String[]{"leave", "escape", "exit", "out of", "get out", "go home"},
            List.of("People do not leave a place like this.", "They stop loading it. That is the only way I know."),
            List.of("There is a door. It is a picture of a door.", "Do not knock.")),
        new Topic("place", "What is this place?", new String[]{"place", "world", "where", "here", "save"},
            List.of("A copy. Someone saved it before it was finished."),
            List.of("A copy of a copy. It gets smaller each time.")),
        new Topic("name", "Why does it know my name?", new String[]{"my name", "know me", "knows me", "knows my"},
            List.of("You told it. Every time you joined."),
            List.of("It was written before you got here.", "I read it. Do not ask me where.")),
        new Topic("gift", "Why are you giving me things?", new String[]{"gift", "give", "gave", "diamond", "diamonds", "paper", "beef", "food", "drop", "dropped"},
            List.of("You looked hungry. Or lost.", "It is hard to tell from up here."),
            List.of("Take what I leave. There will be less soon.")),
        new Topic("who", "Who are you?", new String[]{"who", "you", "kosiw", "yourself"},
            List.of("kosiw.", "That is what the file calls me. I stopped checking."),
            List.of("kosiw.", "I think. The letters keep moving.")),
        new Topic("hi", "Hello?", new String[]{"hello", "hi", "hey", "yo", "sup"},
            List.of("...hi.", "You can talk. Good. Most do not."),
            List.of("Hi, {name}.", "Keep the lights on."))
    );

    private static final String[] FALLBACK = {
        "...",
        "I do not know that one.",
        "Ask me something smaller.",
        "Say that again. It got quiet."
    };

    private static final Map<UUID, Long> LAST_REPLY = new HashMap<>();

    public static boolean kosiwNear(ServerPlayer p, double radius) {
        return !p.level().getEntitiesOfClass(KosiwEntity.class, p.getBoundingBox().inflate(radius)).isEmpty();
    }

    public static void sayAs(ServerPlayer p, String who, String text) {
        p.sendSystemMessage(Component.literal("<" + who + "> " + text));
    }

    public static void say(ServerPlayer p, String text) {
        p.sendSystemMessage(Component.literal("<" + KosiwEntity.NAME + "> " + text));
    }

    /** Right-clicking kosiw prints clickable questions. */
    public static void showMenu(ServerPlayer p) {
        say(p, "...yes?");
        for (Topic t : TOPICS) {
            final String id = t.id();
            p.sendSystemMessage(Component.literal("  > " + t.label()).withStyle(style -> style
                    .withColor(ChatFormatting.GRAY)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quietask " + id))));
        }
    }

    /** Called by the clickable menu. */
    public static void ask(ServerPlayer p, String topicId) {
        if (!kosiwNear(p, 16.0D)) {
            p.sendSystemMessage(Component.literal("Nobody answers.").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (Topic t : TOPICS) {
            if (t.id().equals(topicId)) {
                answer(p, t);
                return;
            }
        }
    }

    /** Called when the player types in chat near kosiw (or mentions his name). */
    public static void reply(ServerPlayer p, String message) {
        long now = Scheduler.now();
        Long last = LAST_REPLY.get(p.getUUID());
        if (last != null && now - last < 40L) return;
        LAST_REPLY.put(p.getUUID(), now);

        String lower = message.toLowerCase(Locale.ROOT);
        Set<String> words = new HashSet<>();
        for (String w : lower.split("[^a-z0-9']+")) {
            if (!w.isEmpty()) words.add(w);
        }

        Topic hit = null;
        for (Topic t : TOPICS) {
            if (matches(lower, words, t)) {
                hit = t;
                break;
            }
        }
        if (hit == null) {
            final String line = FALLBACK[p.getRandom().nextInt(FALLBACK.length)];
            Scheduler.later(25, () -> say(p, line));
            return;
        }
        answer(p, hit);
    }

    private static boolean matches(String lower, Set<String> words, Topic t) {
        for (String key : t.keys()) {
            if (key.contains(" ")) {
                if (lower.contains(key)) return true;
            } else if (words.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static void answer(ServerPlayer p, Topic t) {
        int phase = StoryState.of(p).phase(p);
        List<String> lines = phase == 0 ? t.early() : t.turned();
        String name = p.getName().getString();
        int delay = 20;
        for (String raw : lines) {
            String text = raw.replace("{name}", name);
            if (phase >= 1) {
                text = Corruption.garble(text, p.getRandom(), 0.05D);
            }
            final String line = text;
            Scheduler.later(delay, () -> say(p, line));
            delay += 45;
        }
    }
}
