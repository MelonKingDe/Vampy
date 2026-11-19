package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Slot auf Holy Ice wechseln -> Rechtsklick -> zurueck auf den vorherigen Slot. */
final class TwinclawTask {
    private final Config cfg = Config.get();
    private int wait;
    private int phase = 0;      // 0 = wechseln, 1 = benutzen, 2 = zurueck
    private int prevSlot = -1;
    private boolean switched = false;

    TwinclawTask() {
        wait = Config.msToTicks(Config.rand(cfg.twinclawDelayMinMs, cfg.twinclawDelayMaxMs));
    }

    /** @return true, wenn der Ablauf fertig ist */
    boolean tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return true;
        if (wait > 0) { wait--; return false; }

        switch (phase) {
            case 0 -> {
                prevSlot = p.getInventory().getSelectedSlot();
                int target = cfg.holyIceSlot - 1;
                if (target != prevSlot) {
                    p.getInventory().setSelectedSlot(target);
                    switched = true;
                }
                phase = 1;
                wait = Config.rand(1, 2);
            }
            case 1 -> {
                mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                p.swing(InteractionHand.MAIN_HAND);
                phase = 2;
                wait = Config.rand(2, 3);
            }
            default -> {
                if (switched && prevSlot >= 0) {
                    p.getInventory().setSelectedSlot(prevSlot);
                }
                return true;
            }
        }
        return false;
    }

    void cancel(Minecraft mc) {
        // Falls mitten im Ablauf abgebrochen wird, den Slot wiederherstellen.
        if (switched && prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
    }
}
