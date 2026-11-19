package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Wechselt auf Slot 4, benutzt Holy Ice und kehrt danach immer zu Slot 1 zurueck. */
final class TwinclawTask {
    private final Config cfg = Config.get();
    private int wait;
    private int stage = 0;
    private boolean switched = false;

    TwinclawTask() {
        // Zufallige Reaktionszeit von 100 bis 200 ms.
        wait = Config.msToTicks(Config.rand(cfg.twinclawDelayMinMs, cfg.twinclawDelayMaxMs));
    }

    /** @return true, wenn der Ablauf fertig ist */
    boolean tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return true;

        if (wait > 0) {
            wait--;
            return false;
        }

        if (stage == 0) {
            // Slot 4 (Index 3) auswaehlen und einen Tick Zeit zum Synchronisieren geben.
            p.getInventory().setSelectedSlot(3);
            switched = true;
            stage = 1;
            return false;
        }

        if (stage == 1) {
            // Rechtsklick mit dem Gegenstand in Slot 4.
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            p.swing(InteractionHand.MAIN_HAND);
            stage = 2;
            wait = 1;
            return false;
        }

        // Nach der Benutzung immer auf Hotbar-Slot 1 zurueck, nicht auf den alten Slot.
        p.getInventory().setSelectedSlot(0);
        switched = false;
        return true;
    }

    void cancel(Minecraft mc) {
        // Auch bei Abbruch nicht auf Slot 4 stehen bleiben.
        if (switched && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(0);
            switched = false;
        }
    }
}
