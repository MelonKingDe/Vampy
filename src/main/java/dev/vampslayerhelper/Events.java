package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Wird von den Mixins aufgerufen (Titel-/Actionbar-Texte und eingehende Sounds). */
public final class Events {
    private static final Pattern IMPEL =
            Pattern.compile("(?i)impel\\W*\\s*(CLICK\\s*UP|CLICK\\s*DOWN|JUMP|SNEAK)");
    private static final Map<String, Long> debugSeen = new HashMap<>();

    private Events() {}

    public static void onText(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread()) handleText(mc, text);
        else mc.execute(() -> handleText(mc, text));
    }

    public static void onSound(String soundId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread()) handleSound(mc, soundId);
        else mc.execute(() -> handleSound(mc, soundId));
    }

    private static void handleText(Minecraft mc, String text) {
        if (text == null) return;
        Matcher m = IMPEL.matcher(text);
        if (!m.find()) return;
        String name = m.group(1).toUpperCase().replaceAll("\\s+", "_");
        try {
            VampSlayerHelperClient.triggerImpel(mc, ImpelTask.Command.valueOf(name));
        } catch (IllegalArgumentException ignored) { }
    }

    private static void handleSound(Minecraft mc, String id) {
        if (id == null) return;
        Config c = Config.get();

        if (c.soundDebug && mc.player != null && !id.contains("step")) {
            long now = VampSlayerHelperClient.tickCounter;
            Long last = debugSeen.get(id);
            if (last == null || now - last > 20) {
                mc.player.displayClientMessage(Component.literal("[VSH] Sound: " + id), false);
            }
            debugSeen.put(id, now);
        }

        String wanted = c.twinclawSoundId == null ? "" : c.twinclawSoundId.trim();
        if (!wanted.isEmpty() && id.toLowerCase().contains(wanted.toLowerCase())) {
            VampSlayerHelperClient.triggerTwinclaw(mc);
        }
    }
}
