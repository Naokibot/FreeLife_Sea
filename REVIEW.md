# FreeLifeMarineMobs review

Target: Spigot 1.21.1 / Java 21

## 1.8 production-log findings

1. The repeated `CreatureSpawnEvent` stack traces came from Residence v6.0.2.4 using an incompatible CMILib version (`NoSuchFieldError` for `Version.v26_2_0`). FreeLifeMarineMobs can reduce unnecessary spawn events but cannot repair another plugin's binary dependency mismatch.
2. The orca show definition itself loaded successfully.
3. Manual show startup originally depended on the saved show center, so a wrong center could select zero tracked orcas without making the reason obvious in server logs. Version 1.8 added player-nearby fallback and diagnostics.

## 1.8 autonomous movement and show changes

- Orca roaming: speed levels 4-7, occasional level-8 bursts.
- Shark roaming: speed levels 4-6, occasional level-7 bursts.
- Shorter intent windows and more frequent breach opportunities.
- Manual show startup fallback around the command player.
- Lazy creation of extra orca passenger ArmorStands.

## 1.8.1 airborne-gravity hotfix

The 1.8.0 controller called `setGravity(true)`, but out-of-water recovery could still replace vertical velocity. Version 1.8.1 separated water, grounded land, and unsupported air so scripted vertical movement stopped outside water.

## 1.9.0 final-motion controller

The production server still reported airborne hovering. Version 1.9.0 added a final per-tick controller after normal marine AI, plus player-facing orca steering and a 56 blocks/s ridden-orca target (four times the previous 14 blocks/s maximum).

The 1.9.0 fall recovery still relied on `Entity#setVelocity` to make the invisible Horse/Slime carrier actually change position. The user's live-server report after 1.9.0 showed that assumption was not sufficiently robust for that environment.

## 1.9.1 deterministic manual airborne gravity

Version 1.9.1 removes the remaining dependency on carrier-native velocity integration while the carrier is unsupported in air.

The final controller:

1. detects a tracked marine carrier outside water and without solid support;
2. captures horizontal and vertical velocity on entry to air;
3. temporarily disables carrier-native gravity for that airborne phase;
4. integrates gravity using `0.08` blocks/tick² and `0.98` vertical drag;
5. applies the resulting position directly every tick;
6. subdivides movement into at most 0.20-block sweep increments;
7. restores normal aquatic control on water re-entry;
8. restores normal gravity on solid collision.

The visible BlockDisplay model and Interaction hitboxes remain followers of the carrier.

## 1.10.0 high-activity movement review

The new request was to make autonomous swimming substantially faster and more frequent, support 3-13 block orca breaches, and make ridden orcas follow the player's full gaze so looking up ascends and looking down dives.

### Autonomous swimming

The autonomous profile now uses:

- Orca ordinary roaming: levels 7-9 (14-18 blocks/s), with level-10 (20 blocks/s) bursts selected on about 55% of intent changes.
- Shark ordinary roaming: levels 6-8 (12-16 blocks/s), with level-9 (18 blocks/s) bursts on about 35% of intent changes.
- Orca intent window: 20-55 ticks.
- Shark intent window: 30-75 ticks.
- Wider yaw changes and stronger vertical intent than the 1.8 profile.
- Orca breach opportunity delay: 100-320 ticks.
- Shark breach opportunity delay: 200-600 ticks.

A final-pass minimum-speed guard is applied only when water exists ahead. This is deliberate: it prevents an earlier movement controller from leaving an animal moving slowly in open water without blindly overriding wall avoidance near pool geometry.

### Breaches

`MarineJumpProfile` defines:

- Orca height range: 3-13 blocks above the water surface.
- Shark height range: 3-7 blocks.
- A 14-block overhead-clearance probe for the largest orca breach.
- Increasing initial vertical velocity for each target height from 3 through 13 blocks.

The final motion controller has an independent breach scheduler. While a breach is beginning in water it reasserts the launch vector for a short bounded window so earlier same-tick controllers cannot cancel it. Once the carrier leaves water, the 1.9.1 deterministic airborne integrator takes over.

The configured height is a controller target. Exact live-client apex depends on the water-exit transition and therefore remains an E2E measurement, not something CI alone can prove.

### Three-dimensional rider gaze

Ridden orcas continue to target 56 blocks/s. The final controller now derives both horizontal and vertical movement from the pilot's gaze:

- looking left/right changes heading;
- looking up commands ascent;
- looking down commands a dive;
- deeper water permits a larger vertical component;
- two-block-deep pools clamp the vertical component and probe the surface/floor before accepting further ascent/descent;
- once outside water, rider propulsion stops and deterministic airborne gravity wins.

## Tests and verification boundary

Unit/CI coverage includes:

- ten-level marine speeds and the exact 20 blocks/s level-10 mapping;
- high-activity Orca/Shark autonomous profile ranges;
- 3-13 block orca jump-profile range and monotonic launch velocities;
- 56 blocks/s ridden-orca tuning;
- pursuit, shallow-water, hitbox, and show-schedule behavior;
- deterministic gravity turning a zero-vertical-velocity apex into descent;
- continued acceleration over repeated airborne ticks;
- sweep subdivision for large displacement.

CI can verify compilation against Spigot 1.21.1, unit tests, JAR integrity, Java 21 class version 65, and packaging. It cannot prove the exact production-server movement feel, exact 13-block visible apex, wall behavior at every pool geometry, or 56-block/s rider feel in a live client. Those remain staging-server E2E checks.
