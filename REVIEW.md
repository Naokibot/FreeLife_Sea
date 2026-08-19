# FreeLifeMarineMobs 1.6.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- keep gravity enabled for the added marine mobs;
- support a pool whose usable water depth is only two blocks.

## Review findings and design choices

1. **The 1.5 autonomous controller disabled gravity underwater.**
   - Normal autonomous swimming used `setGravity(false)` while in water.
   - Show-guided swimming and holding also disabled gravity.
   - Version 1.6 keeps gravity enabled on the living movement carrier during ordinary swimming, show control, jump preparation, airborne motion, re-entry, and native riding.

2. **Gravity-on swimming needs active lift, not a static no-gravity state.**
   - `MarineWaterPhysics` adds a small `0.035` block/tick upward swimming component while an aquatic animal is actively controlling depth in water.
   - This lift is added to the animal's biological vertical intent; it does not turn gravity off.
   - Jump preparation uses explicit downward/upward thrust and therefore does not use the ordinary swimming lift.

3. **The old breach-start test was too deep for a two-block pool.**
   - Version 1.5 required water one and two blocks below the current position.
   - In a two-water-layer pool the second probe can already be the floor, so valid shallow pools could fail the test.
   - Version 1.6 requires the current water layer plus one water layer below. A non-water block two levels below marks the pool as shallow rather than invalid.

4. **A full-length dive would hit the floor in shallow water.**
   - Deep-water orcas retain the existing 24-tick dive preparation; deep-water sharks retain 18 ticks.
   - In a detected two-block pool both use a four-tick shallow preparation at only `-0.020` vertical velocity, roughly 0.08 block of commanded descent before upward acceleration.
   - Shallow charge time is also shortened to 32 ticks and upward charge thrust is increased so the animal can transition from the shallow preparation into the existing breach launch without needing extra depth.

5. **Show swimming must follow the same gravity rule.**
   - `guideShow` and `holdShow` now keep gravity enabled.
   - When the show-controlled orca is in water, the same swimming-lift term is applied so it can hold the intended show path without reverting to `setGravity(false)`.
   - Show jump launch already used gravity and remains gravity-driven in air.

6. **Movement carriers explicitly declare gravity.**
   - Invisible Horse carriers for sharks/orcas and the Slime carrier for crabs now explicitly call `setGravity(true)` at creation.
   - Display entities and marker passenger seats remain non-gravity visual/follower components; gravity applies to the living movement carrier that defines the custom mob's physical trajectory.

## Tests

The unit suite now additionally checks:

- two water layers are accepted as sufficient shallow-pool depth;
- current-water plus one-below-water is required;
- the shallow-water dive is four ticks;
- shallow dive vertical command is `-0.020`;
- shallow charge time is 32 ticks;
- the normal swimming lift is `0.035` block/tick and composes with vertical intent.

Existing speed-level, hitbox, mob-type, and show-schedule tests remain.

## Verification boundary

CI can verify source compatibility, the shallow-water decision logic, constants, tests, JAR integrity, Java 21 class version, and packaging.

It cannot prove the exact result of live Horse/Slime water physics, server tick timing, or pool geometry. A staging server is still required to confirm that gravity-on depth holding looks natural, the animal does not visibly clip a two-block-deep floor during jump preparation, and 3-10 block breach trajectories land correctly in the actual aquarium.
