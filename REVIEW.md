# FreeLifeMarineMobs 1.2.0 review

Target: Spigot 1.21.1 / Java 21

## Requested changes

- make riding controllable like a horse rather than simply following the rider's camera direction;
- make shark/orca graphics closer to real animals.

## Review findings and design choices

1. **The old rider path was not horse-like.**
   - Version 1.1 drove the hidden anchor directly from the pilot's look vector.
   - Version 1.2 replaces the pilot carrier of rideable creatures with an invisible, tamed, saddled vanilla horse.
   - The player therefore sends the normal mounted movement input to a native Minecraft rideable entity.
   - The plugin no longer synthesizes steering from camera direction.

2. **Using an experimental/new input API would add avoidable version risk.**
   - The target remains Spigot 1.21.1.
   - Native horse steering is used instead of depending on later/experimental player-input APIs or NMS packet hooks.

3. **A hidden horse has stronger water drag than a real shark/orca.**
   - The plugin preserves native steering and only applies a small capped horizontal assist while in water.
   - It does not overwrite the pilot's direction every tick.
   - On land, shark/orca speed is damped to discourage unrealistic land travel.

4. **Native Horse AI must not fight the marine autonomous controller.**
   - Horse AI is enabled only while a player occupies the pilot seat.
   - With no pilot, horse AI is disabled and the plugin's autonomous aquatic controller is the only movement controller.

5. **The old 28-part orca still had a visibly stepped body.**
   - The new orca has 48 display parts.
   - Body sections are shorter and taper progressively from rostrum to peduncle.
   - Eye patches, saddle patches, underside, dorsal fin, pectoral fins, flukes, mouth edge, eyes, and blowhole are independently modeled.

6. **The shark silhouette needed the same treatment.**
   - The shark now has 39 display parts.
   - It includes nine tapered body sections, five underside sections, ten gill slits, multiple fins, mouth/eye details, and asymmetric caudal lobes.

7. **Eight-player orca seating must remain intact after changing the pilot carrier.**
   - Seat 1 is now the native horse carrier and is the pilot position.
   - Seats 2-8 remain independent invisible armor-stand passenger seats.
   - Passenger seats follow the native pilot carrier with velocity correction.

8. **Autonomous behavior must resume after the pilot dismounts.**
   - The same anchor is reused.
   - When it has no player passenger, custom autonomous water movement resumes.
   - Gravity is disabled for autonomous submerged motion and restored for native ridden movement.

9. **The graphical ceiling of pure Spigot must be explicit.**
   - Vanilla `BlockDisplay` supports transformed block cuboids, not arbitrary external meshes.
   - No third-party OBJ/GLTF/model bytes are embedded.
   - A resource-pack-backed model system would be needed for a genuinely smooth high-poly animal.

## Tests

The unit suite checks command parsing, eight orca seats, one shark seat, crab non-rideability, 10 health, positive model dimensions, and the new minimum detail floors of 48 orca parts and 39 shark parts.

## Verification boundary

CI verifies source compatibility with the real Spigot 1.21.1 API, unit tests, JAR integrity, Java 21 class version, and absence of shaded Bukkit classes.

CI cannot prove real client steering feel. In particular, deep-water mounted behavior of the hidden horse, jump behavior, eight simultaneous human riders, collision feel, and final visual proportions still require a staging-server E2E test.
