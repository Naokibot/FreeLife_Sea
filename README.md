# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.7.0 focuses on smoother client motion, much faster marine-food response, stronger two-block-depth holding, and denser interaction/attack hitboxes.

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

There is no natural spawn path, spawn egg, recipe, or scheduled mob spawn. All plugin-created marine mobs originate from an explicit command.

### Riding

Sharks and orcas keep native mounted steering through an invisible tamed saddled horse pilot carrier. The orca supports eight players: one pilot and seven passenger seats.

## Smoother movement

Display models are now updated every server tick instead of every two ticks. At the maximum 20 blocks/s speed this removes the previous two-block visual step between model updates. BlockDisplay teleport interpolation is reduced to one tick and transformation interpolation remains active.

Visual yaw and pitch are also smoothed independently from the living carrier. The physical carrier still follows the actual controller immediately, while the visible shark/orca body eases toward the new heading instead of snapping to every small rotation change.

## Gravity and two-block-depth holding

Living movement carriers keep Minecraft gravity enabled. Swimming uses active lift and directional thrust rather than disabling gravity.

For a two-block-deep pool, the controller recognizes both useful vertical positions:

- lower water layer: adds a strong upward recovery so the carrier does not scrape the floor;
- upper water layer: clamps vertical oscillation so the animal does not bounce repeatedly between floor and surface.

Shallow breach preparation still uses the short four-tick, `-0.020` descent introduced in 1.6 before upward acceleration.

## Ten swimming-speed levels

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

Normal autonomous movement still uses several tiers rather than staying at maximum speed. High tiers are reserved for food pursuit and breach charging.

## Marine food

```text
/marine food [1-64]
```

Permission: `freelifemarine.food` (default: server operators).

The command creates a PDC-tagged cod item named `海の餌`. Renaming ordinary cod does not create valid marine food.

Food response is more immediate in 1.7:

- food target scanning runs every 2 ticks instead of every 6 ticks;
- distant orcas can use speed level 9 (18 blocks/s);
- distant sharks can use speed level 8 (16 blocks/s);
- acceleration toward food is increased;
- the final approach automatically drops to a lower tier to reduce overshooting;
- crabs also move faster while actively approaching food.

Show-controlled orcas still ignore food until the show ends.

## Dense segmented hitboxes

All custom mobs use overlapping `Interaction` hitboxes for feeding, riding and attack targeting:

- Orca: 10 segments;
- Shark: 8 segments;
- Crab: 5 segments.

The extra overlapping segments reduce dead zones between the visible head, body, tail and claws. These remain targeting/interaction hitboxes rather than solid push bodies; the invisible movement carrier is non-collidable to avoid snagging large animals on pool geometry.

## Autonomous swimming and breaching

Autonomous aquatic mobs use gradual steering, blended acceleration/deceleration, depth intent, water-edge avoidance, gravity-aware vertical control, and autonomous breach behavior. Orcas can target breach heights up to 10 blocks; sharks use smaller breaches.

## Orca shows

Show behavior includes formation swimming, high-speed passes, jump waves, synchronized blow/splash cues, note-block music, and return to normal autonomy.

### Configure and start a show in Minecraft

```text
/marine show set-center [id]
/marine show set-facing [id]
/marine show set-time 15:30 [id]
/marine show add-time 10:00 [id]
/marine show remove-time 10:00 [id]
/marine show enable [id]
/marine show disable [id]
/marine show start [id]
/marine show stop
/marine show status
/marine show list
/marine show reload
```

If `[id]` is omitted, the first configured show is used. The default show id is `orca-show`, so the normal manual-start commands are:

```text
/marine show start
```

or explicitly:

```text
/marine show start orca-show
```

Settings written by the show configuration commands are saved to `config.yml`.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.7.0-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

CI can verify the pursuit tiers, shallow-height controller, hitbox profile and packaging. A real Minecraft staging server is still required to judge subjective smoothness, exact two-block-pool visual clearance, food approach feel at high speed, and hitbox feel from every viewing angle.
