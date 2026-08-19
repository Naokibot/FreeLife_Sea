# FreeLifeMarineMobs 1.9.0 review

## Live issue addressed

Version 1.8.1 still allowed a reported orca carrier to remain apparently suspended in air on the production server. The 1.9.0 fix no longer assumes that the hidden Horse carrier will always accumulate usable native downward motion by itself.

`MarineFinalMotionController` runs after the normal marine movement service. For plugin-owned Horse/Slime anchors it uses a stricter water-contact test than the autonomous controller. If an anchor is neither in water nor on ground, Bukkit gravity is kept enabled and all rider steering is skipped. If successive Y positions show that the entity has stopped descending while vertical velocity is near zero or only weakly upward, a small downward impulse is injected. Normal falling and a real upward jump are not replaced.

## Ridden orca control

Ridden orcas are no longer dependent on vanilla Horse AI for their final movement. The final controller disables Horse AI and sets the orca's velocity from the pilot's facing direction every tick while the carrier is in water.

- Previous ridden-orca cap: 14 blocks/s.
- Requested multiplier: 4x.
- New ridden-orca target: 56 blocks/s = 2.8 blocks/tick.
- In a two-block-deep pool the player's horizontal facing direction controls travel and vertical velocity is tightly limited so the shallow-water controller can keep the carrier in the water column.
- In deeper water a limited amount of player pitch contributes to ascent/descent.
- Once the carrier actually leaves the water, rider propulsion is no longer applied; gravity/fall recovery takes priority so the rider cannot turn the orca into sustained flight.

## Verification boundary

Unit tests cover the exact 4x speed calculation and the stationary-air detection/fall-kick policy. GitHub Actions compiles the plugin against Spigot 1.21.1 / Java 21 and inspects the generated JAR. A real server/client run is still required to prove the feel of 56 blocks/s, collision behavior at that speed, and the exact live-server resolution of the previously reported hovering symptom.
