package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.FreeCamera;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(MinecraftClient.class)
public abstract class FreecamInteractionMixin {
 @Inject(method="doAttack",at=@At("HEAD"),cancellable=true)private void salhack$attack(CallbackInfoReturnable<Boolean> ci){if(FreeCamera.active())ci.setReturnValue(false);}
 @Inject(method="doItemUse",at=@At("HEAD"),cancellable=true)private void salhack$use(CallbackInfo ci){if(FreeCamera.active())ci.cancel();}
 @Inject(method="handleBlockBreaking",at=@At("HEAD"),cancellable=true)private void salhack$break(boolean breaking,CallbackInfo ci){if(FreeCamera.active())ci.cancel();}
}
