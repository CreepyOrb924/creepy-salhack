package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.ShulkerTooltips;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipData;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ShulkerDataMixin {
 @Inject(method="getTooltipData",at=@At("HEAD"),cancellable=true)private void salhack$data(CallbackInfoReturnable<Optional<TooltipData>> ci){var data=ShulkerTooltips.data((ItemStack)(Object)this);if(data!=null)ci.setReturnValue(Optional.of(data));}
}
