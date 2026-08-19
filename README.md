# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.5.0 adds ten discrete swimming-speed levels, faster orca movement, autonomous breaches up to 10 blocks high, and segmented interaction/attack hitboxes for every custom marine mob.

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

Version 1.5 raises mounted-water speed limits as well: the orca can be assisted up to speed level 7 (14 blocks/s) and the shark up to level 6 (12 blocks/s), while normal Horse input still controls steering.

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

Autonomous aquatic mobs keep the natural-intent controller introduced in 1.4: gradual steering, blended acceleration/deceleration, depth intent, species-specific behavior, food attraction, and water-edge avoidance.

Version 1.5 adds autonomous breach behavior:

1. The animal must be near a usable water surface with enough depth below and clear space above.
2. It performs a short dive.
3. It accelerates upward using a higher speed level.
4. It breaks the surface with gravity enabled.
5. It follows a ballistic arc and re-enters the water.
6. The existing splash/bubble transition effects run on exit and re-entry.
7. Normal autonomous swimming resumes after landing.

Orcas choose breach heights from 3 through 10 blocks. Sharks breach less often and use smaller 2-5 block jumps. The 10-block orca jump uses a capped launch velocity intended to peak at approximately 10 blocks under normal Minecraft entity gravity; it is not allowed to exceed the configured 10-block ceiling by the plugin.

## Segmented hitboxes

All three custom marine mobs now have explicit `Interaction` hitboxes that follow the animated body:

- Orca: 6 body segments.
- Shark: 5 body segments.
- Crab: 3 segments covering body and claws.

These hitboxes are registered back to the owning marine mob, so right-click feeding/riding and attack targeting can resolve the custom mob across more of its visible body instead of relying on one oversized central box. They are interaction/attack hitboxes, not solid physics bodies; the invisible movement carrier remains non-collidable so it does not shove players or interfere with pool geometry.

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

The orca uses more than 60 display parts and the shark more than 50. Their existing segmented bodies, ventral surfaces, markings, fins, peduncles, tail/fluke animation, water-entry splashes, wake effects, and orca breathing effects are retained.

The crab remains at the smaller 1.35-block-scale profile introduced in 1.4.

## Orca shows

Existing show behavior remains available, including formation swimming, high-speed passes, jump waves, synchronized blow/splash cues, note-block music, and return to normal autonomy. Show movement speeds are now quantized to the same ten-level speed system.

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
target/FreeLifeMarineMobs-1.5.0-Spigot-1.21.1.jar
```

## Verification boundary

GitHub Actions compiles and tests against the declared Spigot 1.21.1 API, validates the JAR, verifies Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

CI can verify speed-level math, hitbox profiles, source/API compatibility, and packaging. A real Minecraft staging server is still required to judge whether 20-block/s bursts feel appropriate for a specific pool, whether a nominal 10-block breach clears the actual build safely, whether attack targeting feels continuous between segmented hitboxes, and whether mounted passengers remain comfortable during extreme motion.
