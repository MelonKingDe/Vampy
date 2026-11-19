package dev.vampslayerhelper.mixin;

import dev.vampslayerhelper.Events;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void vsh$title(Component title, CallbackInfo ci) {
        if (title != null) Events.onText(title.getString());
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void vsh$subtitle(Component subtitle, CallbackInfo ci) {
        if (subtitle != null) Events.onText(subtitle.getString());
    }

    // Hypixel may display Impel instructions in the action bar instead of a title.
    @Inject(method = "setOverlayMessage", at = @At("HEAD"), require = 0)
    private void vsh$actionbar(Component message, boolean animate, CallbackInfo ci) {
        if (message != null) Events.onText(message.getString());
    }
}
