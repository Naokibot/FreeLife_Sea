# FreeLifeMarineMobs

A standalone Spigot 1.21.1 plugin that adds command-only rideable shark and orca entities with 10 health.

## Requirements

- Java 21
- Spigot 1.21.1

## Commands

```text
/marine spawn shark
/marine spawn orca
```

Permission:

```text
freelifemarine.spawn
```

The permission defaults to server operators. There is no natural spawning, spawn egg, crafting recipe, or automatic generation path.

## Behavior

- Each shark/orca starts with exactly 10 plugin-managed health.
- Right-click the entity's hitbox to mount it.
- While mounted, it moves in the direction the rider is looking.
- Sneak dismount uses Minecraft's normal passenger behavior.
- The visual body is made from vanilla `BlockDisplay` entities and follows an invisible server-side carrier.
- Marine mobs created by this plugin are removed on plugin/server shutdown rather than persisted incompletely across restarts.

## 3D model approach

Pure Spigot cannot send an arbitrary downloaded OBJ/GLTF mesh to an unmodified vanilla client. This plugin therefore does **not** redistribute or import a third-party mesh. It builds an original low-poly approximation from vanilla `BlockDisplay` entities, so no resource pack or client mod is required.

The proportions and silhouette were visually referenced from these public 3D model pages:

- Shark by Quaternius on Poly Pizza — Public Domain (CC0): https://poly.pizza/m/YYsK3gRCBZ
- Orca by Poly by Google on Poly Pizza — Creative Commons Attribution: https://poly.pizza/m/5p9B6IebY-A

No mesh, texture, animation, or other third-party asset is included in this repository or JAR.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.0.0-Spigot-1.21.1.jar
```

## Verification

GitHub Actions compiles and tests against the real Spigot 1.21.1 API, verifies the JAR archive, checks Java 21 class version 65, and verifies that Bukkit classes are not shaded into the plugin.

A real Minecraft client is not available in CI, so appearance, right-click hitbox feel, rider seat position, damage hit behavior, and in-water motion still require staging-server E2E testing before production use.
