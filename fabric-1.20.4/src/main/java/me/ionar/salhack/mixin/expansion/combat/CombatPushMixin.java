package me.ionar.salhack.mixin.expansion.combat;
import me.ionar.salhack.SalHackClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class CombatPushMixin {
 @Inject(method="isPushable",at=@At("RETURN"),cancellable=true)private void salhack$push(CallbackInfoReturnable<Boolean> ci){var c=SalHackClient.INSTANCE;if((Object)this==MinecraftClient.getInstance().player&&c!=null&&c.modules.on("Velocity")&&c.modules.get("Velocity").flag("NoPush"))ci.setReturnValue(false);}
}
