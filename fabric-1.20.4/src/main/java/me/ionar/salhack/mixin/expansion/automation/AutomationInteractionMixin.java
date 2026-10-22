package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.AutomationHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.util.math.BlockPos;
@Mixin(ClientPlayerInteractionManager.class)
public class AutomationInteractionMixin {
 @Shadow private int blockBreakingCooldown;
 @Shadow private float currentBreakingProgress;
 @Shadow private boolean breakingBlock;
 @Inject(method="breakBlock",at=@At("HEAD"),cancellable=true) private void noDestroyPrediction(BlockPos pos,CallbackInfoReturnable<Boolean> cir){if(AutomationHooks.on("NoGlitchBlocks")&&AutomationHooks.manager.get("NoGlitchBlocks").flag("Destroy"))cir.setReturnValue(true);}
 @Inject(method="updateBlockBreakingProgress",at=@At("HEAD")) private void fastMining(BlockPos pos,Direction direction,CallbackInfoReturnable<Boolean> cir){if(AutomationHooks.on("SpeedyGonzales")){blockBreakingCooldown=0;if(currentBreakingProgress>=AutomationHooks.manager.get("SpeedyGonzales").value("Threshold"))currentBreakingProgress=1;}}
 @Inject(method="cancelBlockBreaking",at=@At("HEAD"),cancellable=true) private void retainProgress(CallbackInfo ci){if(breakingBlock&&AutomationHooks.on("SpeedyGonzales")&&AutomationHooks.manager.get("SpeedyGonzales").flag("Reset"))ci.cancel();}
 @ModifyVariable(method="interactBlock",at=@At("HEAD"),argsOnly=true) private BlockHitResult buildHeight(BlockHitResult hit){var mc=MinecraftClient.getInstance();if(AutomationHooks.on("BuildHeight")&&mc.world!=null&&hit.getBlockPos().getY()>=mc.world.getTopY()-1&&hit.getSide()==Direction.UP)return new BlockHitResult(hit.getPos(),Direction.DOWN,hit.getBlockPos(),hit.isInsideBlock());return hit;}
}
