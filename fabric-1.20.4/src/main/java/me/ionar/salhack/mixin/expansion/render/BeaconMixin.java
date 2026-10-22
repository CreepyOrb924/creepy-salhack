package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BeaconBlockEntityRenderer.class)
public abstract class BeaconMixin {
 @Inject(method="render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",at=@At("HEAD"),cancellable=true)private void salhack$beacon(CallbackInfo ci){if(RenderHooks.hide("Beacon"))ci.cancel();}
}
