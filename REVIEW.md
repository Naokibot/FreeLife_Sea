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

The 1.9.0 fall recovery still relied on `Entity#setVelocity` to make the invisible Horse/Slime carrier actually change position. The user's live-server report after 1.9.0 shows that assumption is not sufficiently robust for this environment: the carrier may retain or accept a velocity value without producing the expected positional fall.

## 1.9.1 deterministic manual airborne gravity

Version 1.9.1 removes the remaining dependency on carrier-native velocity integration while the carrier is unsupported in air.

The final controller now:

1. detects a tracked marine carrier that is outside water and lacks solid support;
2. captures its current horizontal and vertical velocity once on entry to air;
3. temporarily disables carrier-native gravity for that airborne phase so two physics systems cannot fight each other;
4. integrates gravity itself using `0.08` blocks/tick² and `0.98` vertical drag;
5. applies the resulting position directly with `Entity#teleport` every tick;
6. subdivides movement into at most 0.20-block sweep increments so a fast jump cannot skip through water or a solid block in one update;
7. restores normal gravity and aquatic control immediately on water re-entry;
8. restores normal gravity with zero velocity when a solid collision is reached.

This is intentionally more deterministic than the 1.8.1 and 1.9.0 approaches. Even if the invisible Horse ignores velocity-based falling on the production server, its Y coordinate is explicitly changed during unsupported-air ticks.

The visible BlockDisplay model and Interaction hitboxes remain follower entities driven from the carrier, so they inherit the carrier's corrected position on the next normal marine update tick.

### Riding interaction

Player-facing orca riding remains unchanged from 1.9.0:

- the pilot's horizontal look direction controls travel in a two-block-deep pool;
- deeper water allows limited pitch-based ascent/descent;
- ridden-orca target speed remains 56 blocks/s (2.8 blocks/tick);
- once the carrier is unsupported in air, rider propulsion yields to the deterministic airborne integrator.

## Tests and verification boundary

Unit tests cover:

- ten-level marine speeds and the exact 20 blocks/s level-10 mapping;
- 56 blocks/s ridden-orca tuning;
- pursuit, activity, shallow-water, hitbox, and show-schedule behavior;
- deterministic gravity turning a zero-vertical-velocity apex into descent;
- continued acceleration over repeated airborne ticks;
- sweep subdivision for large displacement.

CI can verify compilation against Spigot 1.21.1, unit tests, JAR integrity, Java 21 class version 65, and packaging. It cannot reproduce the user's exact production server or prove the visual result of manual airborne teleport integration in a live Minecraft client. A staging-server E2E run is still required before claiming the production hover symptom is definitively eliminated.
