package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Wird von den Mixins aufgerufen. Die Erkennung folgt SkyHanni (VampireSlayerFeatures). */
public final class Events {
    private static final Pattern IMPEL =
            Pattern.compile("(?i)impel\\W*\\s*(CLICK\\s*UP|CLICK\\s*DOWN|JUMP|SNEAK)");

    private Events() {}

    /** Titel/Untertitel (Impel-Befehle). */
    public static void onText(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread()) handleText(mc, text);
        else mc.execute(() -> handleText(mc, text));
    }

    private static void handleText(Minecraft mc, String text) {
        if (text == null) return;
        Matcher m = IMPEL.matcher(text);
        if (!m.find()) return;
        String name = m.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
        try {
            VampSlayerHelperClient.triggerImpel(mc, ImpelTask.Command.valueOf(name));
        } catch (IllegalArgumentException ignored) { }
    }

    /**
     * Twinclaws: Der Boss zeigt einen Armor Stand mit dem Namen "TWINCLAWS 1.2s" an.
     * Wie bei SkyHanni zaehlt der Stand nur, wenn er maximal 5 Bloecke vom Boss entfernt ist.
     */
    public static void onArmorStandUpdate(ArmorStand stand) {
        Minecraft mc = Minecraft.getInstance();
        // Nur Client-Thread (ignoriert Entities des integrierten Servers).
        if (!mc.isSameThread() || mc.level == null || mc.player == null) return;

        Component custom = stand.getCustomName();
        if (custom == null) return;
        if (!custom.getString().toUpperCase(Locale.ROOT).contains("TWINCLAWS")) return;

        for (Player p : mc.level.players()) {
            if (!isBoss(p, mc)) continue;
            if (p.distanceToSqr(stand) > 25.0) continue;                     // 5 Bloecke wie SkyHanni
            VampSlayerHelperClient.triggerTwinclaw(mc);
            return;
        }
    }

    /** Ist ein Boss-Spieler (Name enthaelt "Bloodfiend") in deiner Naehe? */
    public static boolean bossNearby(Minecraft mc) {
        if (mc.level == null) return false;
        for (Player p : mc.level.players()) {
            if (isBoss(p, mc)) return true;
        }
        return false;
    }

    private static boolean isBoss(Player p, Minecraft mc) {
        if (!(p instanceof RemotePlayer) || mc.player == null) return false;
        Config cfg = Config.get();
        String bossName = cfg.bossNameContains == null ? "" : cfg.bossNameContains.toLowerCase(Locale.ROOT);
        if (!p.getName().getString().toLowerCase(Locale.ROOT).contains(bossName)) return false;
        return p.distanceTo(mc.player) <= cfg.maxBossDistance;
    }
}
