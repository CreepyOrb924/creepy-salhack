package me.ionar.salhack.mixin.expansion.movement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import static me.ionar.salhack.modules.ExpansionMovementModules.*;
@Mixin(LivingEntity.class)
public class MovementLivingMixin {
    @Inject(method="hasStatusEffect",at=@At("HEAD"),cancellable=true)
    private void salhack$levitation(StatusEffect effect,CallbackInfoReturnable<Boolean> ci) {
        if(ready() && (Object)this==mc.player && effect==StatusEffects.LEVITATION && on("AntiLevitation")) ci.setReturnValue(false);
    }
    @Inject(method="travel",at=@At("HEAD"),cancellable=true)
    private void salhack$flight(Vec3d input,CallbackInfo ci) {
        if(ready() && (Object)this==mc.player && flightActive()) { flightMotion(); mc.player.move(MovementType.SELF,mc.player.getVelocity()); ci.cancel(); }
    }
}
