# FreeLifeMarineMobs 1.5.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- make orca swimming faster;
- provide ten speed levels, with the fastest reaching 20 blocks/s, and use the levels for different behavior;
- allow autonomous swimming animals to jump/breach;
- allow jumps up to 10 blocks;
- add hit detection to every custom marine mob.

## Review findings and design choices

1. **Speed values needed one common unit and one source of truth.**
   - `MarineSpeedLevel` defines exactly ten levels.
   - Level N is `2 * N` blocks/s, so level 10 is exactly 20 blocks/s = 1.0 block/tick.
   - Existing show velocity requests are quantized to the nearest defined level instead of maintaining a separate arbitrary speed scale.

2. **The maximum speed should not become the default cruise speed.**
   - Orcas roam mainly at levels 3-6 and use higher levels for food pursuit, surfacing, and breach charging.
   - Sharks use a narrower, generally lower range.
   - Edge recovery drops to a low speed so an animal does not hit a pool wall at a 20-block/s burst.
   - Mounted orcas are allowed a higher assisted cap than before, but normal Horse steering remains authoritative.

3. **Autonomous jumps need preparation rather than an instantaneous upward velocity.**
   - The new state machine is `NONE -> DIVE -> CHARGE -> AIRBORNE -> NONE`.
   - A jump starts only near a water surface with at least two blocks of water below and a clear non-solid column above.
   - Orcas choose a 3-10 block target; sharks use smaller 2-5 block breaches and schedule them less often.
   - Launch height maps to a capped vertical velocity table. The 10-block entry uses 1.344 blocks/tick, selected to target approximately a 10-block apex under ordinary Minecraft entity gravity.
   - The 10-block value is a simulation/design target, not a claim of pixel-exact live-server height under every server implementation or lag condition.

4. **Fast jumping must not fight the other controllers.**
   - Food attraction is not allowed to take over once a breach sequence has started.
   - Native mounted control cancels autonomous jump state.
   - Show control cancels autonomous jump state and retains the existing show controller.
   - Normal autonomous intent is regenerated after water re-entry.

5. **One large interaction box did not match the visible bodies.**
   - Orca now uses six `Interaction` segments along its body.
   - Shark uses five.
   - Crab uses three, including lateral claw coverage.
   - Every segment is mapped to the owning `MarineMob`, so feeding, riding, and damage lookup resolve through the same custom-mob state.
   - These are targeting/interaction hitboxes. The movement carrier deliberately remains non-collidable; solid push physics is not added because it would make large composite animals snag on pool walls and push passengers unexpectedly.

6. **The existing display animation must scale sensibly at higher speed.**
   - Tail/fluke/body animation still derives from actual horizontal velocity, but its scale is bounded to avoid extreme oscillation at level 10.
   - Existing splash, wake, breathing, food, riding, eight-seat orca, and show-music behavior remains intact.

7. **Pure Spigot limitations remain.**
   - `Interaction` provides configurable width/height and records interactions, but it is not a solid collision body.
   - BlockDisplay models remain transformed vanilla cuboids rather than skinned meshes.

## Tests

The unit suite now additionally checks:

- exactly ten speed levels;
- level 10 = 20 blocks/s = 1.0 block/tick;
- invalid speed-level rejection;
- show-speed quantization;
- positive hitbox dimensions for every custom mob;
- six orca, five shark, and three crab hitbox segments.

Existing mob-type and show-schedule tests remain.

## Verification boundary

CI verifies source compatibility with the declared Spigot 1.21.1 API, unit tests, JAR integrity, `plugin.yml`, `config.yml`, Java 21 class version, speed/hitbox classes, food/show classes, and absence of shaded Bukkit classes.

CI does not prove live movement feel. A staging server is still required to measure real breach apex/landing position, 20-block/s behavior in the actual pool, rider stability during high-speed motion, attack targeting between adjacent hitbox segments, and any anti-cheat or server-configuration interaction.
