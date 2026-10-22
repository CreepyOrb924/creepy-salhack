package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.MapRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MapRenderer.class)
public abstract class MapMixin {
 @Inject(method="draw",at=@At("HEAD"),cancellable=true)private void salhack$map(CallbackInfo ci){if(RenderHooks.hide("Maps"))ci.cancel();}
}
