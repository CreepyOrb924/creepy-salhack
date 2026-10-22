package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.FreeCamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class FreecamLookMixin {
 @Inject(method="changeLookDirection",at=@At("HEAD"),cancellable=true)private void salhack$look(double x,double y,CallbackInfo ci){if(FreeCamera.look((Entity)(Object)this,x,y))ci.cancel();}
}
