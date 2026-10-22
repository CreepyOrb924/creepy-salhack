package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.WorldDiagnostics;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(RenderTickCounter.class)
public class AutomationTimerMixin {
 @ModifyExpressionValue(method="beginRenderTick",at=@At(value="INVOKE",target="Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F",remap=false)) private float timer(float millis){return millis/WorldDiagnostics.timerMultiplier();}
}
