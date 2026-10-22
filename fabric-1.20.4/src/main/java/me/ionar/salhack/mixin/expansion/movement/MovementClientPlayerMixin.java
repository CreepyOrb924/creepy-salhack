package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(ClientPlayerEntity.class)
public class MovementClientPlayerMixin {
    @Inject(method="getMountJumpStrength",at=@At("HEAD"),cancellable=true)
    private void salhack$fullJump(CallbackInfoReturnable<Float> ci) {
        if(ready() && mc.player.getJumpingMount()!=null && on("HorseJump")) ci.setReturnValue(1F);
    }
}
