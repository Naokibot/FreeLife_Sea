# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.3.0 adds scheduled orca shows with show-specific autonomous movement and built-in music while retaining the native mounted controls and higher-detail models from 1.2.0.

## Requirements

- Java 21
- Spigot 1.21.1

## Marine mob commands

```text
/marine spawn shark
/marine spawn orca
/marine spawn crab
```

Permission: `freelifemarine.spawn` (default: server operators).

There is no natural spawning, spawn egg, crafting recipe, or scheduled automatic mob creation. Scheduled shows only select orcas that were already spawned by command.

## Orca show commands

```text
/marine show list
/marine show start [show-id]
/marine show stop
/marine show status
/marine show reload
```

Permission: `freelifemarine.show` (default: server operators).

`start` can run a disabled schedule manually, which is useful while configuring a pool. `reload` reloads `config.yml` show definitions without restarting the server.

## Scheduled orca shows

A show checks real clock time once per second. Each show has its own Java `ZoneId`; the default example uses `Asia/Tokyo`. A scheduled occurrence is de-duplicated so the once-per-second checker cannot start the same show repeatedly during the matching minute.

Example `config.yml`:

```yaml
shows:
  orca-show:
    enabled: true
    time-zone: 'Asia/Tokyo'
    times:
      - '10:00'
      - '13:00'
      - '15:30'
    world: 'world'
    center:
      x: 120.5
      y: 63.0
      z: -80.5
    heading-yaw: 0.0
    orcas: 4
    control-radius: 48.0
    audience-radius: 64.0
    music:
      enabled: true
      volume: 0.75
```

`heading-yaw` controls the direction of the derived show path: `0` is south (+Z), `90` is west (-X), `180` is north (-Z), and `-90` is east (+X).

The show selects the nearest already-spawned orcas inside `control-radius`, up to `orcas` (1-8). If no orca is available, that occurrence is skipped. The scheduler never creates a replacement animal automatically.

## Show sequence

The current approximately 38-second choreography is deterministic enough for a show while still using real entity movement:

1. gather into a line behind the pool center;
2. hold formation;
3. fast formation swim past the audience-facing center;
4. return to a deeper jump-preparation line;
5. perform a sequential jump wave, with each orca launching 16 ticks after the previous one;
6. regroup near the surface and perform a synchronized blow/splash cue;
7. swim toward the exit side and return to normal autonomous swimming.

Water-state tracking remains active during the show. Crossing the water surface therefore still produces the normal size-scaled entry/exit splash and bubble effects. Riders are ejected when an orca is taken into show control, and mounting is blocked until the performance is finished or stopped.

## Show music

Music is played only to players within `audience-radius` while a show is active. It uses vanilla note-block sounds in the `MUSIC` sound category, so no resource pack or downloaded audio file is required.

The built-in loop combines harp melody notes with bass, bass-drum, and bell accents. Music stops naturally when the show stops because note-block sounds are short one-shot cues; there is no long-running external audio stream to leave behind.

Use:

```yaml
music:
  enabled: true
  volume: 0.75
```

Volume is clamped to `0.0-2.0`.

## Riding controls

Sharks and orcas use an invisible, tamed, saddled vanilla horse as the pilot carrier. The first rider controls the creature through Minecraft's normal mounted movement input instead of a camera-direction autopilot.

- normal mounted forward/back/left/right input drives the pilot carrier;
- normal mounted jump control is preserved;
- excessive speed is capped and a small water-drag assist is applied;
- on land, shark/orca horizontal speed is reduced;
- when no pilot or show is active, autonomous marine movement resumes.

The orca supports eight players: one native pilot plus seven independent invisible passenger seats.

## Graphics

The orca uses 48 `BlockDisplay` parts, including tapered body sections, segmented white ventral markings, multi-part eye and saddle patches, a four-stage dorsal fin, two-stage pectoral flippers, animated horizontal flukes, mouth, eyes, and blowhole detail.

The shark uses 39 display parts with tapered dorsal and ventral sections, eyes, mouth, ten gill slits, multiple fins, tail stock, and asymmetric upper/lower caudal lobes.

The crab remains a laterally walking, non-rideable model with animated legs and claws.

## Rendering limitation

The plugin uses vanilla `BlockDisplay` entities. It does not bundle OBJ/GLTF models or a resource pack, so the animals remain high-detail voxel approximations rather than smooth arbitrary meshes.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.3.0-Spigot-1.21.1.jar
```

## Verification

GitHub Actions compiles and tests against the real Spigot 1.21.1 API, validates the JAR, checks Java 21 class version 65, checks `plugin.yml` and `config.yml`, and verifies that Bukkit classes are not shaded into the plugin.

A real Minecraft client/server staging test is still required for final show-path placement in a specific pool, jump trajectory versus pool dimensions, multi-orca spacing, music loudness, deep-water mounted behavior, eight-player seating, and appearance from all viewing angles.
