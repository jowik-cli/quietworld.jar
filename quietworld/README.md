# Quiet World 0.4.2 (Forge 1.20.1)

A lore-heavy horror mod. The first two in-game days a silent figure (the Watcher) stares at you from the
tree line and leaves you notes. On day 3 the notes turn, on day 5 they stop.

It also has six more mobs, two extra dimensions, ten structure types, corrupted chunks and a set of loud
static/horror sounds tied to the events (see the sections below).

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
| `/quiet overdrive` | Starts Event Overdrive right now |
| `/quiet watcher` | Spawns a Watcher about 25 blocks away |
| `/quiet kosiw sky` / `visit` | Spawns kosiw in the sky, or standing nearby (he drops a gift after ~5-10 seconds) |
| `/quiet structure [0-9]` | Builds a structure 30-45 blocks away (random, or a specific type; ignores the per-player limit) |
| `/quiet mob <hollow\|glitch\|echo\|crawler\|tall\|tenant\|gazer\|moth\|lamplighter\|archivist\|wretch\|child>` | Spawns that mob near you |
| `/quiet corrupt` | Corrupts the chunk you are standing in |
| `/quiet hall` / `/quiet copy` / `/quiet home` | Enter the Static Hall, enter the Hollow Copy, or return to the overworld |
| `/quiet items` | Gives the Codex, Journal, Static Radio and 4 Corrupted Shards |
| `/quiet unlock <id\|all>` | Unlocks an archive entry (e.g. `mob_hollow`) or everything |
| `/quiet sync` | Re-sends your progress to your client (menus, archive, journal) |
| `/quiet sound <name>` | Plays a sound (`static_burst static_long drone screech heartbeat whisper glitch tv_off`) |
| `/quiet phase <0-5>` | Forces the story phase (see Phases below); `-1` goes back to automatic |
| `/quiet reset` | Wipes your story progress |

## Config (`quietworld-common.toml`)

`showWarning`, `eventsEnabled`, `eventChancePercent`, `eventCooldown`, `noteGapSeconds`, `kosiwEnabled`, `maxSites`,
`customMenus`, `flashingEffects`, `desktopFiles`, `soundVolume` (0.0-1.0) and `loudSounds` (false plays everything at about a third of the volume).

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


## Mobs

| Mob | What it does |
|---|---|
| **Watcher** | Stares from the tree line and leaves notes (days 1-2). |
| **kosiw** | Fake player. Sky-watch or visit-and-gift mode; can be questioned. |
| **Hollow** | Only moves while you are not looking at it. Touching you sends you to the **Static Hall** and pushes the story to phase 1. |
| **Glitch** | Hops around you every second or two, leaving corrupted blocks. Killable (6 HP); half the time it drops a corrupted book. |
| **Echo** | Wears your name. Repeats what you typed in chat, garbled, as if you had said it. Leaves when you get close. |
| **The Tall One** | The Watcher's aggressive form (phase 2 only). Chases and hits for 3 hearts, 60 HP, drops a corrupted book. |
| **Tenant** | Faceless villager that lives in the **Hollow Copy**. Right-click to talk; gives a lease note. |
| **Crawler** | Fast black spider. Hunts at night and in the Hollow Copy. |

## Dimensions

- **The Static Hall** (`quietworld:static_hall`): a 5x5 maze of grey concrete rooms under cold lamps. You arrive in the middle.
  The only way out is a lone door in the far corner, or you are thrown out after 4 minutes. Hollows roam it, and being
  touched in here throws you into a random room.
- **The Hollow Copy** (`quietworld:hollow_copy`): your own world's terrain, same seed, eternal night. Reached through a
  crying-obsidian **doorway** structure; a return doorway is built where you arrive. Crawlers and Tenants live here.
  Entering it pushes the story to phase 2.

Both are data-driven dimensions (`data/quietworld/dimension*`). If a world refuses to load after adding the mod, delete
those four JSON files from the jar and send me the log.

## Structures (10 types)

Candle ring with lectern, hay effigy, sunken sigil circle (the three ritual sites), K.'s abandoned camp, the doorway,
a house with no door, an underground dormitory of seven beds with your name on the signs, a radio mast that hums static,
a graveyard with one grave carrying your name, and a lookout tower. Most hold a lore book. They appear as a random event
from phase 0 on, or from kosiw's coordinate papers. Limit per player: `maxSites`.

## Corrupted chunks

A random event (phase 1+) turns the surface of a nearby chunk into magenta/black missing-texture checkers with
floating towers and pits. Standing in a corrupted chunk crackles with static and can spawn Glitches. Up to 14 per world.

## Sounds

Eight generated sounds (static bursts, long static, drone, screech, heartbeat, whisper, glitch, TV-off) play with events,
mob spawns, dimension travel, masts and corrupted chunks. **Some are loud on purpose.** Use `soundVolume` / `loudSounds`
in the config if you want them quieter. The intro warning screen says so.


## Menus

The main menu and the pause menu are fully replaced (`QuietTitleScreen`, `QuietPauseScreen`). Both are black, text-only,
with crawling TV static, scanlines, a vignette and glitching text. Entries are plain lines you hover and click: continue,
join another save, settings, mods, leave. The pause menu has resume, progress, tally,
settings and leave the save. Set `customMenus = false` in the config to get the vanilla menus back.


## Items

| Item | What it does | Where it comes from |
|---|---|---|
| **Codex** | Right-click to open the archive (see below). | Given to you shortly after the first note; craftable (book + black dye + spider eye). |
| **Journal** | Right-click to read every note you have found, as a worn paper book. It updates itself. | Given with the Codex; craftable (book + feather). |
| **Static Radio** | Right-click to hear which way the nearest structure is and how far. Crackles while held near one. | Craftable (iron ingots around a note block and redstone). |
| **Corrupted Shard** | Right-click to tear a hole in the quiet: it fires a random event and is used up. | Dropped by Glitches and the Tall One, or left as a gift by an event. |

Structures built before this update are not known to the radio; new ones are.

## The Archive (Codex)

A wiki that fills in as you play. 82 entries across mobs, structures, dimensions, events, items and story. Each entry
unlocks the first time you meet it (a mob comes near, you walk up to a structure, you enter a dimension, an event fires,
you pick up an item) and shows **what it is, what it does, and a piece of lore**. Locked entries show as `???`.
A toast pops up in the corner each time something is added. Entries are defined in `Codex.java`.
It also has a **notes** tab with every note you have collected.

## The Journal

Every Quiet World book you hold, pick up or read on a lectern is copied into your journal (and your notes tab). Up to
eight corrupted fragments are kept so it does not fill with noise.

## Menus that change with the story

The title and pause menus read your saved progress (a file called `quietworld_progress.dat` in the game folder, updated
whenever you play). They change with the phase: the title text, the `save.dat` subtitle, the footer lines, the amount
of static and a red tint all shift as the helper turns and disappears. New entries appear as you unlock things:
`archive`, `journal`, `kosiw is online` (after you meet him). Visiting the Static Hall leaves a faint grid on the title
screen and visiting the Hollow Copy leaves a doorway. The pause menu gets `archive` and `journal` shortcuts.

## Events (now 30)

New this version: doors swing open, a creeper hiss with no creeper, footsteps that stop behind you, a glitch in chat,
thunder under a clear sky, a distant bell, a one-second blink, a lost note on a lectern, a pumpkin-headed mannequin,
falling ash, undead groans, and a gift of a Corrupted Shard. `flashingEffects = false` disables the thunder and blink.


## Phases (now 6)

| Phase | Name | Starts | What changes |
|---|---|---|---|
| 0 | Arrival | day 1 | The helper is calm. Quiet, early events only. |
| 1 | The Turn | day 2 | Notes contradict each other. Hollows, Echoes, Glitches, Lamplighters, Lost Children, Static Moths. |
| 2 | The Silence | day 3 | The helper is gone. The Tall One, Gazers, Archivists, darkness, storms, sudden nights, hunger. |
| 3 | The Copy | day 4 | Wretch packs, Hollow packs, corruption storms, structure clusters, pickpocketing, health drain. |
| 4 | The Breaking | day 6 | Static storms, displacement, pairs of Tall Ones. |
| 5 | The Quiet | day 8 | Everything at once, and fastest. |

A phase also advances early when you visit the Static Hall (phase 1) or the Hollow Copy (phase 2). Each new phase plays
a sound, shows its name above your hotbar, sends a line in chat and leaves a desktop file. From phase 1 on, events fire
about 35% more often per phase and cool down about 13% faster (and are checked twice as often from phase 4). The menus change with each phase.

## Files on your desktop

The mod can leave small plain-text props on your desktop (each new phase, and a random event), such as
`K_was_here.txt`. They are part of the ARG. Safeguards:

- Only fixed files from a table inside the mod are ever written. A server cannot pick the name or contents.
- Nothing is ever overwritten, opened, run or read. At most 14 files are written in total.
- Each file ends with a line saying it is a harmless file from this mod.
- Turn it off with `desktopFiles = false` in the config. The warning screen mentions it.
- Open the Archive and click **remove desktop files** to delete exactly the files the mod made.

## New mobs (6)

| Mob | What it does |
|---|---|
| **Gazer** | Only moves while you look at it. Contact hurts, darkens and slows. |
| **Static Moth** | Bat-like. Eats torches nearby and hums static. |
| **Lamplighter** | Carries a soul lantern toward the nearest structure and waits for you. |
| **Archivist** | Right-click: writes one of your undiscovered archive entries in, then leaves. |
| **Wretch** | Ragged melee undead that hunts in packs (phase 3+). May drop a shard. |
| **Lost Child** | Runs when watched, creeps closer when not. Talk to it for a shard. |

## New events (51 in total)

21 new: name call, a desktop file, moths, a Lamplighter, a Lost Child, hunger pang, thunderstorm, night falls, a Gazer,
an Archivist, a fake death message, a Wretch pack, a Hollow pack, pickpocket (one stack moved onto the ground nearby),
health drain, structure cluster, corruption storm, hotbar swap, static storm, displacement and a pair of Tall Ones.
All of them are listed in the Archive.


## No mercy (0.4.0)

This version is meant to hurt. Everything below is deliberate:

- **Pacing:** phases come faster (see the table), events are 1.9x as likely at the start (28% per check, 90 second
  cooldown) and scale up from there. Old config keys `eventChance` and `eventCooldownSeconds` are ignored; delete your old
  `quietworld-common.toml` or set the new `eventChancePercent` and `eventCooldown`.
- **Hollow:** touching you now deals 3 hearts and darkens the world, then drags you into the Static Hall.
- **Gazer:** 4 hearts, then heavy slowness. **Watcher:** from phase 4 it strikes (2 hearts) as it leaves.
- **The Tall One:** 80 HP, much faster, hits for 5 hearts, very hard to knock back.
- **Wretch:** 40 HP, faster, 3 hearts. **Crawler:** 24 HP, faster.
- **Static Moth** makes you dizzy up close. **Glitch** blinds you as it hops. **Echo** makes you dizzy from phase 3 on.
- **The Static Hall and the Hollow Copy** spawn more Hollows and Crawlers.
- **Events:** health drain is 2 hearts, hunger pang is 8 points, plus 16 new ones that attack you directly
  (see below).

### The 16 new events

breathing, tremor, your name on a sign, knocking from behind, a choir of whispers (dizzy), lights out (up to 8 torches
die and the world darkens), smother (heavy slowness and mining fatigue), dark tide (20 seconds of darkness), cold snap
(freezing), the Watcher appearing 14 blocks away, a Hollow appearing directly behind you, a Crawler swarm, rust (a
quarter of your held tool's durability, never breaks it), searing (set on fire), a Gazer ambush and a Wretch horde.
That makes **68 events** in total, all recorded in the Archive.

### Tone

All the jokes and references to other horror mods are gone. Menus, signs, books, desktop files and kosiw's answers are
now plain horror.


## Event Overdrive (rare)

A rare event (about one successful roll in forty, phase 1 onward, never while one is already running). For **15 seconds**:

- the Glitch Shard sound plays at your position every **0.3 seconds** (50 times, same sound and pitch as using a shard),
- event chance is **30% higher** (chance x 1.3, capped at 97%),
- the event cooldown is just **2 seconds**,
- and events are checked every second instead of every 5 or 10 seconds, so the 2 second cooldown actually matters.

It is the loudest thing in the mod, so keep `soundVolume` in mind. It is recorded in the Archive as "Event Overdrive",
and `/quiet overdrive` starts it on demand for testing.


## Horror interface (0.4.2)

Every custom screen got the same treatment (`MenuFx.java`), and it gets worse as the story advances:

- **Eyes** open in the dark behind the menus and follow your cursor.
- **Blood** runs down from the top edges, hangs, and dries.
- **A tall figure** stands in the fog behind the title and pause menus. It only changes position between glances, and
  more of them appear from phase 3.
- **The glass cracks** (from phase 2 on the title screen, phase 1 on the pause screen, and in the Archive).
- **A heartbeat** flushes the screen edges red, harder at higher phases.
- **Glitch bars**, heavier static and scanlines, and a bad-bulb flicker.
- **A hidden word** ("BEHIND YOU", "IT SEES YOU"...) shows very dimly for a tenth of a second, about every half minute.
- **Sound inside the menus:** a low drone when the title opens, whispers, a heartbeat at higher phases, and a glitch tick
  when you hover or click an entry. All of it follows `soundVolume` and `loudSounds`.
- **The Archive** has a cracked, bleeding frame, red selection, and locked entries are black **censor bars** instead of `???`.
- **The Journal** is stained, torn and wet. From phase 2 lines get scratched out, and from phase 3 random words turn to
  scribble (more of them each phase).
- **Pop-ups** (archive, journal, desktop file notices) are now static-filled, blood-edged toasts.
- **The warning screen** has static, a pulse and watching eyes.

The flickers and the hidden word are switched off by `flashingEffects = false`. Everything else is purely visual.
