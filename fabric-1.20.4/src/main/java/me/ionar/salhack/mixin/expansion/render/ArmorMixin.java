package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorMixin {
 @Inject(method="render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",at=@At("HEAD"),cancellable=true)private void salhack$armor(MatrixStack matrices,VertexConsumerProvider consumers,int light,LivingEntity entity,float a,float b,float c,float d,float e,float f,CallbackInfo ci){if(RenderHooks.hide("NoArmor")&&(!RenderHooks.hide("NoArmorPlayers")||entity instanceof PlayerEntity))ci.cancel();}
}
