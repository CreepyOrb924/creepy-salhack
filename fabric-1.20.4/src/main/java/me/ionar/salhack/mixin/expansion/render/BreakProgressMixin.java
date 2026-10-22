package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.ExpansionOverlay;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WorldRenderer.class)
public abstract class BreakProgressMixin {
 @Inject(method="setBlockBreakingInfo",at=@At("TAIL"))private void salhack$progress(int id,BlockPos pos,int stage,CallbackInfo ci){ExpansionOverlay.breaking(id,pos,stage);}
}
