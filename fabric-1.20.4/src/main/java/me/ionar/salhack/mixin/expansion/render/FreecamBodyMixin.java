package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.FreeCamera;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayerEntity.class)
public abstract class FreecamBodyMixin {
 @Inject(method="tickMovement",at=@At("HEAD"),cancellable=true)private void salhack$freeze(CallbackInfo ci){if(FreeCamera.normal())ci.cancel();}
}
