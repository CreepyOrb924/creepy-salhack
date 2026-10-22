package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(PlayerEntity.class)
public class MovementPlayerMixin {
    @Inject(method="getOffGroundSpeed",at=@At("RETURN"),cancellable=true)
    private void salhack$glideSpeed(CallbackInfoReturnable<Float> ci) {
        if(ready() && (Object)this==mc.player && on("GlideModule") && mc.player.fallDistance>0 && !mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && !mc.player.isTouchingWater() && !mc.player.isInLava() && !mc.player.isClimbing() && !mc.player.verticalCollision) ci.setReturnValue(ci.getReturnValueF()*1.21337F);
    }
}
