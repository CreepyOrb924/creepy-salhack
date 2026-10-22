package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class EffectMixin {
 @Inject(method="tiltViewWhenHurt",at=@At("HEAD"),cancellable=true)private void salhack$hurt(CallbackInfo ci){if(RenderHooks.hide("HurtCamera"))ci.cancel();}
 @Inject(method="showFloatingItem",at=@At("HEAD"),cancellable=true)private void salhack$totem(ItemStack stack,CallbackInfo ci){if(stack.isOf(Items.TOTEM_OF_UNDYING)&&RenderHooks.hide("TotemAnimation"))ci.cancel();}
}
