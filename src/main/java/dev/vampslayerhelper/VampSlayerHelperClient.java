package dev.vampslayerhelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class VampSlayerHelperClient implements ClientModInitializer {
    public static long tickCounter = 0;

    private enum Kind { TWINCLAW, HEAL }

    private static KeyMapping toggleKey;

    /** Alles, was den Hotbar-Slot wechselt, laeuft ueber diese eine Aufgabe (kein Slot-Chaos). */
    private static SlotUseTask slotTask;
    private static Kind slotKind;
    private static ImpelTask impel;

    private static long lastTwinclawTick = -1000;
    private static long lastHealTick = -1000;
    private static long lastImpelTick = -1000;
    private static ImpelTask.Command lastImpelCmd;

    @Override
    public void onInitializeClient() {
        Config.load();

        // Falls diese Zeile nicht kompiliert: Registrierung aus deiner Command-Wechsel-Mod uebernehmen.
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vampslayerhelper.toggle", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MISC));

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
            if (cfg.enabled && !Config.isServerAllowed(mc)) {
                state += " (dieser Server ist nicht freigegeben - nichts passiert)";
            }
            msg(mc, "Vampire Slayer Helper: " + state, true);
            if (!cfg.enabled) cancelAll(mc);
        }

        if (mc.player == null || mc.level == null || !isActive(mc)) {
            cancelAll(mc);
            return;
        }

        if (slotTask != null && slotTask.tick(mc)) { slotTask = null; slotKind = null; }
        if (impel != null && impel.tick(mc)) impel = null;
        checkAutoHeal(mc);
    }

    static boolean isActive(Minecraft mc) {
        return Config.get().enabled && Config.isServerAllowed(mc);
    }

    /** Unter der eingestellten HP-Schwelle: Healing Melon benutzen. */
    private static void checkAutoHeal(Minecraft mc) {
        Config cfg = Config.get();
        if (!cfg.autoHeal || slotTask != null || mc.gui.screen() != null) return;
        LocalPlayer p = mc.player;
        if (p == null || p.isDeadOrDying()) return;
        float max = p.getMaxHealth();
        float hp = p.getHealth();
        if (max <= 0 || hp <= 0) return;
        if (hp / max > cfg.healThresholdPercent / 100f) return;
        if (tickCounter - lastHealTick < cfg.healCooldownTicks) return;
        if (cfg.healOnlyNearBoss && !Events.bossNearby(mc)) return;

        lastHealTick = tickCounter;
        slotTask = new SlotUseTask(cfg.healingMelonSlot, cfg.healDelayMinMs, cfg.healDelayMaxMs);
        slotKind = Kind.HEAL;
    }

    static void triggerTwinclaw(Minecraft mc) {
        Config cfg = Config.get();
        if (!isActive(mc) || mc.gui.screen() != null) return;
        if (slotTask != null && slotKind == Kind.TWINCLAW) return;
        if (tickCounter - lastTwinclawTick < cfg.twinclawCooldownTicks) return;
        // Twinclaws hat Vorrang: eine laufende Heilung wird abgebrochen (Slot wird zurueckgesetzt).
        if (slotTask != null) slotTask.cancel(mc);
        lastTwinclawTick = tickCounter;
        slotTask = new SlotUseTask(cfg.holyIceSlot, cfg.twinclawDelayMinMs, cfg.twinclawDelayMaxMs);
        slotKind = Kind.TWINCLAW;
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
        if (slotTask != null) { slotTask.cancel(mc); slotTask = null; slotKind = null; }
        if (impel != null) { impel.cancel(mc); impel = null; }
    }

    private static void msg(Minecraft mc, String text, boolean overlay) {
        if (mc.player != null) {
            Component message = Component.literal(text);
            if (overlay) mc.player.sendOverlayMessage(message);
            else mc.player.sendSystemMessage(message);
        }
    }
}
