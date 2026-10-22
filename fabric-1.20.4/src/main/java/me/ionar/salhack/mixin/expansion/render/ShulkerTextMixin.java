package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.RenderHooks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.BlockView;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Suppress only the vanilla container inventory summary, retaining the title, lore and other metadata. */
@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerTextMixin {
    @Inject(method="appendTooltip",at=@At("HEAD"),cancellable=true)
    private void salhack$gridOnly(ItemStack stack,BlockView world,List<Text> tooltip,TooltipContext context,CallbackInfo ci) {
        if(RenderHooks.on("ShulkerPreview"))ci.cancel();
    }
}
