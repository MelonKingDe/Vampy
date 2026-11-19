package dev.vampslayerhelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class VampSlayerHelperClient implements ClientModInitializer {
    public static long tickCounter = 0;

    private static KeyMapping toggleKey;
    private static KeyMapping debugKey;

    private static TwinclawTask twinclaw;
    private static ImpelTask impel;
    private static long lastTwinclawTick = -1000;
    private static long lastImpelTick = -1000;
    private static ImpelTask.Command lastImpelCmd;

    @Override
    public void onInitializeClient() {
        Config.load();

        // Falls diese zwei Zeilen nicht kompilieren: Registrierung aus deiner Command-Wechsel-Mod uebernehmen.
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vampslayerhelper.toggle", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MISC));
        debugKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vampslayerhelper.debug", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(VampSlayerHelperClient::onTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> cancelAll(client));
    }

    private static void onTick(Minecraft mc) {
        tickCounter++;
        Config cfg = Config.get();

        while (toggleKey.consumeClick()) {
            cfg.enabled = !cfg.enabled;
            Config.save();
            String state = cfg.enabled ? "AN" : "AUS";
            msg(mc, "Vampire Slayer Helper: " + state, true);
            if (!cfg.enabled) cancelAll(mc);
        }
        while (debugKey.consumeClick()) {
            cfg.soundDebug = !cfg.soundDebug;
            Config.save();
            msg(mc, "Sound-Debug: " + (cfg.soundDebug ? "AN" : "AUS"), true);
        }

        if (mc.player == null || mc.level == null || !isActive(mc)) {
            cancelAll(mc);
            return;
        }

        if (twinclaw != null && twinclaw.tick(mc)) twinclaw = null;
        if (impel != null && impel.tick(mc)) impel = null;
    }

    static boolean isActive(Minecraft mc) {
        return Config.get().enabled;
    }

    static void triggerTwinclaw(Minecraft mc) {
        if (!isActive(mc) || twinclaw != null) return;
        if (tickCounter - lastTwinclawTick < Config.get().twinclawCooldownTicks) return;
        lastTwinclawTick = tickCounter;
        twinclaw = new TwinclawTask();
    }

    static void triggerImpel(Minecraft mc, ImpelTask.Command cmd) {
        if (!isActive(mc) || impel != null) return;
        // Der Title aktualisiert sich staendig (Timer) -> gleichen Befehl nicht mehrfach ausloesen.
        if (cmd == lastImpelCmd && tickCounter - lastImpelTick < 50) return;
        lastImpelCmd = cmd;
        lastImpelTick = tickCounter;
        impel = new ImpelTask(cmd);
    }

    private static void cancelAll(Minecraft mc) {
        if (twinclaw != null) { twinclaw.cancel(mc); twinclaw = null; }
        if (impel != null) { impel.cancel(mc); impel = null; }
    }

    private static void msg(Minecraft mc, String text, boolean overlay) {
        if (mc.player == null) return;
        if (overlay) mc.gui.hud.setOverlayMessage(Component.literal(text), false);
        else mc.gui.hud.getChat().addClientSystemMessage(Component.literal(text));
    }
}
