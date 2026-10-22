package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameHud.class)
public abstract class PumpkinMixin {
 @Inject(method="renderOverlay",at=@At("HEAD"),cancellable=true)private void salhack$pumpkin(DrawContext context,Identifier texture,float opacity,CallbackInfo ci){if(RenderHooks.hide("PumpkinOverlay")&&texture.getPath().contains("pumpkinblur"))ci.cancel();}
}
