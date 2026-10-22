package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
public abstract class ViewClipMixin {
 @Inject(method="clipToSpace",at=@At("HEAD"),cancellable=true)private void salhack$clip(double distance,CallbackInfoReturnable<Double> ci){if(RenderHooks.on("ViewClip"))ci.setReturnValue(distance);}
}
