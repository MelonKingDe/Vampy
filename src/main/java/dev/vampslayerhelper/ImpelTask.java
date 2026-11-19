package dev.vampslayerhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

/**
 * Fuehrt einen Impel-Befehl aus. Die Kamera wird nicht gesnappt, sondern ueber mehrere Ticks
 * mit Smoothstep-Easing bewegt (inkl. leichter Zufallsabweichung) und danach zurueckgefuehrt.
 */
final class ImpelTask {
    enum Command { CLICK_UP, CLICK_DOWN, JUMP, SNEAK }

    private enum Phase { REACT, OUT, ACT, HOLD, BACK }

    private final Command cmd;
    private final Config cfg = Config.get();

    private Phase phase = Phase.REACT;
    private int wait;
    private int step, steps;
    private float totalPitch, totalYaw;
    private boolean jumpHeld, sneakHeld, attackHeld;
    private final int requestedDurationTicks;

    ImpelTask(Command cmd, int requestedDurationTicks) {
        this.cmd = cmd;
        this.requestedDurationTicks = requestedDurationTicks;
        this.wait = Config.msToTicks(Config.rand(cfg.impelReactMinMs, cfg.impelReactMaxMs));
    }

    /** @return true, wenn der Ablauf fertig ist */
    boolean tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return true;
        // A real left-click is required for CLICK UP; swing() alone is only cosmetic.
        if (attackHeld) {
            mc.options.keyAttack.setDown(false);
            attackHeld = false;
        }
        if (wait > 0) { wait--; return false; }

        switch (phase) {
            case REACT -> {
                switch (cmd) {
                    case CLICK_UP, CLICK_DOWN -> beginMove(p);
                    case JUMP -> {
                        mc.options.keyJump.setDown(true);
                        jumpHeld = true;
                        wait = requestedDurationTicks > 0
                                ? requestedDurationTicks : Config.rand(2, 3);
                        phase = Phase.HOLD;
                    }
                    case SNEAK -> {
                        mc.options.keyShift.setDown(true);
                        sneakHeld = true;
                        // Use the duration displayed by Hypixel (e.g. "SNEAK 1.6s").
                        // Fall back to config only if the server did not include a duration.
                        int holdTicks = requestedDurationTicks > 0
                                ? requestedDurationTicks : cfg.sneakHoldTicks;
                        wait = Math.max(1, holdTicks);
                        phase = Phase.HOLD;
                    }
                }
            }
            case OUT -> {
                if (moveStep(p, +1)) {
                    phase = Phase.ACT;
                    wait = Config.rand(1, 2);   // kurzer Moment, bevor geklickt wird
                }
            }
            case ACT -> {
                if (cmd == Command.CLICK_UP) {
                    // Let Minecraft process an actual attack-key press on the next tick.
                    mc.options.keyAttack.setDown(true);
                    attackHeld = true;
                    p.swing(InteractionHand.MAIN_HAND);
                } else if (mc.gameMode != null) {
                    mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);  // Rechtsklick
                    p.swing(InteractionHand.MAIN_HAND);
                }
                phase = Phase.BACK;
                step = 0;
                steps = Config.rand(cfg.moveMinTicks, cfg.moveMaxTicks);
                wait = Config.rand(2, 4);                                // kurze Pause nach dem Klick
            }
            case HOLD -> {
                releaseKeys(mc);
                return true;
            }
            case BACK -> {
                if (moveStep(p, -1)) return true;
            }
        }
        return false;
    }

    private void beginMove(LocalPlayer p) {
        float target = (cmd == Command.CLICK_UP ? cfg.lookUpPitch : cfg.lookDownPitch) + Config.randF(-4f, 4f);
        totalPitch = Mth.clamp(target, -90f, 90f) - p.getXRot();
        totalYaw = Config.randF(-cfg.yawJitter, cfg.yawJitter);
        steps = Config.rand(cfg.moveMinTicks, cfg.moveMaxTicks);
        step = 0;
        phase = Phase.OUT;
    }

    /** Relative Bewegung, damit eigene Mausbewegungen nicht ueberschrieben werden. */
    private boolean moveStep(LocalPlayer p, int dir) {
        step++;
        float prev = ease((step - 1) / (float) steps);
        float cur = ease(step / (float) steps);
        float f = (cur - prev) * dir;
        p.setXRot(Mth.clamp(p.getXRot() + totalPitch * f, -90f, 90f));
        p.setYRot(p.getYRot() + totalYaw * f);
        return step >= steps;
    }

    private static float ease(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    private void releaseKeys(Minecraft mc) {
        if (jumpHeld) { mc.options.keyJump.setDown(false); jumpHeld = false; }
        if (sneakHeld) { mc.options.keyShift.setDown(false); sneakHeld = false; }
        if (attackHeld) { mc.options.keyAttack.setDown(false); attackHeld = false; }
    }

    boolean isSameCommand(Command other) {
        return this.cmd == other;
    }

    void cancel(Minecraft mc) {
        releaseKeys(mc);
    }
}
