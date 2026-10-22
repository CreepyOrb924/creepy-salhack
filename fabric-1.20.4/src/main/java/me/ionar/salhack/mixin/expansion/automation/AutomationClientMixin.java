package me.ionar.salhack.mixin.expansion.automation;
import me.ionar.salhack.automation.AutomationHooks;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MinecraftClient.class)
public class AutomationClientMixin {
 @Shadow private int itemUseCooldown;
 @Inject(method="tick",at=@At("HEAD")) private void fastPlace(CallbackInfo ci){if(AutomationHooks.fastPlace())itemUseCooldown=0;}
}
