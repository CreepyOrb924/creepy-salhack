package me.ionar.salhack.mixin;
import me.ionar.salhack.SalHackClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method="getStepHeight",at=@At("RETURN"),cancellable=true)
    private void salhack$step(CallbackInfoReturnable<Float> cir) {
        var c=SalHackClient.INSTANCE;
        if(c!=null && (Object)this==MinecraftClient.getInstance().player && c.modules.on("Step")) cir.setReturnValue((float)c.modules.get("Step").value("Height"));
    }
}
