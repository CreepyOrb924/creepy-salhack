package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EnchantingTableBlockEntityRenderer.class)
public abstract class EnchantingMixin {
 @Inject(method="render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",at=@At("HEAD"),cancellable=true)private void salhack$book(CallbackInfo ci){if(RenderHooks.hide("EnchantingTable"))ci.cancel();}
}
