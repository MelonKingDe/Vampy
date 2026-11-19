package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Wartet kurz, benutzt Holy Ice und stellt danach den vorherigen Slot wieder her. */
final class TwinclawTask {
    private final Config cfg = Config.get();
    private int wait = 0;
    private int prevSlot = -1;
    private boolean switched = false;
    private boolean used = false;

    TwinclawTask() {
        // Gewuenschte Reaktionszeit: 100-200 ms (Minecraft verarbeitet Client-Ticks alle ca. 50 ms).
        wait = Config.msToTicks(Config.rand(cfg.twinclawDelayMinMs, cfg.twinclawDelayMaxMs));
    }

    /** @return true, wenn der Ablauf fertig ist */
    boolean tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return true;
        if (wait > 0) { wait--; return false; }

        if (!used) {
            prevSlot = p.getInventory().getSelectedSlot();
            int target = cfg.holyIceSlot - 1;
            if (target != prevSlot) {
                p.getInventory().setSelectedSlot(target);
                switched = true;
            }

            // Slotwechsel-Paket und Use-Paket werden direkt hintereinander gesendet.
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            p.swing(InteractionHand.MAIN_HAND);
            used = true;
            wait = 1; // Einen Tick warten, bevor der vorherige Slot wiederhergestellt wird.
            return false;
        }

        if (switched && prevSlot >= 0) {
            p.getInventory().setSelectedSlot(prevSlot);
        }
        return true;
    }

    void cancel(Minecraft mc) {
        // Falls mitten im Ablauf abgebrochen wird, den Slot wiederherstellen.
        if (switched && prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
    }
}
