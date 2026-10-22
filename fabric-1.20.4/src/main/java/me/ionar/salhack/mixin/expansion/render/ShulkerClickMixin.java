package me.ionar.salhack.mixin.expansion.render;
import me.ionar.salhack.render.expansion.ShulkerTooltips;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(HandledScreen.class)
public abstract class ShulkerClickMixin {
 @Shadow protected Slot focusedSlot;
 @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)private void salhack$preview(double x,double y,int button,CallbackInfoReturnable<Boolean> ci){if(button==2&&focusedSlot!=null&&ShulkerTooltips.open(focusedSlot.getStack()))ci.setReturnValue(true);}
}
