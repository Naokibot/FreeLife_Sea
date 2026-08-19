# FreeLifeMarineMobs 1.0.0 review

Target: Spigot 1.21.1 / Java 21

## Scope

- Shark and orca only.
- Command-only spawning through `/marine spawn <shark|orca>`.
- 10 health managed by the plugin.
- Rideable through normal Bukkit passenger APIs.
- No resource pack and no downloaded 3D asset is redistributed.

## Review findings addressed

1. **An arbitrary internet OBJ/GLTF mesh cannot be rendered by a vanilla client from a Spigot JAR alone.**
   - The plugin uses vanilla `BlockDisplay` entities to create an original low-poly approximation.
   - Public model pages are used only as visual references.

2. **Natural spawning would violate the command-only requirement.**
   - No spawn listener, spawn egg, recipe, or scheduled spawn path exists.
   - `freelifemarine.spawn` defaults to operators.

3. **Using normal Slime death could create drops/splitting behavior.**
   - Damage to plugin-owned marine carriers is cancelled.
   - Health is tracked by the plugin and the entire model is removed at 0 health.

4. **Display entities have no useful collision hitbox for mounting.**
   - A vanilla `Interaction` entity follows the model and provides the right-click area.
   - The rider is mounted on an invisible Slime carrier through Bukkit's passenger API.

5. **Persisting a composite model across restart can leave orphaned display parts if chunks load at different times.**
   - The first version intentionally keeps the composite entities non-persistent and removes all tracked mobs on shutdown.

6. **A rider must be able to move the mount without NMS.**
   - The carrier velocity follows the rider's look direction using Bukkit `setVelocity`.
   - This avoids NMS and keeps the plugin on the Spigot API.

7. **Third-party model licensing must not leak into the JAR.**
   - No downloaded model bytes or textures are included.
   - The README records the visual reference pages and their published licenses.

8. **Looking exactly up/down can produce a zero horizontal vector when choosing a spawn point.**
   - The spawn calculation now falls back to the player's yaw when the flattened look vector is near zero.
   - This prevents normalization of a zero vector and invalid spawn coordinates.

## Verification boundary

CI compiles and tests against the real Spigot 1.21.1 API and validates the JAR. A real Minecraft client is not available in CI, so appearance, right-click hitbox feel, rider seat position, damage hit behavior, and in-water motion still require staging-server E2E testing before production use.
