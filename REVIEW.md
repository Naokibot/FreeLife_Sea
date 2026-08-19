# FreeLifeMarineMobs 1.7.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- reduce visibly jerky movement;
- make marine mobs approach thrown marine food faster;
- add a dedicated height-holding controller for a two-block-deep pool;
- add more hitbox coverage;
- document the show-start command.

## Review findings and design choices

1. **Two-tick visual updates were the main visible source of stepping at high speed.**
   - At speed level 10 the carrier can travel 1.0 block per tick.
   - Updating BlockDisplay positions only every two ticks can therefore create a two-block visual step before interpolation.
   - Version 1.7 updates displays every tick and uses a one-tick teleport duration.
   - Transformation interpolation remains active for body/tail animation.

2. **Carrier rotation and visual rotation should not be identical at every instant.**
   - The carrier must react immediately for navigation.
   - The visible body now keeps a separate smoothed yaw/pitch state and eases toward carrier rotation.
   - This reduces snapping without slowing the actual AI response.

3. **Food response was delayed by both scanning and conservative speed selection.**
   - The food scan interval is reduced from six ticks to two ticks.
   - Orcas use level 9 at long range, level 8 at medium range and level 6 for final approach.
   - Sharks use levels 8, 7 and 5 respectively.
   - Pursuit acceleration is increased, while the lower final-approach tier limits overshoot.
   - Crab food movement is also increased while preserving sideways locomotion.

4. **Two-block pools need continuous height holding, not only shallow jump preparation.**
   - Version 1.6 shortened the breach pre-dive but ordinary swimming could still drift into the lower layer.
   - Version 1.7 detects the lower layer (water above, floor below) and applies a stronger upward recovery.
   - In the upper layer, vertical command is clamped to reduce repeated surface/floor oscillation.
   - Gravity stays enabled; this is active swimming control rather than no-gravity hovering.

5. **Existing hitbox segmentation still left avoidable targeting gaps.**
   - Orca coverage increases from 6 to 10 overlapping segments.
   - Shark coverage increases from 5 to 8.
   - Crab coverage increases from 3 to 5, including body and claw-side coverage.
   - Hitboxes remain `Interaction` entities for attack/right-click resolution, not solid push bodies.

6. **Show start should remain simple.**
   - `/marine show start` starts the first configured show.
   - `/marine show start orca-show` explicitly starts the default `orca-show` definition.
   - The existing show center/time/facing commands are unchanged.

## Tests

The unit suite now additionally verifies:

- lower-layer upward recovery in a two-block pool;
- upper-layer vertical clamp;
- far/medium/final food pursuit speed selection;
- two-tick food scanning;
- increased pursuit acceleration;
- 10 orca, 8 shark and 5 crab hitbox segments;
- positive hitbox dimensions for every custom mob.

Existing speed-level, gravity/shallow-water, mob-type and show-schedule tests remain.

## Verification boundary

CI can prove source/API compatibility, deterministic profile calculations, unit tests, JAR structure, Java class version and dependency packaging.

CI cannot prove subjective client smoothness or the exact result of Horse/Slime water physics in a specific aquarium. A staging server remains necessary to judge visible jitter at 20 blocks/s, two-block-pool floor clearance, high-speed food approach near walls, and whether the denser targeting hitboxes feel continuous in actual combat/interaction.
