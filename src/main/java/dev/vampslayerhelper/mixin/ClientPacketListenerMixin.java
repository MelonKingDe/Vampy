package dev.vampslayerhelper.mixin;

import dev.vampslayerhelper.Events;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleSetTitleText", at = @At("TAIL"), require = 0)
    private void vsh$title(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        Events.onText(packet.text().getString());
    }

    @Inject(method = "handleSetSubtitleText", at = @At("TAIL"), require = 0)
    private void vsh$subtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        Events.onText(packet.text().getString());
    }

    @Inject(method = "handleSetActionBarText", at = @At("TAIL"), require = 0)
    private void vsh$actionbar(ClientboundSetActionBarTextPacket packet, CallbackInfo ci) {
        Events.onText(packet.text().getString());
    }

    @Inject(method = "handleSoundEvent", at = @At("TAIL"), require = 0)
    private void vsh$sound(ClientboundSoundPacket packet, CallbackInfo ci) {
        Events.onSound(packet.getSound().getRegisteredName());
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("TAIL"), require = 0)
    private void vsh$soundEntity(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        Events.onSound(packet.getSound().getRegisteredName());
    }
}
