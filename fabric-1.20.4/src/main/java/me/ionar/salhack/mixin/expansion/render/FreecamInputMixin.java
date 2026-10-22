package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.FreeCamera;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
public abstract class FreecamInputMixin extends Input {
 @Inject(method="tick",at=@At("TAIL"))private void salhack$input(boolean slow,float factor,CallbackInfo ci){if(FreeCamera.active()){movementForward=0;movementSideways=0;pressingForward=false;pressingBack=false;pressingLeft=false;pressingRight=false;jumping=false;sneaking=false;}}
}
