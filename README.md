# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.8.1 fixes airborne gravity while retaining the more active autonomous swimming and show-start recovery added in 1.8.0.

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

There is no natural spawn path, spawn egg, recipe, or scheduled marine-mob spawn. All plugin-created marine mobs originate from an explicit command.

### Riding

Sharks and orcas use an invisible tamed, saddled Horse as the native pilot carrier. Orcas still support up to eight players: one native pilot plus seven additional passenger positions.

Version 1.8 creates those extra ArmorStand passenger seats lazily: they are spawned only when additional riders actually need them, rather than spawning all seven when an orca is created.

## Airborne gravity fix

Aquatic movement control is now suspended whenever a shark or orca is outside water and still airborne. The plugin no longer rewrites vertical velocity every tick in that state, so the invisible living carrier and the visible model fall under normal Minecraft gravity. Scripted show guidance/holding is also water-only; deliberate breach launches still set the initial jump velocity, after which the airborne phase is gravity-driven. Landed animals may still use the restrained return-to-water controller. Crabs likewise stop scripted vertical control while airborne.

## More active autonomous swimming

The ten speed levels remain unchanged:

| Level | Blocks/s | Blocks/tick |
| --- | ---: | ---: |
| 1 | 2 | 0.1 |
| 2 | 4 | 0.2 |
| 3 | 6 | 0.3 |
| 4 | 8 | 0.4 |
| 5 | 10 | 0.5 |
| 6 | 12 | 0.6 |
| 7 | 14 | 0.7 |
| 8 | 16 | 0.8 |
| 9 | 18 | 0.9 |
| 10 | 20 | 1.0 |

Version 1.8 changes how those levels are used during normal autonomous swimming:

- Orca ordinary roaming: levels 4-7 (8-14 blocks/s), with short level-8 (16 blocks/s) bursts on some behavior changes.
- Shark ordinary roaming: levels 4-6 (8-12 blocks/s), with occasional level-7 (14 blocks/s) bursts.
- Orcas choose a new movement intent roughly every 50-120 ticks instead of the previous 95-219 tick range.
- Sharks choose a new movement intent roughly every 70-150 ticks instead of the previous 130-269 tick range.
- Orcas can choose turns up to about 55 degrees per intent; sharks up to about 38 degrees.
- Vertical intent is stronger, so ordinary swimming contains more noticeable rising and diving while the two-block-depth controller still protects shallow pools.
- Orca autonomous breach opportunities are scheduled roughly every 260-650 ticks (13-32.5 seconds).
- Shark autonomous breach opportunities are scheduled roughly every 480-1200 ticks (24-60 seconds).
- Orca surface breathing is also checked more frequently.

These values are targets for the controller. Wall avoidance, shallow-water holding, food pursuit, a rider, or show control can override the current roaming intent.

## Smooth rendering

Visible BlockDisplay models update every server tick. Position interpolation uses one tick, while visible yaw and pitch are smoothed separately from the physical carrier. This keeps the high-speed movement introduced in earlier versions from appearing as large two-tick visual steps.

## Gravity and two-block-depth holding

Living movement carriers keep Minecraft gravity enabled. Swimming uses active lift and directional thrust instead of disabling gravity.

For a two-block-deep pool, the controller distinguishes the lower and upper water layers:

- lower layer: strong upward recovery prevents prolonged floor scraping;
- upper layer: vertical motion is clamped to reduce bouncing between the floor and surface.

Shallow breach preparation uses a short four-tick, `-0.020` vertical descent before the upward charge.

## Marine food

```text
/marine food [1-64]
```

Permission: `freelifemarine.food` (default: server operators).

The command creates a PDC-tagged cod item named `海の餌`. Renaming ordinary cod does not create valid marine food.

Food targets are scanned every two ticks. Distant orcas can pursue food at level 9 (18 blocks/s), distant sharks at level 8 (16 blocks/s), and the controller reduces speed on final approach to reduce overshooting. Show-controlled orcas ignore food until the show finishes.

## Segmented hitboxes

All custom mobs use overlapping `Interaction` hitboxes for feeding, riding, and attack targeting:

- Orca: 10 segments;
- Shark: 8 segments;
- Crab: 5 segments.

These are interaction/attack hitboxes, not solid push bodies. The invisible movement carrier stays non-collidable to reduce snagging on pool geometry.

## Orca shows

Show behavior includes formation swimming, high-speed passes, jump waves, synchronized blow/splash cues, note-block music, and return to autonomous swimming.

### Configure a show

```text
/marine show set-center [id]
/marine show set-facing [id]
/marine show set-time 15:30 [id]
/marine show add-time 10:00 [id]
/marine show remove-time 10:00 [id]
/marine show enable [id]
/marine show disable [id]
/marine show status
/marine show list
/marine show reload
```

The default show id is `orca-show`.

### Start a show manually

```text
/marine show start
```

or:

```text
/marine show start orca-show
```

Manual startup first searches the saved show center as before. If no tracked orca is in that radius and a player ran the command, version 1.8 also searches around that player (at least 96 blocks). If it finds orcas there, the show starts with the player's current position as a temporary center for that one performance. Use:

```text
/marine show set-center
```

to save the correct center permanently.

If startup still fails, the command reports the configured center, control radius, number of tracked usable orcas in that world, and nearest tracked-orca distance. Manual startup results are also written to the server log.

## Residence / CMILib compatibility note

FreeLifeMarineMobs does not depend on Residence or CMILib. If Residence throws a `NoSuchFieldError` from `net.Zrips.CMILib.Version.Version` during `CreatureSpawnEvent`, that indicates a Residence/CMILib binary-version mismatch on the server. Version 1.8 reduces how many spawn events FreeLifeMarineMobs produces by creating extra orca passenger ArmorStands lazily, but it cannot repair incompatible third-party Residence/CMILib JARs. The server's Residence and CMILib versions still need to be mutually compatible.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.8.1-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the release JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

CI can verify the autonomous activity profile, existing speed/pursuit/shallow-water/hitbox logic, source compatibility, and packaging. It cannot reproduce a production server's Residence/CMILib plugin combination or prove the subjective movement feel in a live Minecraft client. A staging server is still required for those E2E checks.
