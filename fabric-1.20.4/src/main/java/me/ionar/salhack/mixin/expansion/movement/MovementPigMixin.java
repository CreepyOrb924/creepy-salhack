package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(PigEntity.class)
public class MovementPigMixin {
    @Inject(method="getControllingPassenger",at=@At("HEAD"),cancellable=true)
    private void salhack$steer(CallbackInfoReturnable<LivingEntity> ci) {if(ready() && mc.player.getVehicle()==(Object)this && on("EntityControl")) ci.setReturnValue(mc.player);}
    @Inject(method="isSaddled",at=@At("HEAD"),cancellable=true)
    private void salhack$saddle(CallbackInfoReturnable<Boolean> ci) {if(ready() && mc.player.getVehicle()==(Object)this && on("EntityControl")) ci.setReturnValue(true);}
}
