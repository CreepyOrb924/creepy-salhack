package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LightmapTextureManager.class)
public abstract class SkylightMixin {
 @Inject(method="update",at=@At("HEAD"),cancellable=true)private void salhack$light(CallbackInfo ci){if(RenderHooks.hide("Skylight"))ci.cancel();}
}
