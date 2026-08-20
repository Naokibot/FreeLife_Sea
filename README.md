# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.10.0 makes autonomous aquatic movement substantially more active, expands orca breaches to 3-13 blocks above the water surface, and makes ridden orcas follow the pilot's three-dimensional gaze while retaining deterministic manual airborne gravity.

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

The orca pilot does not depend on vanilla Horse steering. While in water, the pilot's gaze controls travel in three dimensions: looking upward commands ascent, looking downward commands a dive, and looking left/right changes the horizontal heading. In a two-block-deep pool the vertical component is clamped and floor/surface probes further restrict it so gaze control cannot intentionally drive the carrier through the pool floor. In deeper water, vertical gaze authority is larger.

The ridden orca target speed is 56 blocks/s (2.8 blocks/tick), exactly four times the previous 14 blocks/s maximum. Once the orca actually leaves the water, rider propulsion stops and the deterministic airborne controller takes over.

## Deterministic airborne gravity

Version 1.9.1 stopped relying on the invisible Horse/Slime carrier accepting Bukkit velocity updates while unsupported in air. On a live server, some carriers had been observed to keep a velocity value while their actual position remained suspended.

When a tracked marine carrier is outside water and has no solid support, the final motion controller enters a manual-air state:

- carrier-native gravity integration is temporarily disabled only for the airborne phase;
- the plugin integrates gravity at `0.08` blocks/tick² with `0.98` vertical drag;
- the resulting position is applied directly each tick;
- large displacement is subdivided into 0.20-block sweep steps to avoid skipping water or solid blocks;
- entering water immediately returns control to aquatic movement;
- contacting a solid block ends manual-air control and restores normal gravity.

The visible BlockDisplay model and segmented hitboxes continue to follow the physical carrier.

## High-activity autonomous swimming

The ten speed levels are:

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

Version 1.10.0 uses those levels much more aggressively:

- Orca ordinary roaming: levels 7-9 (14-18 blocks/s).
- Orca burst: level 10 (20 blocks/s), selected on roughly 55% of intent changes.
- Shark ordinary roaming: levels 6-8 (12-16 blocks/s).
- Shark burst: level 9 (18 blocks/s), selected on roughly 35% of intent changes.
- Orca movement intent changes roughly every 20-55 ticks (1.0-2.75 seconds).
- Shark movement intent changes roughly every 30-75 ticks (1.5-3.75 seconds).
- Orca yaw intent can change by about 70 degrees at a time; shark by about 50 degrees.
- Vertical autonomous intent is stronger than previous versions.
- The final motion pass enforces a minimum autonomous horizontal speed when there is water ahead, so a prior controller cannot leave an animal coasting slowly in open water.

Wall avoidance, shallow-water protection, food pursuit, riders, and show control remain higher-priority constraints.

## Autonomous breaches

The final motion controller includes an independent breach scheduler so autonomous jumps cannot be lost to earlier movement updates in the same tick.

- Orca target height: **3-13 blocks above the water surface**.
- Shark target height: 3-7 blocks.
- Orca breach opportunity interval: roughly 100-320 ticks (5-16 seconds).
- Shark breach opportunity interval: roughly 200-600 ticks (10-30 seconds).
- A breach requires the animal to be near the water surface and requires clear overhead space.
- Higher breaches use higher approach speed and a larger initial vertical velocity.
- Once the carrier exits water, deterministic manual airborne gravity produces the ascent, apex, fall, and water re-entry.

The 3-13 block numbers are controller targets. CI validates the configured height range and initial-velocity progression, but the exact visual apex on a live server still requires E2E measurement.

## Smooth rendering

Visible BlockDisplay models update every server tick. Position interpolation uses one tick, while visible yaw and pitch are smoothed separately from the physical carrier.

## Gravity and two-block-depth holding

Swimming uses active lift and directional thrust. For a two-block-deep pool, the controller distinguishes the lower and upper water layers:

- lower layer: strong upward recovery prevents prolonged floor scraping;
- upper layer: vertical motion is clamped to reduce bouncing between the floor and surface.

Shallow breach preparation remains compatible with a two-block-deep pool. After leaving water, the deterministic airborne integrator takes over until water re-entry or landing.

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
target/FreeLifeMarineMobs-1.10.0-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the release JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

CI can verify autonomous profile ranges, breach target ranges and launch tuning, deterministic air-gravity equations, sweep subdivision, existing food/shallow-water/hitbox logic, source compatibility, and packaging. It cannot prove the exact production-server movement feel, exact 13-block visual apex, or high-speed rider behavior in a real Minecraft client; those remain staging-server E2E checks.
