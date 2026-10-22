package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SignBlockEntityRenderer.class)
public abstract class SignTextMixin {
 @Inject(method="renderText",at=@At("HEAD"),cancellable=true)private void salhack$sign(CallbackInfo ci){if(RenderHooks.hide("SignText"))ci.cancel();}
}
