package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(AbstractHorseEntity.class)
public class MovementHorseMixin {
    @Inject(method="isSaddled",at=@At("HEAD"),cancellable=true)
    private void salhack$saddle(CallbackInfoReturnable<Boolean> ci) {
        if(ready() && mc.player.getVehicle()==(Object)this && on("EntityControl")) ci.setReturnValue(true);
    }
}
