package com.sagakenichi.freelifemarine;

import org.bukkit.Material;

import java.util.ArrayList;
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
            16.0,
            0.65F,
            0.055,
            List.of(new SeatOffset(0.0, 1.30, 0.0)),
            sharkParts()
    ),

    ORCA(
            "Orca",
            MovementStyle.AQUATIC,
            10.0,
            0.38,
            0.21,
            10.8F,
            5.0F,
            22.0,
            0.90F,
            0.075,
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
            orcaParts()
    ),

    CRAB(
            "Crab",
            MovementStyle.CRAB,
            10.0,
            0.0,
            0.075,
            1.35F,
            0.72F,
            8.0,
            1.20F,
            0.14,
            List.of(),
            crabParts()
    );

    private final String displayName;
    private final MovementStyle movementStyle;
    private final double maxHealth;
    private final double rideSpeed;
    private final double cruiseSpeed;
    private final float interactionWidth;
    private final float interactionHeight;
    private final double foodAttractionRange;
    private final float autonomousTurnRate;
    private final double autonomousAcceleration;
    private final List<SeatOffset> seats;
    private final List<ModelPart> parts;

    MarineMobType(String displayName, MovementStyle movementStyle, double maxHealth,
                  double rideSpeed, double cruiseSpeed, float interactionWidth, float interactionHeight,
                  double foodAttractionRange, float autonomousTurnRate, double autonomousAcceleration,
                  List<SeatOffset> seats, List<ModelPart> parts) {
        this.displayName = displayName;
        this.movementStyle = movementStyle;
        this.maxHealth = maxHealth;
        this.rideSpeed = rideSpeed;
        this.cruiseSpeed = cruiseSpeed;
        this.interactionWidth = interactionWidth;
        this.interactionHeight = interactionHeight;
        this.foodAttractionRange = foodAttractionRange;
        this.autonomousTurnRate = autonomousTurnRate;
        this.autonomousAcceleration = autonomousAcceleration;
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
    public double foodAttractionRange() { return foodAttractionRange; }
    public float autonomousTurnRate() { return autonomousTurnRate; }
    public double autonomousAcceleration() { return autonomousAcceleration; }
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

    private static List<ModelPart> sharkParts() {
        List<ModelPart> parts = new ArrayList<>();
        double[] forward = {3.62, 3.08, 2.52, 1.94, 1.34, 0.72, 0.08, -0.56, -1.18, -1.76, -2.28, -2.72, -3.08};
        float[] width = {0.78F, 1.08F, 1.36F, 1.62F, 1.82F, 1.94F, 1.98F, 1.88F, 1.68F, 1.44F, 1.16F, 0.90F, 0.66F};
        float[] height = {0.60F, 0.80F, 0.98F, 1.12F, 1.22F, 1.28F, 1.30F, 1.24F, 1.12F, 0.98F, 0.82F, 0.66F, 0.52F};
        for (int i = 0; i < forward.length; i++) {
            Material material = i < 2 ? Material.LIGHT_GRAY_CONCRETE : Material.GRAY_CONCRETE;
            parts.add(p(material, forward[i], 0.08, 0.0, width[i], height[i], 0.64F));
            if (i < 9) {
                parts.add(p(Material.WHITE_CONCRETE, forward[i] - 0.04, -height[i] * 0.43, 0.0,
                        width[i] * 0.78F, 0.14F, 0.68F));
            }
        }

        parts.add(p(Material.BLACK_CONCRETE, 3.18, 0.26, 0.49, 0.09F, 0.10F, 0.11F));
        parts.add(p(Material.BLACK_CONCRETE, 3.18, 0.26, -0.49, 0.09F, 0.10F, 0.11F));
        parts.add(r(Material.DEEPSLATE_TILES, 3.58, -0.20, 0.0, 0.76F, 0.055F, 0.18F, RotationAxis.X, 3.0F));

        double[] gills = {2.36, 2.18, 2.00, 1.82, 1.64};
        for (double gill : gills) {
            parts.add(p(Material.DEEPSLATE_TILES, gill, 0.00, 0.68, 0.045F, 0.42F, 0.07F));
            parts.add(p(Material.DEEPSLATE_TILES, gill, 0.00, -0.68, 0.045F, 0.42F, 0.07F));
        }

        parts.add(r(Material.GRAY_CONCRETE, 0.10, 0.90, 0.0, 0.30F, 1.20F, 0.72F, RotationAxis.X, -9.0F));
        parts.add(r(Material.GRAY_CONCRETE, -0.02, 1.56, 0.0, 0.22F, 0.70F, 0.52F, RotationAxis.X, -12.0F));
        parts.add(r(Material.GRAY_CONCRETE, -1.82, 0.55, 0.0, 0.18F, 0.54F, 0.40F, RotationAxis.X, -8.0F));
        parts.add(r(Material.GRAY_CONCRETE, -2.00, 0.83, 0.0, 0.13F, 0.34F, 0.29F, RotationAxis.X, -10.0F));

        parts.add(r(Material.GRAY_CONCRETE, 0.88, -0.16, 1.22, 1.62F, 0.14F, 0.62F, RotationAxis.Y, 18.0F));
        parts.add(r(Material.GRAY_CONCRETE, 0.55, -0.21, 1.88, 0.86F, 0.11F, 0.42F, RotationAxis.Y, 25.0F));
        parts.add(r(Material.GRAY_CONCRETE, 0.88, -0.16, -1.22, 1.62F, 0.14F, 0.62F, RotationAxis.Y, -18.0F));
        parts.add(r(Material.GRAY_CONCRETE, 0.55, -0.21, -1.88, 0.86F, 0.11F, 0.42F, RotationAxis.Y, -25.0F));
        parts.add(r(Material.GRAY_CONCRETE, -1.28, -0.28, 0.86, 0.72F, 0.10F, 0.34F, RotationAxis.Y, 12.0F));
        parts.add(r(Material.GRAY_CONCRETE, -1.28, -0.28, -0.86, 0.72F, 0.10F, 0.34F, RotationAxis.Y, -12.0F));

        parts.add(a(Material.GRAY_CONCRETE, -3.35, 0.02, 0.0, 0.52F, 0.44F, 0.54F,
                RotationAxis.Y, 0.0F, Animation.SHARK_BODY, 0.0F));
        parts.add(a(Material.GRAY_CONCRETE, -3.70, 0.02, 0.0, 0.36F, 0.34F, 0.42F,
                RotationAxis.Y, 0.0F, Animation.SHARK_BODY, 0.45F));
        parts.add(a(Material.GRAY_CONCRETE, -4.02, 0.54, 0.0, 0.26F, 1.46F, 0.60F,
                RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.0F));
        parts.add(a(Material.GRAY_CONCRETE, -4.00, -0.36, 0.0, 0.24F, 0.88F, 0.52F,
                RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.30F));
        parts.add(a(Material.LIGHT_GRAY_CONCRETE, -3.94, 1.10, 0.0, 0.18F, 0.48F, 0.44F,
                RotationAxis.Y, 0.0F, Animation.SHARK_TAIL, 0.18F));
        parts.add(p(Material.LIGHT_GRAY_CONCRETE, 3.84, -0.08, 0.0, 0.48F, 0.20F, 0.46F));
        parts.add(p(Material.GRAY_CONCRETE, -3.54, 0.00, 0.46, 0.52F, 0.08F, 0.12F));
        parts.add(p(Material.GRAY_CONCRETE, -3.54, 0.00, -0.46, 0.52F, 0.08F, 0.12F));
        return List.copyOf(parts);
    }

    private static List<ModelPart> orcaParts() {
        List<ModelPart> parts = new ArrayList<>();
        double[] forward = {4.70, 4.18, 3.64, 3.08, 2.50, 1.90, 1.28, 0.64, 0.00,
                -0.64, -1.28, -1.90, -2.48, -3.00, -3.46, -3.84, -4.14};
        float[] width = {0.96F, 1.40F, 1.82F, 2.16F, 2.44F, 2.66F, 2.82F, 2.92F, 2.96F,
                2.90F, 2.74F, 2.50F, 2.18F, 1.82F, 1.44F, 1.08F, 0.78F};
        float[] height = {0.72F, 1.02F, 1.30F, 1.54F, 1.74F, 1.90F, 2.02F, 2.10F, 2.12F,
                2.06F, 1.92F, 1.74F, 1.52F, 1.28F, 1.02F, 0.80F, 0.62F};
        for (int i = 0; i < forward.length; i++) {
            parts.add(p(Material.BLACK_CONCRETE, forward[i], 0.12, 0.0, width[i], height[i], 0.66F));
            if (i < 11) {
                parts.add(p(Material.WHITE_CONCRETE, forward[i] - 0.03, -height[i] * 0.43, 0.0,
                        width[i] * 0.70F, 0.16F, 0.70F));
            }
        }

        parts.add(r(Material.WHITE_CONCRETE, 3.36, 0.46, 1.02, 0.10F, 0.48F, 0.68F, RotationAxis.X, -8.0F));
        parts.add(r(Material.WHITE_CONCRETE, 3.02, 0.39, 1.11, 0.10F, 0.34F, 0.52F, RotationAxis.X, -14.0F));
        parts.add(r(Material.WHITE_CONCRETE, 3.36, 0.46, -1.02, 0.10F, 0.48F, 0.68F, RotationAxis.X, 8.0F));
        parts.add(r(Material.WHITE_CONCRETE, 3.02, 0.39, -1.11, 0.10F, 0.34F, 0.52F, RotationAxis.X, 14.0F));
        parts.add(p(Material.BLACK_CONCRETE, 3.77, 0.27, 0.85, 0.07F, 0.07F, 0.09F));
        parts.add(p(Material.BLACK_CONCRETE, 3.77, 0.27, -0.85, 0.07F, 0.07F, 0.09F));

        parts.add(r(Material.LIGHT_GRAY_CONCRETE, -0.76, 0.58, 1.33, 0.10F, 0.58F, 0.80F, RotationAxis.X, -8.0F));
        parts.add(r(Material.LIGHT_GRAY_CONCRETE, -1.20, 0.48, 1.29, 0.10F, 0.42F, 0.62F, RotationAxis.X, -12.0F));
        parts.add(r(Material.LIGHT_GRAY_CONCRETE, -0.76, 0.58, -1.33, 0.10F, 0.58F, 0.80F, RotationAxis.X, 8.0F));
        parts.add(r(Material.LIGHT_GRAY_CONCRETE, -1.20, 0.48, -1.29, 0.10F, 0.42F, 0.62F, RotationAxis.X, 12.0F));

        parts.add(r(Material.BLACK_CONCRETE, -0.12, 1.46, 0.0, 0.36F, 1.04F, 1.10F, RotationAxis.X, -6.0F));
        parts.add(r(Material.BLACK_CONCRETE, -0.27, 2.30, 0.0, 0.30F, 0.94F, 0.86F, RotationAxis.X, -9.0F));
        parts.add(r(Material.BLACK_CONCRETE, -0.40, 3.04, 0.0, 0.24F, 0.72F, 0.64F, RotationAxis.X, -12.0F));
        parts.add(r(Material.BLACK_CONCRETE, -0.51, 3.58, 0.0, 0.18F, 0.52F, 0.47F, RotationAxis.X, -14.0F));
        parts.add(r(Material.BLACK_CONCRETE, -0.59, 3.94, 0.0, 0.13F, 0.30F, 0.34F, RotationAxis.X, -16.0F));

        parts.add(r(Material.BLACK_CONCRETE, 1.16, -0.22, 1.76, 1.86F, 0.19F, 0.76F, RotationAxis.Y, 16.0F));
        parts.add(r(Material.BLACK_CONCRETE, 0.80, -0.34, 2.63, 1.22F, 0.14F, 0.54F, RotationAxis.Y, 23.0F));
        parts.add(r(Material.BLACK_CONCRETE, 1.16, -0.22, -1.76, 1.86F, 0.19F, 0.76F, RotationAxis.Y, -16.0F));
        parts.add(r(Material.BLACK_CONCRETE, 0.80, -0.34, -2.63, 1.22F, 0.14F, 0.54F, RotationAxis.Y, -23.0F));

        parts.add(a(Material.BLACK_CONCRETE, -4.42, 0.02, 0.0, 0.58F, 0.52F, 0.64F,
                RotationAxis.X, 0.0F, Animation.ORCA_PEDUNCLE, 0.0F));
        parts.add(a(Material.BLACK_CONCRETE, -4.78, 0.02, 0.0, 0.38F, 0.38F, 0.48F,
                RotationAxis.X, 0.0F, Animation.ORCA_PEDUNCLE, 0.35F));
        parts.add(a(Material.BLACK_CONCRETE, -5.03, 0.02, 0.95, 1.72F, 0.17F, 0.72F,
                RotationAxis.X, -4.0F, Animation.ORCA_FLUKE, 0.0F));
        parts.add(a(Material.BLACK_CONCRETE, -5.03, 0.02, -0.95, 1.72F, 0.17F, 0.72F,
                RotationAxis.X, 4.0F, Animation.ORCA_FLUKE, 0.0F));
        parts.add(a(Material.BLACK_CONCRETE, -5.08, 0.00, 1.92, 1.00F, 0.13F, 0.54F,
                RotationAxis.X, -7.0F, Animation.ORCA_FLUKE, 0.16F));
        parts.add(a(Material.BLACK_CONCRETE, -5.08, 0.00, -1.92, 1.00F, 0.13F, 0.54F,
                RotationAxis.X, 7.0F, Animation.ORCA_FLUKE, 0.16F));
        parts.add(a(Material.BLACK_CONCRETE, -5.04, -0.03, 2.56, 0.52F, 0.10F, 0.36F,
                RotationAxis.X, -9.0F, Animation.ORCA_FLUKE, 0.28F));
        parts.add(a(Material.BLACK_CONCRETE, -5.04, -0.03, -2.56, 0.52F, 0.10F, 0.36F,
                RotationAxis.X, 9.0F, Animation.ORCA_FLUKE, 0.28F));

        parts.add(r(Material.DEEPSLATE_TILES, 4.46, -0.17, 0.64, 0.055F, 0.055F, 0.62F, RotationAxis.Y, -3.0F));
        parts.add(r(Material.DEEPSLATE_TILES, 4.46, -0.17, -0.64, 0.055F, 0.055F, 0.62F, RotationAxis.Y, 3.0F));
        parts.add(p(Material.DEEPSLATE_TILES, 2.00, 1.10, 0.0, 0.18F, 0.05F, 0.24F));
        parts.add(p(Material.WHITE_CONCRETE, 4.48, -0.34, 0.0, 0.74F, 0.16F, 0.54F));
        parts.add(r(Material.WHITE_CONCRETE, 2.58, -0.20, 1.22, 0.08F, 0.44F, 0.40F, RotationAxis.X, -7.0F));
        parts.add(r(Material.WHITE_CONCRETE, 2.58, -0.20, -1.22, 0.08F, 0.44F, 0.40F, RotationAxis.X, 7.0F));
        parts.add(r(Material.WHITE_CONCRETE, 2.28, -0.28, 1.28, 0.08F, 0.30F, 0.34F, RotationAxis.X, -10.0F));
        parts.add(r(Material.WHITE_CONCRETE, 2.28, -0.28, -1.28, 0.08F, 0.30F, 0.34F, RotationAxis.X, 10.0F));
        return List.copyOf(parts);
    }

    private static List<ModelPart> crabParts() {
        List<ModelPart> parts = new ArrayList<>();
        parts.add(p(Material.ORANGE_TERRACOTTA, 0.0, 0.13, 0.0, 0.92F, 0.28F, 0.68F));
        parts.add(p(Material.RED_TERRACOTTA, 0.06, 0.28, 0.0, 0.78F, 0.10F, 0.56F));
        parts.add(p(Material.BROWN_TERRACOTTA, -0.10, 0.05, 0.0, 0.72F, 0.09F, 0.48F));
        parts.add(p(Material.BLACK_CONCRETE, 0.33, 0.37, 0.23, 0.07F, 0.07F, 0.07F));
        parts.add(p(Material.BLACK_CONCRETE, 0.33, 0.37, -0.23, 0.07F, 0.07F, 0.07F));
        parts.add(r(Material.RED_TERRACOTTA, 0.28, 0.09, 0.61, 0.38F, 0.12F, 0.30F, RotationAxis.Y, -14.0F));
        parts.add(r(Material.RED_TERRACOTTA, 0.28, 0.09, -0.61, 0.38F, 0.12F, 0.30F, RotationAxis.Y, 14.0F));
        parts.add(r(Material.ORANGE_TERRACOTTA, 0.46, 0.10, 0.80, 0.30F, 0.15F, 0.24F, RotationAxis.Y, 22.0F));
        parts.add(r(Material.ORANGE_TERRACOTTA, 0.46, 0.10, -0.80, 0.30F, 0.15F, 0.24F, RotationAxis.Y, -22.0F));

        double[] legForward = {0.23, 0.06, -0.12, -0.29};
        float[] phase = {0.0F, 0.7F, 1.3F, 2.0F};
        for (int i = 0; i < legForward.length; i++) {
            parts.add(a(Material.RED_TERRACOTTA, legForward[i], -0.03, 0.66 - i * 0.03,
                    0.52F - i * 0.03F, 0.07F, 0.10F, RotationAxis.Y,
                    -28.0F + i * 13.0F, i % 2 == 0 ? Animation.CRAB_LEG_A : Animation.CRAB_LEG_B, phase[i]));
            parts.add(a(Material.RED_TERRACOTTA, legForward[i], -0.03, -0.66 + i * 0.03,
                    0.52F - i * 0.03F, 0.07F, 0.10F, RotationAxis.Y,
                    28.0F - i * 13.0F, i % 2 == 0 ? Animation.CRAB_LEG_B : Animation.CRAB_LEG_A, phase[i]));
        }
        return List.copyOf(parts);
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
    public enum Animation {
        STATIC,
        SHARK_BODY,
        SHARK_TAIL,
        ORCA_PEDUNCLE,
        ORCA_FLUKE,
        CRAB_LEG_A,
        CRAB_LEG_B
    }

    public record SeatOffset(double forward, double up, double right) { }

    public record ModelPart(Material material, double forward, double up, double right,
                            float scaleX, float scaleY, float scaleZ,
                            RotationAxis rotationAxis, float baseDegrees,
                            Animation animation, float phase) { }
}
