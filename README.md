# FreeLifeMarineMobs

FreeLifeMarineMobs is a standalone Spigot 1.21.1 plugin for command-only animated shark, orca, and crab entities.

Version 1.2.0 focuses on two changes: native mounted steering and a higher-detail voxel model for sharks and orcas.

## Requirements

- Java 21
- Spigot 1.21.1

## Commands

```text
/marine spawn shark
/marine spawn orca
/marine spawn crab
```

Permission: `freelifemarine.spawn` (default: server operators).

There is no natural spawning, spawn egg, crafting recipe, or scheduled automatic spawn path.

## Riding controls

Sharks and orcas now use an invisible, tamed, saddled vanilla horse as the pilot carrier. The first rider therefore controls the creature through Minecraft's normal mounted movement input instead of the old camera-direction autopilot.

- normal mounted forward/back/left/right input drives the pilot carrier;
- the normal mounted jump control is preserved;
- the plugin caps excessive speed and adds only a small water-drag assist;
- on land, shark/orca horizontal speed is reduced;
- when no pilot is present, the existing autonomous marine movement resumes.

The orca still supports eight players. The first rider is the pilot; the other seven use independent invisible passenger seats that follow the orca.

## Graphics

### Orca

The orca is still about 9-10 blocks long, but the body is now built from 48 display parts instead of 28. The body uses shorter tapered sections to reduce the stepped silhouette. Separate details include:

- narrowing rostrum/head sections;
- broad torso and tapered tail stock;
- segmented white ventral surface;
- two-piece white eye patches on each side;
- two-piece gray saddle patches on each side;
- four-stage swept dorsal fin;
- two-stage pectoral flippers;
- four animated horizontal fluke sections;
- mouth edge, small eyes, and blowhole detail.

### Shark

The shark now uses 39 display parts. Details include:

- nine tapered dorsal body sections;
- five ventral sections;
- eyes and mouth edge;
- five gill slits on each side;
- primary and secondary dorsal fins;
- pectoral and pelvic fins;
- tail stock and asymmetric upper/lower caudal lobes.

Shark tail motion remains side-to-side. Orca flukes remain vertical.

## Water effects and autonomous movement

Water entry/exit splashes, bubbles, surface wake, and the orca blowhole-like surface mist remain enabled. When no player is piloting, sharks and orcas resume autonomous swimming and seek nearby water if stranded. Crabs continue lateral autonomous walking.

## Rendering limitation

The plugin uses only vanilla `BlockDisplay` entities. It does not bundle OBJ/GLTF models or a resource pack. This keeps the plugin usable by an unmodified vanilla client, but it also means the animals remain a high-detail voxel approximation rather than a smooth arbitrary 3D mesh.

Visual references are used only for proportions and markings; no external model asset is copied into the repository or JAR.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeMarineMobs-1.2.0-Spigot-1.21.1.jar
```

## Verification

GitHub Actions compiles and tests against the real Spigot 1.21.1 API, validates the JAR, checks Java 21 class version 65, and checks that Bukkit classes are not shaded into the plugin.

A real Minecraft client/server staging test is still required for final riding feel, deep-water horse-carrier behavior, eight-player seat spacing, and appearance from all viewing angles.
