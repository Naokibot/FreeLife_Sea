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
            6.8F,
            2.4F,
            List.of(new SeatOffset(0.20, 1.35, 0.0)),
            List.of(
                    p(Material.LIGHT_GRAY_CONCRETE, 2.85, 0.05, 0.0, 1.45F, 1.05F, 1.45F),
                    p(Material.GRAY_CONCRETE, 1.80, 0.05, 0.0, 1.65F, 1.20F, 1.80F),
                    p(Material.GRAY_CONCRETE, 0.45, 0.04, 0.0, 1.75F, 1.25F, 1.90F),
                    p(Material.GRAY_CONCRETE, -0.95, 0.03, 0.0, 1.55F, 1.10F, 1.55F),
                    p(Material.GRAY_CONCRETE, -2.05, 0.02, 0.0, 1.05F, 0.85F, 1.20F),
                    p(Material.WHITE_CONCRETE, 2.10, -0.53, 0.0, 1.15F, 0.18F, 1.60F),
                    p(Material.WHITE_CONCRETE, 0.55, -0.60, 0.0, 1.35F, 0.18F, 1.90F),
                    p(Material.WHITE_CONCRETE, -1.00, -0.50, 0.0, 1.10F, 0.16F, 1.45F),
                    p(Material.BLACK_CONCRETE, 2.95, 0.28, 0.66, 0.12F, 0.16F, 0.20F),
                    p(Material.BLACK_CONCRETE, 2.95, 0.28, -0.66, 0.12F, 0.16F, 0.20F),
                    p(Material.DEEPSLATE_TILES, 1.65, 0.02, 0.78, 0.08F, 0.58F, 0.10F),
                    p(Material.DEEPSLATE_TILES, 1.42, 0.02, 0.80, 0.08F, 0.56F, 0.10F),
                    p(Material.DEEPSLATE_TILES, 1.19, 0.02, 0.80, 0.08F, 0.54F, 0.10F),
                    p(Material.DEEPSLATE_TILES, 1.65, 0.02, -0.78, 0.08F, 0.58F, 0.10F),
                    p(Material.DEEPSLATE_TILES, 1.42, 0.02, -0.80, 0.08F, 0.56F, 0.10F),
                    p(Material.DEEPSLATE_TILES, 1.19, 0.02, -0.80, 0.08F, 0.54F, 0.10F),
                    p(Material.GRAY_CONCRETE, 0.15, 1.12, 0.0, 0.26F, 1.45F, 0.85F),
                    r(Material.GRAY_CONCRETE, 0.65, -0.08, 1.12, 1.65F, 0.18F, 0.72F, RotationAxis.Y, 16.0F),
                    r(Material.GRAY_CONCRETE, 0.65, -0.08, -1.12, 1.65F, 0.18F, 0.72F, RotationAxis.Y, -16.0F),
                    p(Material.GRAY_CONCRETE, -2.75, 0.02, 0.0, 0.65F, 0.65F, 1.15F),
                    a(Material.GRAY_CONCRETE, -3.45, 0.72, 0.0, 0.30F, 1.55F, 1.05F, RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.0F),
                    a(Material.GRAY_CONCRETE, -3.45, -0.55, 0.0, 0.28F, 1.05F, 0.85F, RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.35F),
                    p(Material.LIGHT_GRAY_CONCRETE, 3.38, -0.18, 0.0, 0.95F, 0.30F, 0.65F)
            )
    ),

    ORCA(
            "Orca",
            MovementStyle.AQUATIC,
            10.0,
            0.38,
            0.21,
            10.0F,
            4.8F,
            List.of(
                    new SeatOffset(2.35, 1.70, 0.72),
                    new SeatOffset(2.35, 1.70, -0.72),
                    new SeatOffset(0.85, 1.78, 0.82),
                    new SeatOffset(0.85, 1.78, -0.82),
                    new SeatOffset(-0.70, 1.72, 0.78),
                    new SeatOffset(-0.70, 1.72, -0.78),
                    new SeatOffset(-2.05, 1.52, 0.62),
                    new SeatOffset(-2.05, 1.52, -0.62)
            ),
            List.of(
                    p(Material.BLACK_CONCRETE, 3.95, 0.08, 0.0, 2.35F, 1.60F, 1.70F),
                    p(Material.BLACK_CONCRETE, 2.75, 0.08, 0.0, 2.75F, 1.92F, 1.95F),
                    p(Material.BLACK_CONCRETE, 1.20, 0.08, 0.0, 3.05F, 2.10F, 2.30F),
                    p(Material.BLACK_CONCRETE, -0.55, 0.07, 0.0, 3.10F, 2.12F, 2.25F),
                    p(Material.BLACK_CONCRETE, -2.15, 0.05, 0.0, 2.45F, 1.72F, 1.90F),
                    p(Material.BLACK_CONCRETE, -3.35, 0.02, 0.0, 1.45F, 1.18F, 1.45F),
                    p(Material.WHITE_CONCRETE, 3.35, -0.73, 0.0, 1.65F, 0.26F, 1.45F),
                    p(Material.WHITE_CONCRETE, 1.80, -0.92, 0.0, 2.15F, 0.28F, 2.15F),
                    p(Material.WHITE_CONCRETE, -0.25, -0.94, 0.0, 2.20F, 0.26F, 2.10F),
                    p(Material.WHITE_CONCRETE, -2.05, -0.72, 0.0, 1.55F, 0.22F, 1.55F),
                    p(Material.WHITE_CONCRETE, 3.18, 0.44, 1.13, 0.14F, 0.70F, 0.92F),
                    p(Material.WHITE_CONCRETE, 3.18, 0.44, -1.13, 0.14F, 0.70F, 0.92F),
                    p(Material.LIGHT_GRAY_CONCRETE, -0.85, 0.47, 1.47, 0.13F, 0.76F, 1.16F),
                    p(Material.LIGHT_GRAY_CONCRETE, -0.85, 0.47, -1.47, 0.13F, 0.76F, 1.16F),
                    p(Material.BLACK_CONCRETE, -0.55, 1.72, 0.0, 0.62F, 1.72F, 1.05F),
                    p(Material.BLACK_CONCRETE, -0.58, 3.02, 0.0, 0.48F, 1.42F, 0.78F),
                    p(Material.BLACK_CONCRETE, -0.64, 4.02, 0.0, 0.34F, 0.72F, 0.54F),
                    r(Material.BLACK_CONCRETE, 1.18, -0.25, 2.03, 2.35F, 0.24F, 0.82F, RotationAxis.Y, 13.0F),
                    r(Material.BLACK_CONCRETE, 1.18, -0.25, -2.03, 2.35F, 0.24F, 0.82F, RotationAxis.Y, -13.0F),
                    p(Material.BLACK_CONCRETE, -3.95, 0.00, 0.0, 0.92F, 0.82F, 1.30F),
                    a(Material.BLACK_CONCRETE, -4.65, 0.03, 1.42, 2.70F, 0.24F, 1.10F, RotationAxis.X, -5.0F, Animation.ORCA_FLUKE, 0.0F),
                    a(Material.BLACK_CONCRETE, -4.65, 0.03, -1.42, 2.70F, 0.24F, 1.10F, RotationAxis.X, 5.0F, Animation.ORCA_FLUKE, 0.0F),
                    p(Material.GRAY_CONCRETE, 2.10, 1.10, 0.0, 0.38F, 0.08F, 0.48F),
                    p(Material.GRAY_CONCRETE, 4.15, -0.02, 1.18, 0.10F, 0.10F, 0.95F),
                    p(Material.GRAY_CONCRETE, 4.15, -0.02, -1.18, 0.10F, 0.10F, 0.95F),
                    p(Material.BLACK_CONCRETE, 4.62, -0.18, 0.0, 1.15F, 0.38F, 0.72F),
                    p(Material.WHITE_CONCRETE, 3.72, -0.35, 0.93, 0.12F, 0.32F, 0.72F),
                    p(Material.WHITE_CONCRETE, 3.72, -0.35, -0.93, 0.12F, 0.32F, 0.72F)
            )
    ),

    CRAB(
            "Crab",
            MovementStyle.CRAB,
            10.0,
            0.0,
            0.12,
            3.2F,
            1.6F,
            List.of(),
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

    MarineMobType(
            String displayName,
            MovementStyle movementStyle,
            double maxHealth,
            double rideSpeed,
            double cruiseSpeed,
            float interactionWidth,
            float interactionHeight,
            List<SeatOffset> seats,
            List<ModelPart> parts
    ) {
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
        if (value == null) {
            return null;
        }
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

    public record SeatOffset(double forward, double up, double right) {}

    public record ModelPart(
            Material material,
            double forward,
            double up,
            double right,
            float scaleX,
            float scaleY,
            float scaleZ,
            RotationAxis rotationAxis,
            float baseDegrees,
            Animation animation,
            float phase
    ) {}
}
