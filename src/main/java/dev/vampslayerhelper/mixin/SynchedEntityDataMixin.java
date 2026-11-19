package dev.vampslayerhelper.mixin;

import dev.vampslayerhelper.Events;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {

    @Shadow
    @Final
    private SyncedDataHolder entity;

    @Inject(method = "assignValues", at = @At("TAIL"))
    private void vsh$assignValues(List<SynchedEntityData.DataValue<?>> entries, CallbackInfo ci) {
        if (this.entity instanceof ArmorStand stand) {
            Events.onArmorStandUpdate(stand);
        }
    }
}
