# FreeLifeMarineMobs 1.8.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- make autonomous swimming visibly more active;
- investigate the attached production log where the orca show would not start;
- preserve the existing smooth rendering, food pursuit, two-block-depth control, hitboxes, riding, show music, and show configuration commands.

## Production-log findings

1. **The repeated stack traces are produced by Residence, not by the show controller.**
   - The server reports `Could not pass event CreatureSpawnEvent to Residence v6.0.2.4`.
   - The root cause is `java.lang.NoSuchFieldError`: Residence expects `net.Zrips.CMILib.Version.Version.v26_2_0`, but the loaded CMILib class does not contain that field.
   - FreeLifeMarineMobs appears in the stack because it is spawning a Horse or ArmorStand that fires Bukkit's normal `CreatureSpawnEvent`; Residence then fails while handling that event.
   - FreeLifeMarineMobs cannot safely patch another plugin's incompatible binary dependency. Residence and CMILib must be updated/downgraded as a mutually compatible pair on the server.

2. **The show definition itself did load.**
   - The log contains successful `Loaded 1 orca show definition(s).` messages after `/marine show reload`.
   - The log contains several `/marine show start` and `/marine show start orca-show` commands without a FreeLifeMarineMobs exception immediately following them.

3. **The log does not show a saved show center command.**
   - The bundled default center is `world`, `(0.5, 63.0, 0.5)` with a 48-block control radius.
   - If the spawned orcas were elsewhere, the old manual-start path selected zero orcas and returned that only to the command sender's chat, so the failure reason was not visible in the server log.

## Version 1.8 design changes

### More active autonomous swimming

`MarineActivityProfile` makes normal roaming more energetic without keeping animals at maximum speed continuously:

- Orca roaming: speed levels 4-7, with occasional short level-8 bursts.
- Shark roaming: speed levels 4-6, with occasional level-7 bursts.
- Orca intent duration: 50-120 ticks; yaw change up to about 55 degrees; stronger vertical intent.
- Shark intent duration: 70-150 ticks; yaw change up to about 38 degrees.
- Orca breach opportunity delay: 260-650 ticks.
- Shark breach opportunity delay: 480-1200 ticks.
- Orca breathing recurrence is shortened.

Wall avoidance, two-block-depth holding, food pursuit, riding, and show control still override roaming when necessary.

### Manual-show fallback and diagnostics

Manual player starts now follow this order:

1. try the saved show center and normal `control-radius`;
2. if no orca is selected, search around the command player using at least a 96-block fallback radius;
3. if an orca is found, use the player's location as a temporary center for that performance only;
4. tell the operator to use `/marine show set-center` to persist the correct center.

If no orca can be selected, the returned message includes the saved center, control radius, tracked usable-orca count, and nearest tracked-orca distance. Manual start results are also written to the server log.

Scheduled shows remain strict: they still use the configured center and do not silently move the schedule's venue to a random player's location.

### Residence-error reduction

Previously an orca spawn immediately created the invisible Horse plus all seven extra ArmorStand passenger seats. Each living-entity spawn can fire events observed by Residence.

Version 1.8 creates additional passenger ArmorStands only when a second through eighth rider actually needs a seat. This substantially reduces unnecessary `CreatureSpawnEvent` traffic at orca creation, but the Horse anchor itself still legitimately fires a spawn event, so an incompatible Residence/CMILib installation can still emit an error until those plugins are fixed.

## Tests

The new activity-profile tests verify:

- orca roaming levels 4-7 and level-8 burst profile;
- shark roaming levels 4-6 and level-7 burst profile;
- shorter autonomous behavior windows;
- shorter breach intervals;
- wider orca turn intent.

Existing show-schedule, shallow-water/gravity, pursuit, speed-level, hitbox, and mob-type tests remain.

## Verification boundary

CI can verify compilation against Spigot 1.21.1, deterministic activity-profile values, unit tests, JAR integrity, Java 21 class version 65, and packaging.

CI cannot reproduce the user's exact Residence/CMILib JAR combination, prove that Residence stops logging errors after those third-party JARs are corrected, or judge the subjective activity level in a real Minecraft client. A staging-server E2E run remains required for those points.
