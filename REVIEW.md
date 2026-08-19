# FreeLifeMarineMobs 1.1.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- make marine creatures more realistic
- add crab
- make creatures move autonomously
- add splash/bubble behavior when entering or leaving water
- enlarge the orca
- allow up to eight players to ride an orca
- substantially improve the orca model quality

## Review findings and fixes

1. **The old orca silhouette was too small and too coarse.**
   - Rebuilt it as a roughly 9-10 block long model.
   - Increased the visual model to 28 separate parts.
   - Separated dorsal body, white underside, eye patches, saddle patches, pectoral flippers, multi-stage dorsal fin, tail stock, and two horizontal tail flukes.

2. **Using the old single passenger carrier cannot provide eight controlled seat positions.**
   - Orca now owns eight independent invisible marker armor-stand seats.
   - Seat offsets are explicitly defined over the back.
   - The first occupied seat acts as pilot and the other occupied seats follow as passengers.
   - Occupied seats are velocity-corrected toward their desired offsets rather than teleported every tick, reducing the risk of vehicle teleport behavior ejecting riders.

3. **Shark and orca tail motion should not be identical.**
   - Shark tail parts sweep side-to-side around the vertical axis.
   - Orca flukes oscillate vertically around the lateral axis.

4. **Static creatures did not satisfy the movement requirement.**
   - Sharks and orcas now cruise autonomously in water and periodically change heading.
   - They turn away when the sampled position ahead no longer contains water.
   - When stranded, they periodically search a small nearby radius for water and move toward it with restrained flop-like motion.

5. **The invisible Slime carrier can otherwise behave like a normal living mob underwater.**
   - Remaining air is refreshed every tick so a marine creature does not die because its implementation carrier drowns.
   - Fall distance is reset because health is managed by the plugin rather than by the hidden carrier's fall history.

6. **Water effects could spam if emitted continuously.**
   - Large splash/bubble effects are emitted only on water-state transitions.
   - A smaller surface wake is rate-limited to every five ticks and only while moving near the surface.

7. **Orcas need a surface behavior distinct from fish.**
   - Near the surface, an orca periodically produces a small blowhole-like cloud/splash effect.
   - This is visual approximation only; no attempt is made to simulate respiration physiology.

8. **Crab locomotion should not reuse fish movement.**
   - Crab has a separate lateral movement style.
   - It moves sideways relative to body orientation, periodically reverses direction, and alternates two leg-animation groups.
   - Crab is intentionally not rideable because that better matches the realism request.

9. **The old shark model was also visually sparse.**
   - Shark now has 23 parts including tapered body sections, underside, eyes, six gill marks, dorsal and pectoral fins, tail stock, and animated upper/lower caudal sections.

10. **Display-entity animation can become expensive when every part is transformed every server tick.**
    - Physics and rider control remain tick-based.
    - Display transforms update every two ticks and use client interpolation/teleport duration to smooth visual motion.

11. **Water detection must include more than a plain WATER material block.**
    - The check handles normal water, bubble columns, and Bukkit `Waterlogged` block data.

12. **Internet model references must not introduce licensing or redistribution problems.**
    - No OBJ, GLTF, texture, animation, or downloaded model bytes are committed or shaded into the JAR.
    - External models are used only as visual references and are listed in README with their published licenses.

13. **Command-only spawning must remain intact.**
    - `/marine spawn shark`, `/marine spawn orca`, and `/marine spawn crab` are the only plugin spawn paths.
    - No natural spawning listener or scheduled spawning was added.

## Test additions

The type test now checks:

- shark/orca/crab command parsing
- exactly eight orca seats
- one shark seat and zero crab seats
- minimum part-count floors for the upgraded models
- 10 health for all creature types
- positive dimensions for every model part

## Verification boundary

GitHub Actions compiles against the real Spigot 1.21.1 API, runs unit tests, validates the release JAR, checks Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

A real Minecraft client is not available in CI. Therefore the following must still be treated as staging-E2E items rather than CI-proven behavior: eight simultaneous human riders, passenger seat comfort/spacing, exact model appearance from all camera angles, particle placement at shorelines, collision feel, and long-duration autonomous navigation in real terrain.
