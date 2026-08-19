package com.sagakenichi.freelifemarine;

import org.bukkit.Material;

import java.util.List;
import java.util.Locale;

public enum MarineMobType {

    SHARK(
            "Shark",
            MovementStyle.AQUATIC,
            10.0,
            0.42,
            0.25,
            7.4F,
            2.6F,
            List.of(new SeatOffset(0.0, 1.30, 0.0)),
            List.of(
                    p(Material.LIGHT_GRAY_CONCRETE, 3.40, 0.02, 0.0, 1.05F, 0.72F, 0.85F),
                    p(Material.GRAY_CONCRETE, 2.85, 0.05, 0.0, 1.35F, 0.95F, 1.05F),
                    p(Material.GRAY_CONCRETE, 2.15, 0.08, 0.0, 1.60F, 1.10F, 1.20F),
                    p(Material.GRAY_CONCRETE, 1.35, 0.10, 0.0, 1.78F, 1.22F, 1.30F),
                    p(Material.GRAY_CONCRETE, 0.45, 0.10, 0.0, 1.88F, 1.28F, 1.40F),
                    p(Material.GRAY_CONCRETE, -0.50, 0.08, 0.0, 1.76F, 1.18F, 1.35F),
                    p(Material.GRAY_CONCRETE, -1.40, 0.05, 0.0, 1.48F, 0.98F, 1.20F),
                    p(Material.GRAY_CONCRETE, -2.15, 0.02, 0.0, 1.10F, 0.72F, 1.00F),
                    p(Material.GRAY_CONCRETE, -2.78, 0.00, 0.0, 0.72F, 0.54F, 0.85F),
                    p(Material.WHITE_CONCRETE, 3.05, -0.43, 0.0, 0.92F, 0.18F, 1.05F),
                    p(Material.WHITE_CONCRETE, 2.15, -0.53, 0.0, 1.18F, 0.18F, 1.25F),
                    p(Material.WHITE_CONCRETE, 1.10, -0.59, 0.0, 1.35F, 0.18F, 1.35F),
                    p(Material.WHITE_CONCRETE, 0.00, -0.61, 0.0, 1.42F, 0.18F, 1.40F),
                    p(Material.WHITE_CONCRETE, -1.05, -0.52, 0.0, 1.20F, 0.16F, 1.22F),
                    p(Material.BLACK_CONCRETE, 3.14, 0.25, 0.55, 0.10F, 0.12F, 0.16F),
                    p(Material.BLACK_CONCRETE, 3.14, 0.25, -0.55, 0.10F, 0.12F, 0.16F),
                    r(Material.DEEPSLATE_TILES, 3.42, -0.22, 0.55, 0.06F, 0.07F, 0.65F, RotationAxis.Y, -2.0F),
                    r(Material.DEEPSLATE_TILES, 3.42, -0.22, -0.55, 0.06F, 0.07F, 0.65F, RotationAxis.Y, 2.0F),
                    p(Material.DEEPSLATE_TILES, 2.30, 0.00, 0.73, 0.055F, 0.46F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 2.12, 0.00, 0.76, 0.055F, 0.49F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 1.94, 0.00, 0.78, 0.055F, 0.51F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 1.76, 0.00, 0.79, 0.055F, 0.49F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 1.58, 0.00, 0.78, 0.055F, 0.46F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 2.30, 0.00, -0.73, 0.055F, 0.46F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 2.12, 0.00, -0.76, 0.055F, 0.49F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 1.94, 0.00, -0.78, 0.055F, 0.51F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 1.76, 0.00, -0.79, 0.055F, 0.49F, 0.08F),
                    p(Material.DEEPSLATE_TILES, 1.58, 0.00, -0.78, 0.055F, 0.46F, 0.08F),
                    r(Material.GRAY_CONCRETE, 0.18, 0.90, 0.0, 0.28F, 1.18F, 0.82F, RotationAxis.X, -8.0F),
                    r(Material.GRAY_CONCRETE, 0.02, 1.63, 0.0, 0.20F, 0.78F, 0.58F, RotationAxis.X, -12.0F),
                    r(Material.GRAY_CONCRETE, -1.85, 0.56, 0.0, 0.18F, 0.55F, 0.45F, RotationAxis.X, -8.0F),
                    r(Material.GRAY_CONCRETE, 0.95, -0.17, 1.25, 1.70F, 0.17F, 0.72F, RotationAxis.Y, 20.0F),
                    r(Material.GRAY_CONCRETE, 0.95, -0.17, -1.25, 1.70F, 0.17F, 0.72F, RotationAxis.Y, -20.0F),
                    r(Material.GRAY_CONCRETE, -1.10, -0.28, 0.84, 0.82F, 0.13F, 0.45F, RotationAxis.Y, 12.0F),
                    r(Material.GRAY_CONCRETE, -1.10, -0.28, -0.84, 0.82F, 0.13F, 0.45F, RotationAxis.Y, -12.0F),
                    p(Material.GRAY_CONCRETE, -3.30, 0.0, 0.0, 0.48F, 0.42F, 0.72F),
                    a(Material.GRAY_CONCRETE, -3.78, 0.56, 0.0, 0.30F, 1.45F, 0.78F, RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.0F),
                    a(Material.GRAY_CONCRETE, -3.75, -0.43, 0.0, 0.28F, 0.95F, 0.68F, RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.42F),
                    p(Material.LIGHT_GRAY_CONCRETE, 3.75, -0.11, 0.0, 0.58F, 0.22F, 0.52F)
            )
    ),

    ORCA(
            "Orca",
            MovementStyle.AQUATIC,
            10.0,
            0.38,
            0.21,
            10.8F,
            5.0F,
            List.of(
                    new SeatOffset(0.0, 1.62, 0.0),
                    new SeatOffset(1.90, 1.55, 0.72),
                    new SeatOffset(1.90, 1.55, -0.72),
                    new SeatOffset(0.65, 1.72, 0.82),
                    new SeatOffset(0.65, 1.72, -0.82),
                    new SeatOffset(-0.70, 1.65, 0.78),
                    new SeatOffset(-0.70, 1.65, -0.78),
                    new SeatOffset(-1.95, 1.43, 0.0)
            ),
            List.of(
                    p(Material.BLACK_CONCRETE, 4.55, 0.00, 0.0, 1.28F, 0.90F, 0.82F),
                    p(Material.BLACK_CONCRETE, 4.10, 0.06, 0.0, 1.72F, 1.18F, 0.92F),
                    p(Material.BLACK_CONCRETE, 3.58, 0.10, 0.0, 2.05F, 1.45F, 1.05F),
                    p(Material.BLACK_CONCRETE, 2.98, 0.12, 0.0, 2.35F, 1.67F, 1.15F),
                    p(Material.BLACK_CONCRETE, 2.30, 0.13, 0.0, 2.58F, 1.82F, 1.28F),
                    p(Material.BLACK_CONCRETE, 1.55, 0.14, 0.0, 2.72F, 1.94F, 1.34F),
                    p(Material.BLACK_CONCRETE, 0.78, 0.14, 0.0, 2.82F, 2.02F, 1.38F),
                    p(Material.BLACK_CONCRETE, 0.00, 0.13, 0.0, 2.84F, 2.04F, 1.38F),
                    p(Material.BLACK_CONCRETE, -0.80, 0.12, 0.0, 2.72F, 1.95F, 1.34F),
                    p(Material.BLACK_CONCRETE, -1.58, 0.10, 0.0, 2.48F, 1.75F, 1.28F),
                    p(Material.BLACK_CONCRETE, -2.28, 0.06, 0.0, 2.08F, 1.48F, 1.16F),
                    p(Material.BLACK_CONCRETE, -2.88, 0.03, 0.0, 1.66F, 1.15F, 1.05F),
                    p(Material.BLACK_CONCRETE, -3.38, 0.00, 0.0, 1.22F, 0.88F, 0.88F),
                    p(Material.WHITE_CONCRETE, 4.18, -0.53, 0.0, 1.08F, 0.17F, 0.92F),
                    p(Material.WHITE_CONCRETE, 3.42, -0.69, 0.0, 1.48F, 0.20F, 1.05F),
                    p(Material.WHITE_CONCRETE, 2.55, -0.82, 0.0, 1.82F, 0.21F, 1.20F),
                    p(Material.WHITE_CONCRETE, 1.55, -0.91, 0.0, 2.02F, 0.22F, 1.28F),
                    p(Material.WHITE_CONCRETE, 0.42, -0.94, 0.0, 2.08F, 0.22F, 1.33F),
                    p(Material.WHITE_CONCRETE, -0.72, -0.89, 0.0, 1.95F, 0.21F, 1.28F),
                    p(Material.WHITE_CONCRETE, -1.78, -0.74, 0.0, 1.65F, 0.19F, 1.16F),
                    r(Material.WHITE_CONCRETE, 3.28, 0.46, 1.07, 0.10F, 0.46F, 0.78F, RotationAxis.X, -8.0F),
                    r(Material.WHITE_CONCRETE, 2.92, 0.38, 1.13, 0.10F, 0.34F, 0.58F, RotationAxis.X, -14.0F),
                    r(Material.WHITE_CONCRETE, 3.28, 0.46, -1.07, 0.10F, 0.46F, 0.78F, RotationAxis.X, 8.0F),
                    r(Material.WHITE_CONCRETE, 2.92, 0.38, -1.13, 0.10F, 0.34F, 0.58F, RotationAxis.X, 14.0F),
                    r(Material.LIGHT_GRAY_CONCRETE, -0.82, 0.56, 1.30, 0.10F, 0.58F, 0.92F, RotationAxis.X, -8.0F),
                    r(Material.LIGHT_GRAY_CONCRETE, -1.30, 0.45, 1.24, 0.10F, 0.42F, 0.70F, RotationAxis.X, -12.0F),
                    r(Material.LIGHT_GRAY_CONCRETE, -0.82, 0.56, -1.30, 0.10F, 0.58F, 0.92F, RotationAxis.X, 8.0F),
                    r(Material.LIGHT_GRAY_CONCRETE, -1.30, 0.45, -1.24, 0.10F, 0.42F, 0.70F, RotationAxis.X, 12.0F),
                    r(Material.BLACK_CONCRETE, -0.18, 1.48, 0.0, 0.34F, 1.10F, 1.28F, RotationAxis.X, -7.0F),
                    r(Material.BLACK_CONCRETE, -0.33, 2.38, 0.0, 0.29F, 1.03F, 0.98F, RotationAxis.X, -10.0F),
                    r(Material.BLACK_CONCRETE, -0.47, 3.20, 0.0, 0.23F, 0.84F, 0.72F, RotationAxis.X, -13.0F),
                    r(Material.BLACK_CONCRETE, -0.59, 3.83, 0.0, 0.17F, 0.56F, 0.48F, RotationAxis.X, -15.0F),
                    r(Material.BLACK_CONCRETE, 1.18, -0.23, 1.72, 1.95F, 0.22F, 0.82F, RotationAxis.Y, 17.0F),
                    r(Material.BLACK_CONCRETE, 0.82, -0.34, 2.62, 1.28F, 0.16F, 0.58F, RotationAxis.Y, 24.0F),
                    r(Material.BLACK_CONCRETE, 1.18, -0.23, -1.72, 1.95F, 0.22F, 0.82F, RotationAxis.Y, -17.0F),
                    r(Material.BLACK_CONCRETE, 0.82, -0.34, -2.62, 1.28F, 0.16F, 0.58F, RotationAxis.Y, -24.0F),
                    p(Material.BLACK_CONCRETE, -3.88, 0.00, 0.0, 0.86F, 0.68F, 0.78F),
                    p(Material.BLACK_CONCRETE, -4.32, 0.00, 0.0, 0.54F, 0.48F, 0.62F),
                    a(Material.BLACK_CONCRETE, -4.72, 0.02, 1.05, 1.90F, 0.20F, 0.92F, RotationAxis.X, -4.0F, Animation.ORCA_FLUKE, 0.0F),
                    a(Material.BLACK_CONCRETE, -4.72, 0.02, -1.05, 1.90F, 0.20F, 0.92F, RotationAxis.X, 4.0F, Animation.ORCA_FLUKE, 0.0F),
                    a(Material.BLACK_CONCRETE, -4.82, 0.00, 2.10, 1.10F, 0.14F, 0.60F, RotationAxis.X, -7.0F, Animation.ORCA_FLUKE, 0.18F),
                    a(Material.BLACK_CONCRETE, -4.82, 0.00, -2.10, 1.10F, 0.14F, 0.60F, RotationAxis.X, 7.0F, Animation.ORCA_FLUKE, 0.18F),
                    r(Material.WHITE_CONCRETE, 4.45, -0.35, 0.0, 0.82F, 0.18F, 0.62F, RotationAxis.X, 4.0F),
                    r(Material.DEEPSLATE_TILES, 4.43, -0.18, 0.69, 0.055F, 0.07F, 0.72F, RotationAxis.Y, -3.0F),
                    r(Material.DEEPSLATE_TILES, 4.43, -0.18, -0.69, 0.055F, 0.07F, 0.72F, RotationAxis.Y, 3.0F),
                    p(Material.BLACK_CONCRETE, 3.78, 0.30, 1.03, 0.07F, 0.08F, 0.10F),
                    p(Material.BLACK_CONCRETE, 3.78, 0.30, -1.03, 0.07F, 0.08F, 0.10F),
                    p(Material.DEEPSLATE_TILES, 2.02, 1.10, 0.0, 0.22F, 0.055F, 0.30F)
            )
    ),

    CRAB(
            "Crab", MovementStyle.CRAB, 10.0, 0.0, 0.12, 3.2F, 1.6F, List.of(),
            List.of(
                    p(Material.ORANGE_TERRACOTTA, 0.0, 0.22, 0.0, 2.25F, 0.52F, 1.45F),
                    p(Material.RED_TERRACOTTA, 0.20, 0.48, 0.0, 1.85F, 0.18F, 1.18F),
                    p(Material.BROWN_TERRACOTTA, -0.28, 0.10, 0.0, 1.75F, 0.18F, 1.00F),
                    p(Material.BLACK_CONCRETE, 0.78, 0.67, 0.52, 0.16F, 0.16F, 0.16F),
                    p(Material.BLACK_CONCRETE, 0.78, 0.67, -0.52, 0.16F, 0.16F, 0.16F),
                    r(Material.RED_TERRACOTTA, 0.72, 0.15, 1.42, 0.86F, 0.28F, 0.72F, RotationAxis.Y, -14.0F),
                    r(Material.RED_TERRACOTTA, 0.72, 0.15, -1.42, 0.86F, 0.28F, 0.72F, RotationAxis.Y, 14.0F),
                    r(Material.ORANGE_TERRACOTTA, 1.12, 0.16, 1.88, 0.72F, 0.34F, 0.54F, RotationAxis.Y, 22.0F),
                    r(Material.ORANGE_TERRACOTTA, 1.12, 0.16, -1.88, 0.72F, 0.34F, 0.54F, RotationAxis.Y, -22.0F),
                    a(Material.RED_TERRACOTTA, 0.52, -0.05, 1.48, 1.18F, 0.16F, 0.22F, RotationAxis.Y, -30.0F, Animation.CRAB_LEG_A, 0.0F),
                    a(Material.RED_TERRACOTTA, 0.15, -0.06, 1.52, 1.25F, 0.16F, 0.22F, RotationAxis.Y, -12.0F, Animation.CRAB_LEG_B, 0.7F),
                    a(Material.RED_TERRACOTTA, -0.25, -0.06, 1.48, 1.22F, 0.16F, 0.22F, RotationAxis.Y, 10.0F, Animation.CRAB_LEG_A, 1.3F),
                    a(Material.RED_TERRACOTTA, -0.62, -0.05, 1.38, 1.10F, 0.16F, 0.22F, RotationAxis.Y, 28.0F, Animation.CRAB_LEG_B, 2.0F),
                    a(Material.RED_TERRACOTTA, 0.52, -0.05, -1.48, 1.18F, 0.16F, 0.22F, RotationAxis.Y, 30.0F, Animation.CRAB_LEG_B, 0.0F),
                    a(Material.RED_TERRACOTTA, 0.15, -0.06, -1.52, 1.25F, 0.16F, 0.22F, RotationAxis.Y, 12.0F, Animation.CRAB_LEG_A, 0.7F),
                    a(Material.RED_TERRACOTTA, -0.25, -0.06, -1.48, 1.22F, 0.16F, 0.22F, RotationAxis.Y, -10.0F, Animation.CRAB_LEG_B, 1.3F),
                    a(Material.RED_TERRACOTTA, -0.62, -0.05, -1.38, 1.10F, 0.16F, 0.22F, RotationAxis.Y, -28.0F, Animation.CRAB_LEG_A, 2.0F)
            )
    );

    private final String displayName;
    private final MovementStyle movementStyle;
    private final double maxHealth;
    private final double rideSpeed;
    private final double cruiseSpeed;
    private final float interactionWidth;
    private final float interactionHeight;
    private final List<SeatOffset> seats;
    private final List<ModelPart> parts;

    MarineMobType(String displayName, MovementStyle movementStyle, double maxHealth,
                  double rideSpeed, double cruiseSpeed, float interactionWidth, float interactionHeight,
                  List<SeatOffset> seats, List<ModelPart> parts) {
        this.displayName = displayName;
        this.movementStyle = movementStyle;
        this.maxHealth = maxHealth;
        this.rideSpeed = rideSpeed;
        this.cruiseSpeed = cruiseSpeed;
        this.interactionWidth = interactionWidth;
        this.interactionHeight = interactionHeight;
        this.seats = List.copyOf(seats);
        this.parts = List.copyOf(parts);
    }

    public String displayName() { return displayName; }
    public MovementStyle movementStyle() { return movementStyle; }
    public double maxHealth() { return maxHealth; }
    public double rideSpeed() { return rideSpeed; }
    public double cruiseSpeed() { return cruiseSpeed; }
    public float interactionWidth() { return interactionWidth; }
    public float interactionHeight() { return interactionHeight; }
    public List<SeatOffset> seats() { return seats; }
    public List<ModelPart> parts() { return parts; }
    public boolean rideable() { return !seats.isEmpty(); }

    public static MarineMobType fromInput(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "shark" -> SHARK;
            case "orca", "killer_whale", "killer-whale" -> ORCA;
            case "crab" -> CRAB;
            default -> null;
        };
    }

    private static ModelPart p(Material material, double forward, double up, double right,
                               float scaleX, float scaleY, float scaleZ) {
        return new ModelPart(material, forward, up, right, scaleX, scaleY, scaleZ,
                RotationAxis.NONE, 0.0F, Animation.STATIC, 0.0F);
    }

    private static ModelPart r(Material material, double forward, double up, double right,
                               float scaleX, float scaleY, float scaleZ,
                               RotationAxis axis, float baseDegrees) {
        return new ModelPart(material, forward, up, right, scaleX, scaleY, scaleZ,
                axis, baseDegrees, Animation.STATIC, 0.0F);
    }

    private static ModelPart a(Material material, double forward, double up, double right,
                               float scaleX, float scaleY, float scaleZ,
                               RotationAxis axis, float baseDegrees, Animation animation, float phase) {
        return new ModelPart(material, forward, up, right, scaleX, scaleY, scaleZ,
                axis, baseDegrees, animation, phase);
    }

    public enum MovementStyle { AQUATIC, CRAB }
    public enum RotationAxis { NONE, X, Y, Z }
    public enum Animation { STATIC, SHARK_TAIL, ORCA_FLUKE, CRAB_LEG_A, CRAB_LEG_B }

    public record SeatOffset(double forward, double up, double right) { }

    public record ModelPart(Material material, double forward, double up, double right,
                            float scaleX, float scaleY, float scaleZ,
                            RotationAxis rotationAxis, float baseDegrees,
                            Animation animation, float phase) { }
}
