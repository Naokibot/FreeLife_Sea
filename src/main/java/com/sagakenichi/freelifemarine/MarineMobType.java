package com.sagakenichi.freelifemarine;

import org.bukkit.Material;

import java.util.List;
import java.util.Locale;

public enum MarineMobType {

    SHARK(
            "Shark",
            0.34,
            3.6F,
            1.8F,
            List.of(
                    new ModelPart(Material.GRAY_CONCRETE, 0.0, 0.60, 0.0, 1.10F, 0.80F, 3.20F),
                    new ModelPart(Material.LIGHT_GRAY_CONCRETE, 1.65, 0.55, 0.0, 0.75F, 0.65F, 0.80F),
                    new ModelPart(Material.GRAY_CONCRETE, -1.75, 0.58, 0.0, 0.55F, 0.55F, 1.05F),
                    new ModelPart(Material.GRAY_CONCRETE, -2.35, 0.58, 0.0, 0.22F, 1.15F, 0.55F),
                    new ModelPart(Material.GRAY_CONCRETE, 0.15, 1.25, 0.0, 0.22F, 0.90F, 0.75F),
                    new ModelPart(Material.GRAY_CONCRETE, 0.10, 0.40, 0.78, 0.95F, 0.18F, 0.75F),
                    new ModelPart(Material.GRAY_CONCRETE, 0.10, 0.40, -0.78, 0.95F, 0.18F, 0.75F),
                    new ModelPart(Material.WHITE_CONCRETE, 0.35, 0.28, 0.0, 0.78F, 0.14F, 1.65F)
            )
    ),
    ORCA(
            "Orca",
            0.30,
            4.4F,
            2.2F,
            List.of(
                    new ModelPart(Material.BLACK_CONCRETE, 0.0, 0.72, 0.0, 1.35F, 1.00F, 3.80F),
                    new ModelPart(Material.BLACK_CONCRETE, 2.00, 0.70, 0.0, 0.90F, 0.82F, 0.95F),
                    new ModelPart(Material.BLACK_CONCRETE, -2.05, 0.70, 0.0, 0.58F, 0.62F, 1.15F),
                    new ModelPart(Material.BLACK_CONCRETE, -2.70, 0.70, 0.0, 0.25F, 1.30F, 0.60F),
                    new ModelPart(Material.BLACK_CONCRETE, 0.10, 1.55, 0.0, 0.28F, 1.35F, 0.80F),
                    new ModelPart(Material.BLACK_CONCRETE, 0.15, 0.45, 0.95, 1.20F, 0.20F, 0.85F),
                    new ModelPart(Material.BLACK_CONCRETE, 0.15, 0.45, -0.95, 1.20F, 0.20F, 0.85F),
                    new ModelPart(Material.WHITE_CONCRETE, 0.65, 0.32, 0.0, 0.90F, 0.18F, 1.75F),
                    new ModelPart(Material.WHITE_CONCRETE, 1.20, 0.88, 0.60, 0.28F, 0.22F, 0.55F),
                    new ModelPart(Material.WHITE_CONCRETE, 1.20, 0.88, -0.60, 0.28F, 0.22F, 0.55F)
            )
    );

    private final String displayName;
    private final double rideSpeed;
    private final float interactionWidth;
    private final float interactionHeight;
    private final List<ModelPart> parts;

    MarineMobType(
            String displayName,
            double rideSpeed,
            float interactionWidth,
            float interactionHeight,
            List<ModelPart> parts
    ) {
        this.displayName = displayName;
        this.rideSpeed = rideSpeed;
        this.interactionWidth = interactionWidth;
        this.interactionHeight = interactionHeight;
        this.parts = parts;
    }

    public String displayName() {
        return displayName;
    }

    public double rideSpeed() {
        return rideSpeed;
    }

    public float interactionWidth() {
        return interactionWidth;
    }

    public float interactionHeight() {
        return interactionHeight;
    }

    public List<ModelPart> parts() {
        return parts;
    }

    public static MarineMobType fromInput(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "shark" -> SHARK;
            case "orca", "killer_whale", "killer-whale" -> ORCA;
            default -> null;
        };
    }

    public record ModelPart(
            Material material,
            double forward,
            double up,
            double right,
            float scaleX,
            float scaleY,
            float scaleZ
    ) {
    }
}
