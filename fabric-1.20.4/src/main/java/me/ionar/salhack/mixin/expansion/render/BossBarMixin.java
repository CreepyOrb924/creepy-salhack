package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BossBarHud.class)
public abstract class BossBarMixin {
 @Inject(method="render",at=@At("HEAD"),cancellable=true)private void salhack$boss(CallbackInfo ci){if(RenderHooks.hide("BossHealth"))ci.cancel();}
}
