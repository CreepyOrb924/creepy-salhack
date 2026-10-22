package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.ActionResult;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ClientPlayerInteractionManager.class)
public abstract class EnderChestMixin {
 @Inject(method="interactBlock",at=@At("HEAD"))private void salhack$ender(ClientPlayerEntity player,Hand hand,BlockHitResult hit,CallbackInfoReturnable<ActionResult> ci){var world=MinecraftClient.getInstance().world;if(RenderHooks.on("ShulkerPreview")&&world!=null&&world.getBlockState(hit.getBlockPos()).isOf(Blocks.ENDER_CHEST))ShulkerTooltips.clickedEnder();}
}
