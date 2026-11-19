package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Auf einen Hotbar-Slot wechseln -> Rechtsklick -> zurueck auf den vorherigen Slot (Holy Ice, Healing Melon). */
final class SlotUseTask {
    private final int slot;     // 0-8
    private int wait;
    private int phase = 0;      // 0 = wechseln, 1 = benutzen, 2 = zurueck
    private int prevSlot = -1;
    private boolean switched = false;

    SlotUseTask(int slot1Based, int delayMinMs, int delayMaxMs) {
        this.slot = Math.max(0, Math.min(8, slot1Based - 1));
        this.wait = Config.msToTicks(Config.rand(delayMinMs, delayMaxMs));
    }

    /** @return true, wenn der Ablauf fertig ist */
    boolean tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return true;
        if (wait > 0) { wait--; return false; }

        switch (phase) {
            case 0 -> {
                prevSlot = p.getInventory().getSelectedSlot();
                if (slot != prevSlot) {
                    p.getInventory().setSelectedSlot(slot);
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

    /** Bei Abbruch mitten im Ablauf den urspruenglichen Slot wiederherstellen. */
    void cancel(Minecraft mc) {
        if (switched && prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
    }
}
