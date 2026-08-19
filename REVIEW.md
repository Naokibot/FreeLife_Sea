# FreeLifeMarineMobs 1.3.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- allow autonomous orca shows to start at specified real-world times;
- play music while an orca show is running.

## Review findings and fixes

1. **Scheduled shows must not violate command-only marine mob spawning.**
   - The show manager never creates an orca.
   - It selects the nearest already-spawned orcas inside the configured control radius.
   - If no orca is available, the occurrence is skipped.

2. **A once-per-second scheduler can otherwise start the same show repeatedly during one matching minute.**
   - Each scheduled occurrence is keyed by show id, local date, and local scheduled time.
   - The occurrence is fired only once.
   - Old occurrence keys are pruned.

3. **Server host time zone must not determine the schedule accidentally.**
   - Each show has an explicit Java `ZoneId`.
   - Invalid zones fall back to `Asia/Tokyo` with a warning.
   - Clock parsing is isolated in the Bukkit-independent `ShowSchedule` helper and unit tested.

4. **Show AI and normal autonomous AI must never control an orca simultaneously.**
   - `MarineMob` has an explicit show-control state.
   - Native Horse AI and normal marine autonomous movement are bypassed while that state is active.
   - Manual stop, normal completion, plugin shutdown, and world/mob failure all release show control.

5. **Players must not be able to fight the choreography with native riding input.**
   - All riders are ejected when an orca enters show control.
   - Right-click mounting is rejected for a show-controlled orca.
   - Normal eight-seat riding returns when the show finishes.

6. **The show should use movement rather than teleporting the model through the pool.**
   - Gathering, formation pass, regrouping, and exit use velocity-based target guidance.
   - The jump wave uses an actual horizontal launch vector plus vertical velocity and gravity.
   - Existing water transition detection remains active, so water entry/exit still emits splash and bubble effects.

7. **Multiple orcas need stable formation spacing.**
   - The show derives lateral offsets around the configured center line.
   - Up to eight orcas are supported.
   - Jump launches are staggered by 16 ticks to create a wave instead of stacking all bodies in one trajectory.

8. **Music must not require copyrighted downloaded audio or a mandatory resource pack.**
   - Version 1.3.0 uses short vanilla note-block sounds only.
   - Harp melody, bass, bass drum, and bell accents are played in the `MUSIC` sound category.
   - Only players inside the configured audience radius hear the music.
   - Because every cue is a short one-shot sound, stopping a show does not leave a long audio stream playing.

9. **Configuration defaults must be safe.**
   - The example schedule ships with `enabled: false`.
   - Operators must set the real pool center/world and explicitly enable scheduled execution.
   - Manual `/marine show start` remains available for setup tests even while the schedule is disabled.

10. **A show that loses its world or all controlled orcas must fail closed.**
    - The manager cancels and releases show control instead of continuing with stale entity references.

## Commands

- `/marine show list`
- `/marine show start [show-id]`
- `/marine show stop`
- `/marine show status`
- `/marine show reload`

Permission: `freelifemarine.show` (default: op).

## Unit coverage

The test suite retains the marine model/type checks and adds schedule tests for:

- valid `H:mm` / `HH:mm` values;
- rejection of invalid hours, minutes, and malformed strings;
- sorting and de-duplication;
- valid zone parsing and invalid-zone fallback.

## Verification boundary

CI verifies compilation against the real Spigot 1.21.1 API, unit tests, JAR integrity, `plugin.yml`, `config.yml`, Java 21 class version 65, and absence of shaded Bukkit classes.

CI cannot prove that the default derived choreography fits a specific physical aquarium build. Pool dimensions, water depth, jump landing positions, spectator sound level, simultaneous multi-orca spacing, and visual timing still require a staging-server E2E test with the target map.
