package dev.vampslayerhelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

public final class Config {
    // ---- Sicherheit ----
    public boolean enabled = false;

    // ---- Twinclaws ----
    /** Sound-ID (oder Teil davon) des Twinclaw-Telegraphs. Per Sound-Debug herausfinden. */
    public String twinclawSoundId = "";
    public boolean soundDebug = false;
    public int twinclawCooldownTicks = 80;
    public int holyIceSlot = 2;              // Hotbar-Slot 1-9
    public int twinclawDelayMinMs = 450;
    public int twinclawDelayMaxMs = 750;

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
        if (twinclawSoundId == null) twinclawSoundId = "";
        holyIceSlot = Math.max(1, Math.min(9, holyIceSlot));
        if (twinclawDelayMaxMs < twinclawDelayMinMs) twinclawDelayMaxMs = twinclawDelayMinMs;
        if (impelReactMaxMs < impelReactMinMs) impelReactMaxMs = impelReactMinMs;
        moveMinTicks = Math.max(2, moveMinTicks);
        if (moveMaxTicks < moveMinTicks) moveMaxTicks = moveMinTicks;
        sneakHoldTicks = Math.max(2, sneakHoldTicks);
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
