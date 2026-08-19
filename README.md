# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.9.1 replaces the live-server airborne-hover fallback with deterministic manual air gravity, while retaining player-facing orca riding, active autonomous swimming, marine food, shallow-pool control, dense hitboxes, and scheduled orca shows.

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

Sharks and orcas use an invisible tamed, saddled Horse as the pilot carrier. Orcas support up to eight players: one pilot plus seven lazily-created additional passenger positions.

The orca pilot no longer depends on vanilla Horse steering. While the carrier is in water, the orca follows the pilot's facing direction directly. In a two-block-deep pool, the player's horizontal facing controls travel while vertical motion is tightly limited. In deeper water, player pitch can provide limited ascent/descent.

The ridden orca target speed remains 56 blocks/s (2.8 blocks/tick), exactly four times the previous 14 blocks/s maximum.

## Deterministic airborne gravity

Version 1.9.1 no longer relies on the invisible Horse/Slime carrier accepting Bukkit velocity updates while unsupported in air. On a live server, some carriers were observed to keep a velocity value while their actual position remained suspended.

When a tracked marine carrier is outside water and has no solid support, the final motion controller enters a manual-air state:

- carrier-native gravity integration is temporarily disabled only for the airborne phase;
- the plugin integrates a gravity acceleration of `0.08` blocks/tick² with `0.98` vertical drag;
- the resulting position is applied directly each tick;
- large displacement is subdivided into 0.20-block sweep steps to avoid skipping water or solid blocks;
- entering water immediately returns control to the normal aquatic controller;
- contacting a solid block ends the manual-air state and restores normal gravity.

This means the airborne result no longer depends on whether Horse velocity integration is functioning correctly on the server. The visible BlockDisplay model and hitboxes continue to follow the carrier.

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

Normal autonomous profiles remain:

- Orca ordinary roaming: levels 4-7 (8-14 blocks/s), with short level-8 (16 blocks/s) bursts.
- Shark ordinary roaming: levels 4-6 (8-12 blocks/s), with occasional level-7 (14 blocks/s) bursts.
- Orcas choose a new movement intent roughly every 50-120 ticks.
- Sharks choose a new movement intent roughly every 70-150 ticks.
- Orca breach opportunities are scheduled roughly every 260-650 ticks.
- Shark breach opportunities are scheduled roughly every 480-1200 ticks.

Wall avoidance, shallow-water holding, food pursuit, a rider, or show control can override roaming intent.

## Smooth rendering

Visible BlockDisplay models update every server tick. Position interpolation uses one tick, while visible yaw and pitch are smoothed separately from the physical carrier.

## Gravity and two-block-depth holding

Swimming uses active lift and directional thrust. For a two-block-deep pool, the controller distinguishes the lower and upper water layers:

- lower layer: strong upward recovery prevents prolonged floor scraping;
- upper layer: vertical motion is clamped to reduce bouncing between the floor and surface.

Shallow breach preparation uses a short four-tick, `-0.020` vertical descent before the upward charge. After the carrier leaves the water, the deterministic airborne integrator takes over until water re-entry or landing.

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

Manual startup first searches the saved show center. If no tracked orca is in that radius and a player ran the command, it also searches around that player using at least a 96-block fallback radius. Use `/marine show set-center` to persist the correct center.

If startup still fails, the command reports the configured center, control radius, number of tracked usable orcas in that world, and nearest tracked-orca distance. Manual startup results are also written to the server log.

## Residence / CMILib compatibility note

FreeLifeMarineMobs does not depend on Residence or CMILib. If Residence throws a `NoSuchFieldError` from `net.Zrips.CMILib.Version.Version` during `CreatureSpawnEvent`, that indicates a Residence/CMILib binary-version mismatch on the server. FreeLifeMarineMobs cannot repair incompatible third-party Residence/CMILib JARs.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.9.1-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the release JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

CI can verify the deterministic air-gravity equations, sweep subdivision, existing speed/pursuit/shallow-water/hitbox logic, source compatibility, and packaging. It still cannot reproduce the exact production-server physics/plugin combination or prove the visual result in a live Minecraft client; that requires a staging-server E2E run.
