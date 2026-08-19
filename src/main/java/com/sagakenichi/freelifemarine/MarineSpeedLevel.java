package com.sagakenichi.freelifemarine;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ten discrete movement speeds used by autonomous and show-controlled marine mobs.
 * Values are expressed in blocks per second and converted to Bukkit velocity units
 * (blocks per tick) at the boundary where velocity is applied.
 */
public enum MarineSpeedLevel {
    LEVEL_1(1, 2.0),
    LEVEL_2(2, 4.0),
    LEVEL_3(3, 6.0),
    LEVEL_4(4, 8.0),
    LEVEL_5(5, 10.0),
    LEVEL_6(6, 12.0),
    LEVEL_7(7, 14.0),
    LEVEL_8(8, 16.0),
    LEVEL_9(9, 18.0),
    LEVEL_10(10, 20.0);

    private final int level;
    private final double blocksPerSecond;

    MarineSpeedLevel(int level, double blocksPerSecond) {
        this.level = level;
        this.blocksPerSecond = blocksPerSecond;
    }

    public int level() {
        return level;
    }

    public double blocksPerSecond() {
        return blocksPerSecond;
    }

    public double blocksPerTick() {
        return blocksPerSecond / 20.0;
    }

    public static MarineSpeedLevel of(int level) {
        if (level < 1 || level > 10) {
            throw new IllegalArgumentException("Marine speed level must be between 1 and 10");
        }
        return values()[level - 1];
    }

    public static MarineSpeedLevel randomBetween(int minLevel, int maxLevel) {
        if (minLevel < 1 || maxLevel > 10 || minLevel > maxLevel) {
            throw new IllegalArgumentException("Invalid marine speed range");
        }
        return of(ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1));
    }

    public static MarineSpeedLevel nearestBlocksPerTick(double blocksPerTick) {
        MarineSpeedLevel nearest = LEVEL_1;
        double best = Double.MAX_VALUE;
        for (MarineSpeedLevel candidate : values()) {
            double delta = Math.abs(candidate.blocksPerTick() - blocksPerTick);
            if (delta < best) {
                best = delta;
                nearest = candidate;
            }
        }
        return nearest;
    }
}
