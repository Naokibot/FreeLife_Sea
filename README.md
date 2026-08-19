# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin that adds command-only animated marine creatures built entirely with vanilla display entities.

Version 1.1.0 contains:

- shark
- orca
- crab

All three use plugin-managed health of 10. Sharks and orcas can be ridden. Orcas have eight independent rider seats.

## Requirements

- Java 21
- Spigot 1.21.1

## Commands

```text
/marine spawn shark
/marine spawn orca
/marine spawn crab
```

Permission:

```text
freelifemarine.spawn
```

The permission defaults to server operators. There is no natural spawn listener, spawn egg, recipe, or scheduled automatic spawn path.

## Creature behavior

### Shark

- one rider seat
- autonomous swimming when nobody is riding
- horizontal tail sweep while swimming
- tapered body, white underside, eyes, gill details, dorsal fin, pectoral fins, and animated caudal fin
- rider steering uses the rider's look direction in water
- stranded sharks make small movements toward nearby water instead of flying over land

### Orca

- approximately 9-10 blocks from snout to tail-fluke extent, intentionally much larger than version 1.0.0
- eight independent rider seats arranged over the back
- the first occupied seat controls movement; the other seven riders are passengers
- autonomous swimming when not ridden
- vertically oscillating tail flukes rather than shark-style side-to-side tail motion
- black dorsal body, white ventral body, white eye patches, light saddle patches, large pectoral flippers, tall multi-part dorsal fin, tail stock, and separate horizontal flukes
- surface wake particles while moving quickly
- periodic blowhole-like mist/splash effect near the surface

### Crab

- command-only spawning with `/marine spawn crab`
- 10 health
- not rideable
- broad shell, eyes, claws, and eight animated walking legs
- moves sideways rather than walking forward
- can move on land and in shallow water

## Water entry and exit effects

The plugin tracks whether each creature is in water. When that state changes it emits a splash effect and plays the generic splash sound. Entering water also emits bubbles.

Particle amount scales with creature size, so an orca produces a much larger splash than a shark or crab. Fast swimming near the surface produces a smaller repeated wake.

Water detection includes:

- normal water
- bubble columns
- waterlogged blocks

## Movement and animation

Movement runs on the server tick. Visual display updates run every two ticks with client interpolation enabled to reduce jitter and unnecessary transformation updates.

Aquatic creatures wander and turn while submerged. They avoid continuing straight when the next sampled position leaves the water. If stranded, they search a small nearby area for water and move toward it with short flop-like movement.

Crabs use lateral movement relative to body orientation, occasionally reverse direction, and animate alternating leg groups.

## 3D model approach

A Spigot plugin cannot send an arbitrary OBJ/GLTF mesh to an unmodified vanilla client. Therefore this project does not bundle downloaded model files or textures. The models are original low-poly/voxel approximations composed from vanilla `BlockDisplay` entities.

Public 3D models used only as visual proportion/silhouette references:

- Shark by Quaternius on Poly Pizza — Public Domain (CC0): https://poly.pizza/m/YYsK3gRCBZ
- Orca by Poly by Google on Poly Pizza — Creative Commons Attribution: https://poly.pizza/m/5p9B6IebY-A
- Crab Enemy by Quaternius on Poly Pizza — Public Domain (CC0): https://poly.pizza/m/Gs3yfsV5lB

Biological proportion and motion references were also checked against NOAA Fisheries material for killer whales and sharks. No third-party mesh, texture, animation, or model file is included in this repository or JAR.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.1.0-Spigot-1.21.1.jar
```

## Verification

GitHub Actions compiles and tests against the real Spigot 1.21.1 API, verifies the JAR archive, checks Java 21 class version 65, and verifies that Bukkit classes are not shaded into the plugin.

CI cannot replace a real Minecraft client/server staging test. Appearance, eight-player seat spacing, rider movement feel, water-transition particle placement, collision behavior, and long-running movement should still be verified on a staging server before production use.
