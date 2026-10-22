package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameOverlayRenderer.class)
public abstract class FireOverlayMixin {
 @Inject(method="renderFireOverlay",at=@At("HEAD"),cancellable=true)private static void salhack$fire(CallbackInfo ci){if(RenderHooks.hide("Fire"))ci.cancel();}
}


