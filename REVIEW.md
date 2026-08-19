# FreeLifeMarineMobs 1.4.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- reduce the oversized crab;
- make autonomous swimming look more biological;
- add a custom `海の餌` item that attracts marine mobs;
- configure orca-show center and time from Minecraft commands;
- improve shark/orca graphics again.

## Review findings and design choices

1. **The crab was proportionally too large.**
   - Interaction width was reduced from 3.2 to 1.35 blocks and height from 1.6 to 0.72 blocks.
   - The visible shell, claws, eyes, and all eight legs were rescaled with it.
   - Cruise speed was reduced and short pauses were added so the smaller crab does not look like a fast sliding vehicle.

2. **The old autonomous aquatic controller looked synthetic.**
   - It replaced velocity every tick at near-constant speed and used a fixed sine wave for vertical motion.
   - Version 1.4 stores a changing natural intent: target heading, speed factor, vertical intent, and behavior duration.
   - Velocity is blended toward the desired velocity, producing acceleration/deceleration instead of abrupt speed replacement.
   - Sharks use steadier speed, longer turns, and smaller depth changes.
   - Orcas vary speed more and periodically rise to breathe before resuming deeper swimming.

3. **Food attraction must not be spoofable by an anvil rename.**
   - `海の餌` uses a `PersistentDataContainer` marker under the plugin namespace.
   - A renamed ordinary cod item is therefore not accepted as marine food.
   - Players can hold it as a lure, drop it as a consumable target, or right-click a mob to feed it directly.

4. **Food must not interfere with show control or native riding.**
   - Show-controlled orcas ignore food completely.
   - A ridden shark/orca remains under native mounted control.
   - Food attraction only takes over the ordinary autonomous path when no pilot/show controller is active.

5. **Dropped-food behavior needs a bounded search.**
   - Each species has its own attraction range.
   - Nearby dropped entities are searched only inside that bounded cube and the cached target is refreshed every four ticks.
   - This avoids a global world-item scan on every movement tick.

6. **Show configuration should survive restart.**
   - `set-center`, `set-facing`, schedule edits, and enable/disable write back to `config.yml` and reload the definitions.
   - `set-center` also stores the player's current world.
   - Time input is validated through the existing `ShowSchedule` parser and normalized to `HH:mm`.

7. **The previous model detail floor still left stepped silhouettes.**
   - The orca is generated from 17 short tapered body slices plus ventral, marking, fin, peduncle, and fluke detail, exceeding 60 display parts.
   - The shark uses 13 tapered body slices plus ventral, gill, fin, posterior-body, peduncle, keel, and caudal detail, exceeding 50 display parts.
   - Posterior shark body motion and orca peduncle motion are now animated separately from the final tail/fluke pieces.

8. **Pure Spigot rendering still has a ceiling.**
   - The plugin intentionally remains resource-pack-free and uses transformed vanilla `BlockDisplay` cuboids.
   - It can improve silhouette, markings, proportions, and animation, but cannot become a smooth skinned high-poly animal without a resource-pack/model pipeline.

## Tests

The unit suite checks supported mob names, eight-seat orca behavior, the new model detail floors, reduced crab dimensions, species-specific attraction/movement parameters, 10 health, valid display scales, and the existing show schedule parser.

## Verification boundary

CI verifies source compatibility with the declared Spigot 1.21.1 API, unit tests, JAR integrity, `plugin.yml`, `config.yml`, Java 21 class version, the presence of the marine-food/show classes, and absence of shaded Bukkit classes.

CI cannot prove the subjective look of movement or models. A staging server is still needed to assess food attraction around the actual aquarium geometry, crab apparent size beside players, show-center placement, eight-rider spacing, and final shark/orca appearance from all angles.
