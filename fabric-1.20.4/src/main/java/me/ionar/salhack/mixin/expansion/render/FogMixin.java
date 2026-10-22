package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.*;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BackgroundRenderer.class)
public abstract class FogMixin {
 @Inject(method="applyFog",at=@At("TAIL"))private static void salhack$fog(CallbackInfo ci){var mc=MinecraftClient.getInstance();boolean blind=RenderHooks.hide("Blindness")&&mc.getCameraEntity() instanceof LivingEntity living&&living.hasStatusEffect(StatusEffects.BLINDNESS);if(RenderHooks.on("AntiFog")||blind){RenderSystem.setShaderFogStart(Float.MAX_VALUE);RenderSystem.setShaderFogEnd(Float.MAX_VALUE);}}
}
