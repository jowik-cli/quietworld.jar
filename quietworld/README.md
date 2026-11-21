# Quiet World (Forge 1.20.1)

A lore-heavy horror mod. The first two in-game days a silent figure (the Watcher) stares at you from the
tree line and leaves you notes. On day 3 the notes turn, on day 5 they stop.

Early on (days 1-4) a fake player called **kosiw** also "joins the game". He either hangs high in the sky
watching you, or stands nearby, hands you an item, and answers questions. Ritual-looking structures appear
in the world, and one of kosiw's gifts is a paper with the coordinates of a freshly built one.

- Minecraft 1.20.1
- Forge 47.3.0 (official Mojang mappings)
- Java 17

## Build the jar

1. Install a JDK 17.
2. This project has no Gradle wrapper jar. Pick one:
   - Download the Forge 1.20.1 MDK from files.minecraftforge.net and copy its `gradlew`, `gradlew.bat`
     and `gradle/` folder into this project (overwrite `gradle/wrapper`), then run `./gradlew build`.
   - Or install Gradle 8.1.1 and run `gradle build`.
3. The jar ends up in `build/libs/quietworld-0.1.0.jar`. Drop it in the `mods` folder of a Forge 1.20.1 install.

For development use `gradlew runClient` (or `genIntellijRuns` / `genEclipseRuns` first).

## Test commands (op / cheats)

| Command | What it does |
|---|---|
| `/quiet note` | Gives the next note for your current phase (ignores the time gap) |
| `/quiet event` | Fires one random event allowed in your phase |
| `/quiet watcher` | Spawns a Watcher about 25 blocks away |
| `/quiet kosiw sky` / `visit` | Spawns kosiw in the sky, or standing nearby (he drops a gift after ~5-10 seconds) |
| `/quiet ritual` | Builds a ritual site 30-45 blocks away (ignores the per-player limit) |
| `/quiet phase <0-2>` | Forces the story phase (0 helper, 1 the turn, 2 the helper is gone); `-1` goes back to automatic |
| `/quiet reset` | Wipes your story progress |

## Config (`quietworld-common.toml`)

`showWarning`, `eventsEnabled`, `eventChance`, `eventCooldownSeconds`, `noteGapSeconds`, `kosiwEnabled`, `maxRituals`.

## kosiw

- **Sky mode:** appears about 38 blocks above the terrain, 20-35 blocks away, stares down at you for 25-45 seconds, then "leaves the game".
- **Visit mode:** stands 9-14 blocks away, stays about 75 seconds, and after 5-10 seconds drops one item:
  1-4 diamonds, 2 cooked beef, a corrupted random book, or a paper named with ritual-site coordinates.
- **Asking him things:** right-click him to get clickable questions, or type in chat near him (or include "kosiw" in the message).
  Keywords like "who", "notes", "circles", "leave" pick the answer. His answers get glitchier after day 2.
- **Look:** a classic-layout player skin with missing-texture patches, shifted rows and noise (`textures/entity/kosiw.png`).
  Replace it with any 64x64 skin you like.
- He cannot be hurt, and only appears in the Overworld.

## Ritual sites

Three procedural variants, each built inside one chunk: a blackstone candle ring with a lectern holding a corrupted book,
a hay-and-fence effigy ringed by candle posts, and a sunken circle with a red-carpet sigil and a skull on a bone pillar.
They appear as a random event (day 3+) or from kosiw's coordinate paper. Limit per player: `maxRituals`.

## Where things live

| Want to change | File |
|---|---|
| Notes and their order | `Lore.java` |
| Random events and signs | `RandomEvents.java` |
| kosiw's behaviour and gifts | `KosiwEntity.java` |
| kosiw's answers | `Dialogue.java` |
| Corrupted books and text | `Corruption.java` |
| Ritual structures | `Rituals.java` |
| Day thresholds for the phases | `StoryState.phase()` |
| Intro warning text | `WarningScreen.java` (`LINES`) |
| Splash texts | `assets/minecraft/texts/splashes.txt` |
| Watcher look | `assets/quietworld/textures/entity/watcher.png` (64x64, zombie UV layout) |

## Custom menu panorama

Drop six square images (for example 1024x1024) into
`src/main/resources/assets/minecraft/textures/gui/title/background/` named `panorama_0.png` to `panorama_5.png`.
The mod's resources override vanilla automatically.
