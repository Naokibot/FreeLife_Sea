package com.sagakenichi.freelifemarine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class OrcaShowManager {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final int SHOW_END_TICK = 760;
    private static final float[] MELODY = {
            1.000F, 1.122F, 1.260F, 1.335F,
            1.498F, 1.335F, 1.260F, 1.122F,
            1.000F, 1.122F, 1.260F, 1.498F,
            1.682F, 1.498F, 1.260F, 1.122F
    };

    private final JavaPlugin plugin;
    private final MarineMobService mobs;
    private final Map<String, ShowDefinition> definitions = new LinkedHashMap<>();
    private final Set<String> firedOccurrences = new HashSet<>();
    private BukkitTask task;
    private ActiveShow active;
    private long serverTicks;

    public OrcaShowManager(JavaPlugin plugin, MarineMobService mobs) {
        this.plugin = plugin;
        this.mobs = mobs;
    }

    public void start() {
        reload();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        stopShow("Plugin disabled.");
    }

    public void reload() {
        plugin.reloadConfig();
        definitions.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("shows");
        if (root == null) {
            plugin.getLogger().warning("No 'shows' section found in config.yml.");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ShowDefinition definition = readDefinition(id, section);
            definitions.put(id.toLowerCase(Locale.ROOT), definition);
        }
        plugin.getLogger().info("Loaded " + definitions.size() + " orca show definition(s).");
    }

    public List<String> showIds() {
        return List.copyOf(definitions.keySet());
    }

    public String setCenter(Player player, String requestedId) {
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        Location location = player.getLocation();
        String path = path(definition.id);
        plugin.getConfig().set(path + ".world", location.getWorld().getName());
        plugin.getConfig().set(path + ".center.x", roundCoordinate(location.getX()));
        plugin.getConfig().set(path + ".center.y", roundCoordinate(location.getY()));
        plugin.getConfig().set(path + ".center.z", roundCoordinate(location.getZ()));
        plugin.saveConfig();
        reload();
        return "Show '" + definition.id + "' center set to " + location.getWorld().getName() + " "
                + trim(roundCoordinate(location.getX())) + ", " + trim(roundCoordinate(location.getY())) + ", "
                + trim(roundCoordinate(location.getZ())) + ".";
    }

    public String setFacing(Player player, String requestedId) {
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        float yaw = normalizeYaw(player.getLocation().getYaw());
        plugin.getConfig().set(path(definition.id) + ".heading-yaw", yaw);
        plugin.saveConfig();
        reload();
        return "Show '" + definition.id + "' heading set to yaw " + String.format(Locale.ROOT, "%.1f", yaw) + ".";
    }

    public String setSingleTime(String requestedId, String rawTime) {
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        LocalTime time = ShowSchedule.parseTime(rawTime);
        if (time == null) {
            return "Invalid time. Use HH:mm, for example 15:30.";
        }
        plugin.getConfig().set(path(definition.id) + ".times", List.of(CLOCK.format(time)));
        plugin.saveConfig();
        reload();
        return "Show '" + definition.id + "' time set to " + CLOCK.format(time) + ".";
    }

    public String addTime(String requestedId, String rawTime) {
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        LocalTime time = ShowSchedule.parseTime(rawTime);
        if (time == null) {
            return "Invalid time. Use HH:mm, for example 15:30.";
        }
        String configPath = path(definition.id) + ".times";
        List<String> values = new ArrayList<>(plugin.getConfig().getStringList(configPath));
        values.add(CLOCK.format(time));
        plugin.getConfig().set(configPath, ShowSchedule.parseTimes(values).stream().map(CLOCK::format).toList());
        plugin.saveConfig();
        reload();
        return "Added " + CLOCK.format(time) + " to show '" + definition.id + "'.";
    }

    public String removeTime(String requestedId, String rawTime) {
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        LocalTime time = ShowSchedule.parseTime(rawTime);
        if (time == null) {
            return "Invalid time. Use HH:mm, for example 15:30.";
        }
        String configPath = path(definition.id) + ".times";
        List<LocalTime> times = new ArrayList<>(ShowSchedule.parseTimes(plugin.getConfig().getStringList(configPath)));
        boolean removed = times.remove(time);
        plugin.getConfig().set(configPath, times.stream().map(CLOCK::format).toList());
        plugin.saveConfig();
        reload();
        return removed
                ? "Removed " + CLOCK.format(time) + " from show '" + definition.id + "'."
                : "Show '" + definition.id + "' did not contain " + CLOCK.format(time) + ".";
    }

    public String setEnabled(String requestedId, boolean enabled) {
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        plugin.getConfig().set(path(definition.id) + ".enabled", enabled);
        plugin.saveConfig();
        reload();
        return "Show '" + definition.id + "' scheduled execution is now " + (enabled ? "enabled" : "disabled") + ".";
    }

    public String startShow(String requestedId) {
        if (active != null) {
            return "A show is already running: " + active.definition.id + ".";
        }
        ShowDefinition definition = findDefinition(requestedId);
        if (definition == null) {
            return unknownShow();
        }
        World world = Bukkit.getWorld(definition.worldName);
        if (world == null) {
            return "Show world '" + definition.worldName + "' is not loaded.";
        }

        Location center = definition.center(world);
        List<MarineMobService.MarineMob> selected = mobs.nearbyOrcas(
                world, center, definition.controlRadius, definition.orcaCount);
        if (selected.isEmpty()) {
            return "No spawned orcas are within " + trim(definition.controlRadius)
                    + " blocks of the show center. The show was not started.";
        }

        for (MarineMobService.MarineMob mob : selected) {
            mobs.beginShowControl(mob);
        }
        active = new ActiveShow(definition, selected);
        announce(center, definition.audienceRadius,
                "§b§lOrca Show §f- §e" + definition.id + " §fhas started!", definition.musicVolume);
        return "Started show '" + definition.id + "' with " + selected.size() + " orca(s).";
    }

    public String stopShow() {
        return stopShow("Show stopped by an administrator.");
    }

    public String status() {
        if (active == null) {
            return "No orca show is currently running.";
        }
        return "Show '" + active.definition.id + "' is running at tick " + active.tick
                + " with " + active.orcas.size() + " orca(s).";
    }

    private String stopShow(String reason) {
        ActiveShow current = active;
        if (current == null) {
            return "No orca show is currently running.";
        }
        active = null;
        for (MarineMobService.MarineMob mob : current.orcas) {
            mobs.endShowControl(mob);
        }
        World world = Bukkit.getWorld(current.definition.worldName);
        if (world != null) {
            announce(current.definition.center(world), current.definition.audienceRadius,
                    "§bOrca Show §f- " + reason, current.definition.musicVolume);
        }
        return reason;
    }

    private void tick() {
        serverTicks++;
        if (serverTicks % 20L == 0L) {
            checkSchedules();
        }
        if (active != null) {
            tickActiveShow(active);
        }
    }

    private void checkSchedules() {
        if (active != null) {
            return;
        }
        for (ShowDefinition definition : definitions.values()) {
            if (!definition.enabled || definition.times.isEmpty()) {
                continue;
            }
            ZonedDateTime now = ZonedDateTime.now(definition.zone);
            LocalDate date = now.toLocalDate();
            LocalTime minute = now.toLocalTime().withSecond(0).withNano(0);
            for (LocalTime scheduled : definition.times) {
                if (!minute.equals(scheduled)) {
                    continue;
                }
                String occurrence = definition.id + '|' + date + '|' + scheduled;
                if (firedOccurrences.add(occurrence)) {
                    String result = startShow(definition.id);
                    plugin.getLogger().info("Scheduled orca show: " + result);
                    pruneOccurrences(date.minusDays(2));
                }
                return;
            }
        }
    }

    private void pruneOccurrences(LocalDate oldestDate) {
        firedOccurrences.removeIf(value -> {
            int first = value.indexOf('|');
            int second = value.indexOf('|', first + 1);
            if (first < 0 || second < 0) {
                return true;
            }
            try {
                return LocalDate.parse(value.substring(first + 1, second)).isBefore(oldestDate);
            } catch (RuntimeException ignored) {
                return true;
            }
        });
    }

    private void tickActiveShow(ActiveShow show) {
        if (active != show) {
            return;
        }
        World world = Bukkit.getWorld(show.definition.worldName);
        if (world == null) {
            stopShow("Show world unloaded; show cancelled.");
            return;
        }

        show.orcas.removeIf(mob -> !mobs.isUsable(mob));
        if (show.orcas.isEmpty()) {
            stopShow("All show orcas became unavailable; show cancelled.");
            return;
        }

        Location center = show.definition.center(world);
        int tick = show.tick++;
        if (show.definition.musicEnabled) {
            playMusic(show, center);
        }

        if (tick < 160) {
            guideFormation(show, center, -8.0, -0.55, 0.34);
        } else if (tick < 220) {
            hold(show);
        } else if (tick < 360) {
            guideFormation(show, center, 11.0, -0.20, 0.48);
        } else if (tick < 440) {
            guideFormation(show, center, -7.0, -1.10, 0.42);
        } else if (tick < 560) {
            runJumpWave(show, center, tick);
        } else if (tick < 620) {
            guideFormation(show, center, 2.0, -0.10, 0.28);
            if (!show.blowDone && tick >= 590) {
                show.blowDone = true;
                for (MarineMobService.MarineMob mob : show.orcas) {
                    mobs.emitShowBlow(mob);
                }
                announce(center, show.definition.audienceRadius,
                        "§f§lSplash & blow!", show.definition.musicVolume);
            }
        } else if (tick < SHOW_END_TICK) {
            guideFormation(show, center, -10.0, -0.45, 0.38);
        } else {
            stopShow("Show complete. Normal autonomous swimming resumed.");
        }
    }

    private void guideFormation(ActiveShow show, Location center, double forward, double up, double speed) {
        for (int index = 0; index < show.orcas.size(); index++) {
            double right = formationOffset(index, show.orcas.size(), 2.8);
            Location target = relative(center, show.definition.headingYaw, forward, up, right);
            mobs.guideShow(show.orcas.get(index), target, speed);
        }
    }

    private void hold(ActiveShow show) {
        for (MarineMobService.MarineMob mob : show.orcas) {
            mobs.holdShow(mob);
        }
    }

    private void runJumpWave(ActiveShow show, Location center, int tick) {
        for (int index = 0; index < show.orcas.size(); index++) {
            MarineMobService.MarineMob mob = show.orcas.get(index);
            int launchTick = 440 + index * 16;
            double right = formationOffset(index, show.orcas.size(), 2.8);
            Location prep = relative(center, show.definition.headingYaw, -7.0, -1.10, right);
            Location landing = relative(center, show.definition.headingYaw, 7.5, -0.10, right);

            if (tick < launchTick) {
                mobs.guideShow(mob, prep, 0.24);
            } else if (tick == launchTick) {
                mobs.launchShowJump(mob, landing, 0.54, 0.72);
                announce(center, show.definition.audienceRadius,
                        "§bJump!", show.definition.musicVolume);
            } else if (tick > launchTick + 52) {
                mobs.guideShow(mob, landing, 0.34);
            }
        }
    }

    private void playMusic(ActiveShow show, Location center) {
        if (show.tick % 8 != 0) {
            return;
        }
        int beat = show.musicBeat++;
        float melodyPitch = MELODY[beat % MELODY.length];
        float volume = show.definition.musicVolume;
        double radiusSquared = show.definition.audienceRadius * show.definition.audienceRadius;

        World world = center.getWorld();
        if (world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(center) > radiusSquared) {
                continue;
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP,
                    SoundCategory.MUSIC, volume, melodyPitch);
            if (beat % 4 == 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS,
                        SoundCategory.MUSIC, volume * 0.72F, 0.80F);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM,
                        SoundCategory.MUSIC, volume * 0.62F, 1.00F);
            }
            if (beat % 8 == 6) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,
                        SoundCategory.MUSIC, volume * 0.46F, melodyPitch * 1.12F);
            }
        }
    }

    private void announce(Location center, double radius, String message, float volume) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        double radiusSquared = radius * radius;
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(center) <= radiusSquared) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.MASTER, Math.min(1.0F, Math.max(0.2F, volume)), 1.25F);
            }
        }
    }

    private ShowDefinition findDefinition(String requestedId) {
        if (requestedId == null || requestedId.isBlank()) {
            return definitions.values().stream().findFirst().orElse(null);
        }
        return definitions.get(requestedId.toLowerCase(Locale.ROOT));
    }

    private String unknownShow() {
        return definitions.isEmpty()
                ? "No shows are configured."
                : "Unknown show. Available: " + String.join(", ", definitions.keySet());
    }

    private ShowDefinition readDefinition(String id, ConfigurationSection section) {
        String configuredZone = section.getString("time-zone");
        ZoneId zone = ShowSchedule.parseZone(configuredZone, DEFAULT_ZONE);
        if (configuredZone != null && !configuredZone.isBlank()
                && !zone.getId().equals(configuredZone.trim())) {
            plugin.getLogger().warning("Show '" + id + "' has invalid time-zone '"
                    + configuredZone + "'; falling back to " + DEFAULT_ZONE + ".");
        }

        List<String> rawTimes = section.getStringList("times");
        List<LocalTime> times = ShowSchedule.parseTimes(rawTimes);
        if (times.size() != rawTimes.size()) {
            plugin.getLogger().warning("Show '" + id
                    + "' contains invalid or duplicate times; valid entries were kept.");
        }

        ConfigurationSection center = section.getConfigurationSection("center");
        double x = center == null ? 0.5 : center.getDouble("x", 0.5);
        double y = center == null ? 63.0 : center.getDouble("y", 63.0);
        double z = center == null ? 0.5 : center.getDouble("z", 0.5);

        ConfigurationSection music = section.getConfigurationSection("music");
        boolean musicEnabled = music == null || music.getBoolean("enabled", true);
        float musicVolume = (float) clamp(
                music == null ? 0.75 : music.getDouble("volume", 0.75), 0.0, 2.0);

        return new ShowDefinition(
                id,
                section.getBoolean("enabled", false),
                zone,
                times,
                section.getString("world", "world"),
                x,
                y,
                z,
                (float) section.getDouble("heading-yaw", 0.0),
                clampInt(section.getInt("orcas", 4), 1, 8),
                clamp(section.getDouble("control-radius", 48.0), 4.0, 256.0),
                clamp(section.getDouble("audience-radius", 64.0), 4.0, 256.0),
                musicEnabled,
                musicVolume
        );
    }

    private static String path(String id) {
        return "shows." + id;
    }

    private static double roundCoordinate(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static float normalizeYaw(float yaw) {
        float normalized = yaw % 360.0F;
        if (normalized > 180.0F) normalized -= 360.0F;
        if (normalized <= -180.0F) normalized += 360.0F;
        return normalized;
    }

    private static double formationOffset(int index, int count, double spacing) {
        return (index - (count - 1) / 2.0) * spacing;
    }

    private static Location relative(Location center, float yaw,
                                     double forward, double up, double right) {
        double radians = Math.toRadians(yaw);
        Vector forwardVector = new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
        Vector rightVector = new Vector(forwardVector.getZ(), 0.0, -forwardVector.getX());
        return center.clone()
                .add(forwardVector.multiply(forward))
                .add(rightVector.multiply(right))
                .add(0.0, up, 0.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trim(double value) {
        return value == Math.rint(value) ? Integer.toString((int) value) : Double.toString(value);
    }

    private record ShowDefinition(
            String id,
            boolean enabled,
            ZoneId zone,
            List<LocalTime> times,
            String worldName,
            double x,
            double y,
            double z,
            float headingYaw,
            int orcaCount,
            double controlRadius,
            double audienceRadius,
            boolean musicEnabled,
            float musicVolume
    ) {
        Location center(World world) {
            return new Location(world, x, y, z, headingYaw, 0.0F);
        }
    }

    private static final class ActiveShow {
        private final ShowDefinition definition;
        private final List<MarineMobService.MarineMob> orcas;
        private int tick;
        private int musicBeat;
        private boolean blowDone;

        private ActiveShow(ShowDefinition definition, List<MarineMobService.MarineMob> orcas) {
            this.definition = definition;
            this.orcas = new ArrayList<>(orcas);
        }
    }
}
