package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Wird von den Mixins aufgerufen (Titel-/Actionbar-Texte und eingehende Sounds). */
public final class Events {
    private static final Pattern IMPEL =
            Pattern.compile("(?i)impel\\W*\\s*(CLICK\\s*UP|CLICK\\s*DOWN|JUMP|SNEAK)");
    private static final Map<String, Long> debugSeen = new HashMap<>();
    private record NoteCue(float pitch, long tick) {}
    private static final Deque<NoteCue> noteCue = new ArrayDeque<>();

    private Events() {}

    public static void onText(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread()) handleText(mc, text);
        else mc.execute(() -> handleText(mc, text));
    }

    public static void onSound(String soundId, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread()) handleSound(mc, soundId, pitch);
        else mc.execute(() -> handleSound(mc, soundId, pitch));
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

    private static void handleSound(Minecraft mc, String id, float pitch) {
        if (id == null) return;
        Config c = Config.get();

        if (c.soundDebug && mc.player != null && !id.contains("step")) {
            long now = VampSlayerHelperClient.tickCounter;
            Long last = debugSeen.get(id);
            if (last == null || now - last > 20) {
                mc.gui.hud.getChat().addClientSystemMessage(Component.literal("[VSH] Sound: " + id + " (pitch=" + pitch + ")"));
            }
            debugSeen.put(id, now);
        }

        String lowerId = id.toLowerCase();
        String wanted = c.twinclawSoundId == null ? "" : c.twinclawSoundId.trim();

        // Twinclaws use a short ascending note-block/beep telegraph. Detect three
        // consecutive note-block sound packets with rising pitch instead of
        // requiring users to know the server-side sound registry ID.
        if (lowerId.contains("note_block")) {
            long now = VampSlayerHelperClient.tickCounter;
            while (!noteCue.isEmpty() && now - noteCue.peekFirst().tick() > 16) noteCue.removeFirst();
            noteCue.addLast(new NoteCue(pitch, now));
            while (noteCue.size() > 3) noteCue.removeFirst();
            if (noteCue.size() == 3) {
                NoteCue[] cue = noteCue.toArray(new NoteCue[0]);
                boolean timed = cue[1].tick() - cue[0].tick() >= 1
                        && cue[1].tick() - cue[0].tick() <= 12
                        && cue[2].tick() - cue[1].tick() >= 1
                        && cue[2].tick() - cue[1].tick() <= 12;
                boolean rising = cue[1].pitch() > cue[0].pitch() + 0.005f
                        && cue[2].pitch() > cue[1].pitch() + 0.005f;
                if (timed && rising) {
                    noteCue.clear();
                    VampSlayerHelperClient.triggerTwinclaw(mc);
                }
            }
        }

        // Optional exact/partial sound-ID override remains available in config.
        if (!wanted.isEmpty() && lowerId.contains(wanted.toLowerCase())) {
            VampSlayerHelperClient.triggerTwinclaw(mc);
        }
    }
}
