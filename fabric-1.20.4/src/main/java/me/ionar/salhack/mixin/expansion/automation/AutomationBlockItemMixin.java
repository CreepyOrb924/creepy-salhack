package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.AutomationHooks;
import net.minecraft.item.*;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockItem.class)
public class AutomationBlockItemMixin {
 @Inject(method="place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z",at=@At("HEAD"),cancellable=true) private void noPrediction(ItemPlacementContext context,BlockState state,CallbackInfoReturnable<Boolean> cir){if(context.getWorld().isClient&&AutomationHooks.on("NoGlitchBlocks")&&AutomationHooks.manager.get("NoGlitchBlocks").flag("Place"))cir.setReturnValue(true);}
}
