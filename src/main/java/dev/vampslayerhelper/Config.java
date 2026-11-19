package dev.vampslayerhelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class Config {
    // ---- Sicherheit ----
    public boolean enabled = false;
    /** Nur auf diesen Server-Adressen laeuft die Automatik. Leer = nirgends. */
    public List<String> allowedServers = new ArrayList<>();
    public boolean allowSingleplayer = false;

    // ---- Twinclaws ----
    /** Namensteil des Boss-Spielers (wie bei SkyHanni "Bloodfiend"). */
    public String bossNameContains = "Bloodfiend";
    /** Maximale Entfernung zwischen dir und dem Boss, damit Twinclaws beachtet werden. */
    public float maxBossDistance = 20f;
    public int twinclawCooldownTicks = 100;      // wie SkyHanni: hoechstens alle 5 Sekunden
    public int holyIceSlot = 4;              // Hotbar-Slot 1-9
    public int twinclawDelayMinMs = 150;
    public int twinclawDelayMaxMs = 350;

    // ---- Auto-Heilung ----
    public boolean autoHeal = true;
    public int healingMelonSlot = 2;         // Hotbar-Slot 1-9
    public int healThresholdPercent = 50;    // ab so viel Prozent HP oder weniger wird geheilt
    public int healCooldownTicks = 60;       // Mindestabstand zwischen zwei Heilungen
    public boolean healOnlyNearBoss = true;  // nur heilen, wenn ein Boss in der Naehe ist
    public int healDelayMinMs = 150;
    public int healDelayMaxMs = 400;

    // ---- Impel ----
    public int impelReactMinMs = 100;
    public int impelReactMaxMs = 350;
    public float lookUpPitch = -40f;         // negativ = nach oben
    public float lookDownPitch = 50f;        // positiv = nach unten
    public float yawJitter = 3f;             // zufaellige Seitenabweichung in Grad
    public int moveMinTicks = 4;             // Dauer der Kamerabewegung (20 Ticks = 1 s)
    public int moveMaxTicks = 7;
    public int sneakHoldTicks = 12;

    // ---------------------------------------------------------------

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance = new Config();

    public static Config get() { return instance; }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vampslayerhelper.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                Config c = GSON.fromJson(r, Config.class);
                if (c != null) instance = c;
            } catch (Exception e) {
                System.err.println("[VampSlayerHelper] Config konnte nicht gelesen werden: " + e);
            }
        }
        instance.sanitize();
        save();
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            System.err.println("[VampSlayerHelper] Config konnte nicht gespeichert werden: " + e);
        }
    }

    private void sanitize() {
        if (allowedServers == null) allowedServers = new ArrayList<>();
        if (bossNameContains == null) bossNameContains = "Bloodfiend";
        holyIceSlot = Math.max(1, Math.min(9, holyIceSlot));
        healingMelonSlot = Math.max(1, Math.min(9, healingMelonSlot));
        healThresholdPercent = Math.max(1, Math.min(99, healThresholdPercent));
        if (healDelayMaxMs < healDelayMinMs) healDelayMaxMs = healDelayMinMs;
        if (twinclawDelayMaxMs < twinclawDelayMinMs) twinclawDelayMaxMs = twinclawDelayMinMs;
        if (impelReactMaxMs < impelReactMinMs) impelReactMaxMs = impelReactMinMs;
        moveMinTicks = Math.max(2, moveMinTicks);
        if (moveMaxTicks < moveMinTicks) moveMaxTicks = moveMinTicks;
        sneakHoldTicks = Math.max(2, sneakHoldTicks);
    }

    /**
     * Multiplayer: auf jedem verbundenen Server erlaubt, einschliesslich Hypixel.
     * Singleplayer bleibt separat ueber allowSingleplayer steuerbar.
     */
    public static boolean isServerAllowed(Minecraft mc) {
        ServerData sd = mc.getCurrentServer();
        if (sd != null) return true;
        return instance.allowSingleplayer && mc.getSingleplayerServer() != null;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase(Locale.ROOT);
        if (s.endsWith(":25565")) s = s.substring(0, s.length() - 6);
        return s;
    }

    // ---- Zufalls-Helfer ----
    public static int rand(int a, int b) {
        if (b < a) { int t = a; a = b; b = t; }
        return ThreadLocalRandom.current().nextInt(a, b + 1);
    }

    public static float randF(float a, float b) {
        if (a == b) return a;
        return a + ThreadLocalRandom.current().nextFloat() * (b - a);
    }

    public static int msToTicks(int ms) {
        return Math.max(1, Math.round(ms / 50f));
    }
}
