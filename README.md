# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.6.0 keeps Minecraft gravity enabled for living marine-mob carriers and adds explicit compatibility for pools that are only two water blocks deep.

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

The orca can be assisted up to speed level 7 (14 blocks/s) and the shark up to level 6 (12 blocks/s), while normal Horse input still controls steering.

## Gravity-aware swimming

The living movement carriers now keep `setGravity(true)` during normal autonomous swimming, show-controlled swimming, riding, jump preparation, airborne motion, and water re-entry.

Swimming no longer depends on disabling gravity. Instead, active aquatic movement adds a small upward swimming force while gravity continues to act. This keeps falling/jump arcs physical while still allowing sharks and orcas to hold depth in water.

The crab also continues to use normal gravity while walking on the floor.

## Two-block-deep pool support

Autonomous breach preparation no longer requires water two full blocks below the mob. A valid shallow pool only needs:

- water at the animal's current layer; and
- water one block below it.

If the block two levels below is no longer water, the controller treats the pool as shallow. In that case the pre-jump dive is reduced to four ticks at only `-0.020` vertical velocity before the animal accelerates upward. This is designed to prevent the orca or shark from driving into the floor of a two-block-deep pool.

Deep pools keep the longer dive preparation used previously.

## Ten swimming-speed levels

Aquatic movement is expressed through `MarineSpeedLevel` rather than arbitrary per-behavior velocity constants.

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

The autonomous controller deliberately changes level instead of staying at maximum speed:

- Orca ordinary roaming: mainly levels 3-6 (6-12 blocks/s).
- Orca surface/breath approach: at least level 5.
- Orca food pursuit: level 7.
- Orca breach charge: levels 8-10 depending on planned height.
- Shark ordinary roaming: mainly levels 3-5.
- Shark food pursuit: level 6.
- Water-edge recovery uses a low level instead of full-speed turns.
- Existing show movement requests are quantized to the nearest one of the ten levels.

## Autonomous swimming and breaching

Autonomous aquatic mobs use gradual steering, blended acceleration/deceleration, depth intent, species-specific behavior, food attraction, water-edge avoidance, and gravity-aware vertical swimming force.

A breach sequence is:

1. confirm a usable surface, two-layer water depth, and clear space above;
2. perform a short dive, shortened further in a two-block pool;
3. accelerate upward using a high speed level;
4. break the surface with gravity already enabled;
5. follow a ballistic arc;
6. re-enter the water with splash/bubble effects;
7. resume ordinary autonomous swimming.

Orcas choose breach targets from 3 through 10 blocks. Sharks breach less often and use smaller 2-5 block jumps. The 10-block orca jump remains a design target under normal Minecraft entity physics rather than a claim of pixel-exact apex height on every live server.

## Segmented hitboxes

All three custom marine mobs have explicit `Interaction` hitboxes that follow the animated body:

- Orca: 6 body segments.
- Shark: 5 body segments.
- Crab: 3 segments covering body and claws.

These hitboxes resolve feeding, riding, and attack targeting. They are interaction/attack hitboxes, not solid physics bodies; the invisible movement carrier remains non-collidable so it does not shove players or snag on pool geometry.

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

## Graphics

The renderer remains pure Spigot `BlockDisplay`; no resource pack or third-party mesh is bundled.

The orca uses more than 60 display parts and the shark more than 50. Their segmented bodies, ventral surfaces, markings, fins, peduncles, tail/fluke animation, water-entry splashes, wake effects, and orca breathing effects are retained.

The crab remains at the smaller 1.35-block-scale profile introduced in 1.4.

## Orca shows

Existing show behavior remains available, including formation swimming, high-speed passes, jump waves, synchronized blow/splash cues, note-block music, and return to normal autonomy. Show-guided swimming now also keeps gravity enabled and uses swimming lift while the animal is in water.

### Configure a show entirely in Minecraft

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

If `[id]` is omitted, the first configured show is used. Commands write settings back to `config.yml`, so center, facing direction, and times survive a restart. The default show id is `orca-show` and scheduled execution is disabled until explicitly enabled.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.6.0-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

CI can verify the shallow-water profile and gravity-control code paths. A real Minecraft staging server is still required to measure actual depth holding with Horse water physics, confirm that a two-block-deep pool never causes visible floor clipping, and measure the exact breach apex/landing path in the target aquarium.
