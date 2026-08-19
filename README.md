# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.4.0 focuses on smaller crabs, more natural autonomous motion, marine-food attraction, in-game orca-show setup, and another graphics pass for sharks and orcas.

## Requirements

- Java 21
- Spigot 1.21.1

## Marine mobs

```text
/marine spawn shark
/marine spawn orca
/marine spawn crab
```

Permission: `freelifemarine.spawn` (default: server operators).

There is no natural spawn path, spawn egg, recipe, or scheduled mob spawn. All plugin-created marine mobs still originate from an explicit command.

### Riding

Sharks and orcas keep native mounted steering through an invisible tamed saddled horse pilot carrier. The orca still supports eight players: one pilot and seven passenger seats.

## Marine food

```text
/marine food [1-64]
```

Permission: `freelifemarine.food` (default: server operators).

The command creates a PDC-tagged cod item named `海の餌`. Renaming ordinary cod does not turn it into valid marine food.

- Hold the food in either hand to attract nearby marine mobs.
- Drop the food into or near the water to make nearby marine mobs approach it.
- A dropped food item is consumed when a marine mob reaches it.
- Right-click a marine mob while holding the food to feed it directly; one item is consumed and up to two health is restored, capped at 10.
- Attraction range is species-specific: orcas are the most responsive, sharks are intermediate, and crabs have the shortest range.
- Show-controlled orcas ignore food until the show finishes.

## More natural autonomous behavior

Version 1.4 no longer drives every aquatic mob with a fixed speed and fixed sinusoidal vertical path.

- Sharks use long, gradual turns, relatively steady forward speed, gentle depth changes, and a bias away from repeatedly skimming the surface.
- Orcas vary speed more, make broader course changes, periodically rise toward the surface for a breath, then resume deeper swimming.
- Velocity changes are blended over time instead of being replaced abruptly each tick.
- Water-edge avoidance remains active so autonomous aquatic mobs turn before leaving usable water where possible.
- Food temporarily overrides ordinary roaming while preserving smooth steering.
- Crabs are substantially smaller, walk more slowly, pause occasionally, change lateral direction, and still move sideways rather than like a fish.

## Graphics

The renderer remains pure Spigot `BlockDisplay`; no resource pack or third-party mesh is bundled.

### Orca

The orca now uses more than 60 display parts. The model uses shorter tapered body slices, a longer hydrodynamic profile, segmented white ventral surface, two-part eye patches, saddle patches, a five-stage dorsal fin, multi-stage pectoral flippers, animated caudal peduncle, and three-stage flukes on each side.

### Shark

The shark now uses more than 50 display parts. It has a more tapered spindle-shaped body, segmented white underside, five gill slits per side, multiple dorsal/pectoral/pelvic fin sections, animated posterior body, caudal peduncle keels, and asymmetric upper/lower tail lobes.

### Crab

The crab hitbox is reduced from the old 3.2-block width to 1.35 blocks, and the visible body/claws/legs were rescaled to match.

## Orca shows

Existing show behavior remains available, including formation swimming, high-speed passes, jump waves, synchronized blow/splash cues, note-block music, and return to normal autonomy.

### Configure a show entirely in Minecraft

Stand at the desired show center:

```text
/marine show set-center [id]
```

Look toward the direction the show should face:

```text
/marine show set-facing [id]
```

Replace the schedule with one time:

```text
/marine show set-time 15:30 [id]
```

Add or remove times:

```text
/marine show add-time 10:00 [id]
/marine show remove-time 10:00 [id]
```

Enable or disable automatic scheduled execution:

```text
/marine show enable [id]
/marine show disable [id]
```

Other show commands:

```text
/marine show start [id]
/marine show stop
/marine show status
/marine show list
/marine show reload
```

If `[id]` is omitted, the first configured show is used. Commands write the setting back to `config.yml`, so the center, facing direction, and times survive a restart.

The default show id is `orca-show` and scheduled execution is disabled until explicitly enabled.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.4.0-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

A real Minecraft staging server is still required to judge visual proportions from all angles, attraction feel near complex pool geometry, deep-water mounted behavior, and the final show trajectory for a specific aquarium build.
